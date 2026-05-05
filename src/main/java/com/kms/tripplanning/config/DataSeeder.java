package com.kms.tripplanning.config;


import com.kms.tripplanning.entity.*;
import com.kms.tripplanning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final DestinationRepository destinationRepository;
    private final TripRepository tripRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (destinationRepository.count() > 0) {
            return; // prevent duplicate seeding
        }

        // 1. Categories
        List<Category> categories;
        if (categoryRepository.count() > 0) {
            categories = new ArrayList<>(categoryRepository.findAll());
        } else {
            List<String> categoryNames = List.of(
                    "Beach", "Mountain", "City", "Historical",
                    "Nature", "Adventure", "Food", "Cultural"
            );

            categories = categoryNames.stream().map(name -> {
                Category c = new Category();
                c.setName(name);
                return c;
            }).toList();

            categories = new ArrayList<>(categoryRepository.saveAll(categories));
        }

        // 2. Destinations (100+)
        List<Destination> destinations = new ArrayList<>();

        for (int i = 1; i <= 120; i++) {
            Destination d = new Destination();
            d.setName("Destination " + i);
            d.setCity("City " + (i % 20));
            d.setCountry("Vietnam");
            d.setRating((float) (3 + random.nextDouble() * 2));
            d.setLatitude(8 + random.nextDouble() * 15);
            d.setLongtitude(102 + random.nextDouble() * 10);
            d.setThumbnailUrl("https://example.com/img" + i + ".jpg");

            // assign 1–3 categories
            Collections.shuffle(categories);
            int count = 1 + random.nextInt(3);
            d.setCategories(new ArrayList<>(categories.subList(0, count)));

            destinations.add(d);
        }

        destinations = new ArrayList<>(destinationRepository.saveAll(destinations));

        // 3. Trips (30)
        List<Trip> trips = new ArrayList<>();

        for (int i = 1; i <= 30; i++) {
            Trip t = new Trip();
            Date date = new Date();
            OffsetDateTime odt = date.toInstant()
                .atOffset(ZoneOffset.UTC);
            t.setName("Trip " + i);
            t.setCreatedAt(odt);
            t.setUpdatedAt(odt);

            t.setStartDate(new Date());
            t.setEndDate(new Date(System.currentTimeMillis() + (i + 5) * 24 * 60 * 60 * 1000));

            // assign 3–6 destinations
            Collections.shuffle(destinations);
            int count = 3 + random.nextInt(4);

            t.setDestinations(new ArrayList<>(destinations.subList(0, count)));

            trips.add(t);
        }

        tripRepository.saveAll(trips);

        System.out.println("✅ Seeded database with test data");
    }
}
