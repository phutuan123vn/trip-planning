package com.kms.tripplanning.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import com.kms.tripplanning.utils.AuditMixin;

@Entity
@Table(name = "trips")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trip extends AuditMixin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @ManyToMany
    @JoinTable(
            name = "trip_destinations",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "destination_id")
    )
    Set<Destination> destinations;


}
