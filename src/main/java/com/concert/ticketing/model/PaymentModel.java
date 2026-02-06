package com.concert.ticketing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class PaymentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    @OneToOne
    @JoinColumn(name = "booking_id")
    private BookingsModel booking;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String gatewayTransactionId;
    private LocalDateTime paidAt;

}
