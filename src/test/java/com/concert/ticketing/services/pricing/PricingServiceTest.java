package com.concert.ticketing.services.pricing;

import com.concert.ticketing.dto.pricing.PricingItem;
import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.repositories.TicketCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private TicketCategoryRepository categoryRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private PricingService pricingService;

    @Test
    void shouldGetRealTimePricingForEvent() {
        UUID eventId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        TicketCategoryModel cat = new TicketCategoryModel();
        cat.setId(catId);
        cat.setPrice(new BigDecimal("100000"));
        cat.setAvailableStock(10);
        cat.setTotalAllocation(100);

        when(categoryRepository.findByEventId(eventId)).thenReturn(List.of(cat));
        when(categoryRepository.findById(catId)).thenReturn(java.util.Optional.of(cat));

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("10"); // Match available stock

        List<PricingItem> result = pricingService.getRealTimePricing(eventId);

        assertEquals(1, result.size());
        assertEquals(catId, result.get(0).getCategoryId());
        // Surge calculation: occupancy = (100-10)/100 = 0.9. Rate > 0.7 -> 1.8
        // multiplier.
        // multiplier = 1.0 (base) + 1.8 (surge) = 2.8? Wait, calculateSurge logic:
        // multiplier = 1.8; return basePrice.multiply(BigDecimal.valueOf(1 +
        // multiplier));
        // So 100000 * 2.8 = 280000.
    }
}
