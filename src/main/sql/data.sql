CREATE TABLE subscription
(
    id   SERIAL PRIMARY KEY,
    link VARCHAR(255) NOT NULL
);
CREATE TABLE price_history
(
    subscription_id INT REFERENCES subscription (id) ON DELETE CASCADE,
    price           DOUBLE PRECISION NOT NULL,
    date            DATE             NOT NULL
);

INSERT INTO subscription(link)
VALUES ('https://www.amazon.com/Apple-iPhone-14-Pro-128GB/dp/B0BN95FRW9');
INSERT INTO subscription(link)
VALUES ('https://www.amazon.de/DJI-CP-PT-000498-Mavic-Drohne-grau/dp/B01M0AVO1P');

INSERT INTO price_history
VALUES (1, 30.5, '2023-05-18');
INSERT INTO price_history
VALUES (1, 20, '2023-04-18');
INSERT INTO price_history
VALUES (1, 10.99, '2023-01-18');

INSERT INTO price_history
VALUES (2, 40, '2023-05-18');
INSERT INTO price_history
VALUES (2, 30, '2023-04-18');
INSERT INTO price_history
VALUES (2, 99.99, '2023-01-18');