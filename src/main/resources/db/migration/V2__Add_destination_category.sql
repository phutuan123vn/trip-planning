CREATE TABLE categories
(
    id   UUID NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE destination_categories
(
    category_id    UUID NOT NULL,
    destination_id UUID NOT NULL
);

CREATE TABLE destinations
(
    id            UUID             NOT NULL,
    name          VARCHAR(255),
    city          VARCHAR(255),
    country       VARCHAR(255),
    rating        FLOAT            NOT NULL,
    latitude      DOUBLE PRECISION NOT NULL,
    longtitude    DOUBLE PRECISION NOT NULL,
    thumbnail_url VARCHAR(255),
    CONSTRAINT pk_destinations PRIMARY KEY (id)
);

ALTER TABLE categories
    ADD CONSTRAINT uc_categories_name UNIQUE (name);

ALTER TABLE destination_categories
    ADD CONSTRAINT fk_descat_on_category FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE destination_categories
    ADD CONSTRAINT fk_descat_on_destination FOREIGN KEY (destination_id) REFERENCES destinations (id);

ALTER TABLE users
DROP
COLUMN id;

ALTER TABLE users
    ADD id UUID NOT NULL PRIMARY KEY;