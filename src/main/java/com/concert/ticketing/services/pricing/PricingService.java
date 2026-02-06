package com.concert.ticketing.services.pricing;

import com.concert.ticketing.constant.ErrorList;
import com.concert.ticketing.constant.Origin;
import com.concert.ticketing.dto.pricing.PricingAuditDTO;
import com.concert.ticketing.dto.pricing.PricingItem;
import com.concert.ticketing.exception.ServiceException;
import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.repositories.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PricingService {
    private final TicketCategoryRepository ticketCategoryRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_STOCK_PREFIX = "stock::";

    public List<PricingItem> getRealTimePricing(UUID eventId) {
        log.info("Getting real-time pricing for all categories in event: {}", eventId);

        List<TicketCategoryModel> categories = ticketCategoryRepository.findByEventId(eventId);

        if (categories.isEmpty()) {
            log.warn("No ticket categories found for event: {}", eventId);
            throw new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND);
        }

        return categories.stream()
                .map(category -> getRealTimePricingForCategory(category))
                .collect(Collectors.toList());
    }

    public PricingItem getRealTimePricingForCategory(UUID categoryId) {
        TicketCategoryModel ticketCategory = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Failed to inquiry ticket category id '{}'", categoryId);
                    return new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND);
                });
        return getRealTimePricingForCategory(ticketCategory);
    }

    private PricingItem getRealTimePricingForCategory(TicketCategoryModel ticketCategory) {
        UUID categoryId = ticketCategory.getId();
        String stockKey = REDIS_STOCK_PREFIX + categoryId;
        String availableStockStr = redisTemplate.opsForValue().get(stockKey);

        Integer availableStock;

        if (availableStockStr == null) {
            availableStock = ticketCategory.getAvailableStock();
            if (availableStock == null)
                availableStock = 0;
            redisTemplate.opsForValue().set(stockKey, String.valueOf(availableStock), Duration.ofMinutes(10));
        } else {
            availableStock = Integer.parseInt(availableStockStr);
        }

        BigDecimal currentPrice = calculateSurge(ticketCategory.getPrice(),
                ticketCategory.getTotalAllocation() != null ? ticketCategory.getTotalAllocation() : 0,
                availableStock);

        String concertName = ticketCategory.getEvent() != null ? ticketCategory.getEvent().getName() : null;
        String artistName = ticketCategory.getEvent() != null ? ticketCategory.getEvent().getArtist() : null;

        return new PricingItem(categoryId, concertName, artistName, currentPrice, availableStock);
    }

    private BigDecimal calculateSurge(BigDecimal basePrice, int total, int available) {
        double occupancyRate = (double) (total - available) / total;
        double multiplier;

        if (occupancyRate > 0.9)
            multiplier = 2.5;
        else if (occupancyRate > 0.7)
            multiplier = 1.8;
        else if (occupancyRate > 0.4)
            multiplier = 1.3;
        else if (occupancyRate < 0.1)
            multiplier = 0.1;
        else
            multiplier = 1.0;

        return basePrice.multiply(BigDecimal.valueOf(1 + multiplier));
    }
}
