package com.concert.ticketing.services.inventory;

import com.concert.ticketing.dto.pricing.AvailabilityItem;
import com.concert.ticketing.exception.ConcurrentUpdateException;
import com.concert.ticketing.exception.InsufficientStockException;
import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.repositories.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TicketCategoryRepository categoryRepository;

    public List<AvailabilityItem> getAvailability(UUID eventId) {
        log.info("Getting availability for all categories in event: {}", eventId);

        List<TicketCategoryModel> categories = categoryRepository.findByEventId(eventId);

        return categories.stream()
                .map(this::buildAvailabilityResponse)
                .collect(Collectors.toList());
    }

    private AvailabilityItem buildAvailabilityResponse(TicketCategoryModel category) {
        String stockKey = "stock::" + category.getId();
        Integer availableStock;

        try {
            String stockStr = redisTemplate.opsForValue().get(stockKey);
            availableStock = (stockStr != null) ? Integer.parseInt(stockStr) : category.getAvailableStock();
        } catch (Exception e) {
            log.warn("Failed to get Redis stock for category: {}. Using DB value.", category.getId());
            availableStock = category.getAvailableStock();
        }

        AvailabilityItem response = new AvailabilityItem();
        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());
        response.setConcertName(category.getEvent() != null ? category.getEvent().getName() : null);
        response.setArtistName(category.getEvent() != null ? category.getEvent().getArtist() : null);
        response.setTotalAllocation(category.getTotalAllocation() != null ? category.getTotalAllocation() : 0);
        response.setAvailableStock(availableStock != null ? availableStock : 0);
        response.setStatus((availableStock != null && availableStock > 0) ? "AVAILABLE" : "SOLD_OUT");

        return response;
    }

    private Long reserveFromRedis(String stockKey) {
        try {
            String script = "if redis.call('get', KEYS[1]) == false or tonumber(redis.call('get', KEYS[1])) <= 0 then "
                    +
                    "return -1 " +
                    "else " +
                    "return redis.call('decr', KEYS[1]) " +
                    "end";

            Long result = redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList(stockKey));

            log.debug("Redis script execution result for key {}: {}", stockKey, result);
            return result;

        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failure during reservation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error executing Redis script for key: {}", stockKey, e);
            return null;
        }
    }

    private void rollbackRedis(String stockKey) {
        try {
            Long newValue = redisTemplate.opsForValue().increment(stockKey);
            log.info("Rolled back Redis stock for key: {}. New value: {}", stockKey, newValue);
        } catch (Exception e) {
            log.error("Failed to rollback Redis for key: {}. Manual intervention may be required.",
                    stockKey, e);
        }
    }

    private void reserveFromDatabaseOnly(UUID categoryId) {
        log.warn("Using database-only reservation mode for category: {}", categoryId);

        TicketCategoryModel category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        if (category.getAvailableStock() <= 0) {
            throw new InsufficientStockException("Stock not available");
        }

        category.setAvailableStock(category.getAvailableStock() - 1);

        try {
            categoryRepository.save(category);
            log.info("Successfully reserved ticket (DB-only) for category: {}", categoryId);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConcurrentUpdateException("Stock update conflict, please try again");
        }
    }
}
