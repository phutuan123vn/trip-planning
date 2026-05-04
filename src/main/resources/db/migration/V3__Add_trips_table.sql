CREATE TABLE trip_destinations
(
    destination_id UUID NOT NULL,
    trip_id        UUID NOT NULL
);

CREATE TABLE trips
(
    id         UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    name       VARCHAR(255),
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_trips PRIMARY KEY (id)
);

ALTER TABLE trip_destinations
    ADD CONSTRAINT fk_trides_on_destination FOREIGN KEY (destination_id) REFERENCES destinations (id);

ALTER TABLE trip_destinations
    ADD CONSTRAINT fk_trides_on_trip FOREIGN KEY (trip_id) REFERENCES trips (id);