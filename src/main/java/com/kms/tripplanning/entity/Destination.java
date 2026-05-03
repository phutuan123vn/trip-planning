package com.kms.tripplanning.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "destinations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Destination {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String city;

    private String country;

    private float rating;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "destination_categories",
        joinColumns = @JoinColumn(name = "destination_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    private double latitude;

    private double longtitude;


    @Column(name = "thumbnail_url")
    @URL(message = "Invalid URL format")
    private String thumbnailUrl;

    
}
