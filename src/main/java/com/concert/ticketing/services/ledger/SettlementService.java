package com.concert.ticketing.services.ledger;

import com.concert.ticketing.model.LedgerEntriesModel;
import com.concert.ticketing.repositories.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final LedgerRepository ledgerRepository;

    public Map<String, Object> getSettlementReport(UUID concertId) {
        List<LedgerEntriesModel> entries = ledgerRepository.findByConcertId(concertId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalRefunds = BigDecimal.ZERO;
        int debitCount = 0;
        int creditCount = 0;

        for (LedgerEntriesModel entry : entries) {
            if ("DEBIT".equals(entry.getType())) {
                totalRevenue = totalRevenue.add(entry.getAmount());
                debitCount++;
            } else if ("CREDIT".equals(entry.getType())) {
                totalRefunds = totalRefunds.add(entry.getAmount());
                creditCount++;
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("concertId", concertId);
        report.put("totalRevenue", totalRevenue);
        report.put("totalRefunds", totalRefunds);
        report.put("netRevenue", totalRevenue.subtract(totalRefunds));
        report.put("transactionCount", entries.size());
        report.put("bookingCount", debitCount);
        report.put("refundCount", creditCount);
        report.put("transactions", entries.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("bookingId", e.getBookingId());
            m.put("amount", e.getAmount());
            m.put("type", e.getType());
            m.put("recordedAt", e.getRecordedAt());
            return m;
        }).toList());

        return report;
    }

    public List<LedgerEntriesModel> getAllTransactions() {
        return ledgerRepository.findAll();
    }
}
