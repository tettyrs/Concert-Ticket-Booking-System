package com.concert.ticketing.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ticket_categories")
@Getter
@Setter
@Data
@NoArgsConstructor
public class TicketCategoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventsModel event;

    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "total_allocation")
    private Integer totalAllocation;
    @Column(name = "available_stock")
    private Integer availableStock;
    @Version
    @Column(name = "version")
    private Integer version;

}
