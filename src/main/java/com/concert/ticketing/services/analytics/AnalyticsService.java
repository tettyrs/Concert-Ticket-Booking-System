package com.concert.ticketing.services.analytics;

import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.repositories.BookingRepository;
import com.concert.ticketing.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public Map<String, Object> getDashboardStats() {
        long totalBookings = bookingRepository.count();

        List<Map<String, Object>> concertStats = eventRepository.findAllWithCategories().stream().map(event -> {
            int totalAllocation = event.getCategories().stream()
                    .mapToInt(TicketCategoryModel::getTotalAllocation).sum();
            int currentStock = event.getCategories().stream()
                    .mapToInt(TicketCategoryModel::getAvailableStock).sum();

            int sold = totalAllocation - currentStock;
            double occupancy = totalAllocation > 0 ? (double) sold / totalAllocation * 100 : 0;

            Map<String, Object> stats = new HashMap<>();
            stats.put("concertName", event.getName());
            stats.put("soldTickets", sold);
            stats.put("occupancyRate", String.format("%.2f%%", occupancy));
            return stats;
        }).collect(Collectors.toList());

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalBookings", totalBookings);
        dashboard.put("concertAnalytics", concertStats);

        return dashboard;
    }
}
