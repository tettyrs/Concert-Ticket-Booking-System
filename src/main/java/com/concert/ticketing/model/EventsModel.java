package com.concert.ticketing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class EventsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    private String artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", referencedColumnName = "id")
    private VenuesModel venue;

    @Column(name = "name")
    private String name;

    @Column(name = "event_date")
    private ZonedDateTime eventDate;

    @Column(name = "timezone")
    private String timezone;
    @Column(name = "status")
    private String status;
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<TicketCategoryModel> categories;
}
