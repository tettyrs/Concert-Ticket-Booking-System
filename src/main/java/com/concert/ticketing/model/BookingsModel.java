package com.concert.ticketing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class BookingsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_category_id")
    private TicketCategoryModel category;

    @Column(name = "ticket_category_id", insertable = false, updatable = false)
    private UUID categoryId;

    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;
    @Column(unique = true)
    private String idempotencyKey;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
