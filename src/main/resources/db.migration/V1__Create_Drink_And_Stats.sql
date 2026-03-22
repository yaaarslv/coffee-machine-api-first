CREATE TABLE drink
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    water_amount  INT          NOT NULL,
    coffee_amount INT          NOT NULL,
    milk_amount   INT          NOT NULL
);

CREATE TABLE drink_statistics
(
    id         BIGSERIAL PRIMARY KEY,
    drink_id   BIGINT REFERENCES drink (id),
    order_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE machine_inventory
(
    id     BIGSERIAL PRIMARY KEY,
    water  BIGINT NOT NULL,
    coffee BIGINT NOT NULL,
    milk   BIGINT NOT NULL
);
