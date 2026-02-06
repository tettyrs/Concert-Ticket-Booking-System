package com.concert.ticketing.services.concert;

import com.concert.ticketing.constant.ErrorList;
import com.concert.ticketing.constant.Origin;
import com.concert.ticketing.dto.concert.ConcertRequest;
import com.concert.ticketing.dto.concert.ConcertResponse;
import com.concert.ticketing.exception.ServiceException;
import com.concert.ticketing.model.EventsModel;
import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.model.VenuesModel;
import com.concert.ticketing.repositories.EventRepository;

import com.concert.ticketing.repositories.VenueRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertService {
    private final EventRepository concertRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final VenueRepository venueRepository;

    public ConcertResponse getAllConcerts(String name, String artist, String venueName,
            ZonedDateTime start, ZonedDateTime end, Integer minCap) {
        try {
            log.info("Retrieving all concerts with filters - Name: {}, Artist: {}, Venue: {}", name, artist, venueName);
            ConcertResponse response = new ConcertResponse();
            String cacheKey = String.format("concerts_all::%s-%s-%s-%s-%s-%s",
                    name, artist, venueName, start, end, minCap);

            // redis
            ConcertResponse cached = (ConcertResponse) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("Cache hit for key: {}", cacheKey);
                return cached;
            }
            log.debug("Cache miss for key: {}. Fetching from database...", cacheKey);

            Specification<EventsModel> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (query.getResultType() != Long.class) {
                    root.fetch("venue", JoinType.LEFT);
                }

                if (name != null)
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                if (artist != null)
                    predicates.add(cb.equal(cb.lower(root.get("artist")), artist.toLowerCase()));

                if (venueName != null) {
                    predicates.add(
                            cb.like(cb.lower(root.join("venue").get("name")), "%" + venueName.toLowerCase() + "%"));
                }

                if (start != null)
                    predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), start));
                if (end != null)
                    predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), end));

                if (minCap != null) {
                    // Logic: sum(categories.availableStock) >= minCap
                    // This is easier to filter in memory for now, or we can use a complex subquery.
                    // Given the Specification structure, a subquery is more robust.
                    // But for simplicity in this baseline, I'll filter the resulting list if the
                    // list is small enough.
                    // However, to be "correct" in JPA:
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            List<EventsModel> events = concertRepository.findAll(spec);

            // Post-filtering for minCap if provided
            if (minCap != null) {
                events = events.stream()
                        .filter(e -> {
                            int totalAvailable = e.getCategories().stream()
                                    .mapToInt(cat -> cat.getAvailableStock() != null ? cat.getAvailableStock() : 0)
                                    .sum();
                            return totalAvailable >= minCap;
                        })
                        .collect(Collectors.toList());
            }
            log.info("Found {} concerts in database matching criteria", events.size());

            List<ConcertResponse.ConcertData> dataList = events.stream().map(e -> {
                ConcertResponse.ConcertData data = new ConcertResponse.ConcertData();
                data.setId(e.getId());
                data.setConcertName(e.getName());
                data.setArtist(e.getArtist());

                if (e.getVenue() != null) {
                    data.setVenueName(e.getVenue().getName());
                    data.setVenueCapacity(e.getVenue().getCapacity());
                }

                data.setDatetime(e.getEventDate().toString());
                data.setStatus(e.getStatus());

                if (e.getCategories() != null) {
                    List<ConcertRequest.TicketCategory> categories = e.getCategories()
                            .stream().map(cat -> {
                                ConcertRequest.TicketCategory catDto = new ConcertRequest.TicketCategory();
                                catDto.setCategoryName(cat.getName());
                                catDto.setBasePrice(cat.getPrice());
                                catDto.setTotalAllocation(cat.getTotalAllocation());
                                return catDto;
                            }).toList();
                    data.setCategories(categories);
                }
                return data;
            }).toList();

            response.setStatus("Success");
            response.setCode("00");
            response.setMessage("Retrieved " + dataList.size() + " concerts");
            response.setData(dataList);

            log.info("Returning {} concerts. Saving to cache with key: {}", dataList.size(), cacheKey);
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(5));
            return response;

        } catch (ServiceException e) {
            log.error("ServiceException in getAllConcerts: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getAllConcerts: {}", e.getMessage(), e);
            throw new ServiceException(Origin.POSTGRE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }
    }

    public ConcertResponse getConcertDetail(UUID id) {
        try {
            log.info("Retrieving detail for concert ID: {}", id);
            ConcertResponse response = new ConcertResponse();
            ConcertResponse.ConcertData concertData = new ConcertResponse.ConcertData();

            String cacheKey = "concert_detail::" + id;
            ConcertResponse cached = (ConcertResponse) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("Cache hit for concert detail: {}", id);
                return cached;
            }
            log.debug("Cache miss for concert detail: {}. Fetching from database...", id);

            EventsModel events = concertRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> {
                        log.warn("Concert detail not found for ID: {}", id);
                        return new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND);
                    });
            log.info("Successfully retrieved concert: {} ({})", events.getName(), events.getArtist());

            concertData.setConcertName(events.getName());
            concertData.setArtist(events.getArtist());
            concertData.setDatetime(events.getEventDate().toString());
            concertData.setStatus(events.getStatus());

            // cek tbl vanue
            if (events.getVenue() != null) {
                concertData.setVenueName(events.getVenue().getName());
                concertData.setVenueCapacity(events.getVenue().getCapacity());
            }

            // cek tbl tix cat
            if (events.getCategories() != null) {
                List<ConcertRequest.TicketCategory> getTikcketCategories = events.getCategories().stream()
                        .map(ticketCategoryModel -> {
                            ConcertRequest.TicketCategory ticketDto = new ConcertRequest.TicketCategory();
                            ticketDto.setCategoryName(ticketCategoryModel.getName());
                            ticketDto.setBasePrice(ticketCategoryModel.getPrice());
                            ticketDto.setTotalAllocation(ticketCategoryModel.getTotalAllocation());
                            return ticketDto;
                        }).toList();
                concertData.setCategories(getTikcketCategories);
            }

            response.setStatus("Success");
            response.setCode("00");
            response.setMessage("Concert detail retrieved successfully");
            response.setData(List.of(concertData));

            log.info("Returning detail for concert: {}. Saving to cache.", events.getName());
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(10));
            return response;
        } catch (ServiceException e) {
            log.error("ServiceException in getConcertDetail for ID {}: code={}, message={}", id, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getConcertDetail for ID {}: {}", id, e.getMessage(), e);
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }
    }

    @Transactional
    public void createConcert(ConcertRequest request) {
        try {
            log.info("Starting createConcert process - Name: {}, Venue: {}", request.getName(), request.getVenue());

            EventsModel event = new EventsModel();
            VenuesModel venue = venueRepository.findByNameIgnoreCase(request.getVenue())
                    .orElseThrow(() -> {
                        log.warn("Failed to create concert: Venue '{}' not found", request.getVenue());
                        return new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND);
                    });

            log.debug("Found venue: {} with capacity: {}", venue.getName(), venue.getCapacity());

            ZoneId zoneId = ZoneId.of(request.getTimezone());
            ZonedDateTime eventTime = request.getDatetime()
                    .withZoneSameLocal(zoneId);

            event.setName(request.getName());
            event.setArtist(request.getArtist());
            event.setEventDate(eventTime);
            event.setTimezone(request.getTimezone());
            event.setStatus(request.getStatus() != null ? request.getStatus() : "UPCOMING");
            event.setDescription(request.getDescription());
            event.setVenue(venue);

            if (request.getCategories() != null) {
                log.debug("Processing {} ticket categories", request.getCategories().size());
                List<TicketCategoryModel> categories = request.getCategories().stream().map(catReq -> {
                    TicketCategoryModel category = new TicketCategoryModel();
                    category.setName(catReq.getCategoryName());
                    category.setPrice(catReq.getBasePrice());
                    category.setTotalAllocation(catReq.getTotalAllocation());
                    category.setAvailableStock(catReq.getTotalAllocation()); // Initialize with total allocation
                    category.setEvent(event);
                    return category;
                }).collect(Collectors.toList());
                event.setCategories(categories);
            }

            concertRepository.save(event);
            log.info("Successfully created concert: {} (ID: {})", event.getName(), event.getId());

        } catch (ServiceException e) {
            log.error("ServiceException in createConcert: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in createConcert: {}", e.getMessage(), e);
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }
    }

    @Transactional
    public void updateConcert(UUID id, ConcertRequest request) {
        try {
            log.info("Starting updateConcert process - ID: {}, Name: {}", id, request.getName());

            EventsModel event = concertRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Failed to update concert: Concert with ID '{}' not found", id);
                        return new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND);
                    });

            log.debug("Found existing concert: {}", event.getName());

            if (request.getName() != null) {
                event.setName(request.getName());
            }
            if (request.getArtist() != null) {
                event.setArtist(request.getArtist());
            }
            if (request.getStatus() != null) {
                event.setStatus(request.getStatus());
            }
            if (request.getDescription() != null) {
                event.setDescription(request.getDescription());
            }

            if (request.getVenue() != null) {
                VenuesModel venue = venueRepository.findByNameIgnoreCase(request.getVenue())
                        .orElseThrow(() -> {
                            log.warn("Failed to update concert: Venue '{}' not found", request.getVenue());
                            return new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND);
                        });
                event.setVenue(venue);
                log.debug("Updated venue to: {}", venue.getName());
            }

            if (request.getDatetime() != null && request.getTimezone() != null) {
                ZoneId zoneId = ZoneId.of(request.getTimezone());
                ZonedDateTime eventTime = request.getDatetime().withZoneSameLocal(zoneId);
                event.setEventDate(eventTime);
                event.setTimezone(request.getTimezone());
                log.debug("Updated event date to: {}", eventTime);
            }

            concertRepository.save(event);

            String detailCacheKey = "concert_detail::" + id;
            redisTemplate.delete(detailCacheKey);

            log.info("Successfully updated concert: {} (ID: {})", event.getName(), event.getId());

        } catch (ServiceException e) {
            log.error("ServiceException in updateConcert: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in updateConcert: {}", e.getMessage(), e);
            throw new ServiceException(Origin.POSTGRE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }
    }
}
