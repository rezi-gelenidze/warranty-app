-- Demo data. Dates are relative to today so the "Ending soon" list is never empty.

INSERT INTO store (name) VALUES ('Alta'), ('Zoommer'), ('IKEA'), ('Elit Electronics');

INSERT INTO tag (name) VALUES ('Electronics'), ('Home'), ('Kitchen'), ('Gift');

INSERT INTO purchase (name, price, purchase_date, warranty_months, warranty_until, notes, store_id)
VALUES
    ('Bosch dishwasher',  1250.00, current_date - INTERVAL '24 months' + INTERVAL '12 days', 24, current_date, 'Installed by the store. Keep the installation slip.', (SELECT id FROM store WHERE name = 'Alta')),
    ('Logitech MX Keys',   329.00, current_date - INTERVAL '24 months' + INTERVAL '19 days', 24, current_date, NULL, (SELECT id FROM store WHERE name = 'Zoommer')),
    ('Philips air fryer',  289.00, current_date - INTERVAL '24 months' + INTERVAL '27 days', 24, current_date, NULL, (SELECT id FROM store WHERE name = 'Alta')),
    ('MacBook Air 13',    3499.00, current_date - INTERVAL '2 months',  24, current_date, 'AppleCare not bought.', (SELECT id FROM store WHERE name = 'Zoommer')),
    ('MALM desk',          449.00, current_date - INTERVAL '5 months', 120, current_date, NULL, (SELECT id FROM store WHERE name = 'IKEA')),
    ('Sony WH-1000XM5',    899.00, current_date - INTERVAL '8 months',  12, current_date, NULL, (SELECT id FROM store WHERE name = 'Elit Electronics')),
    ('Dyson V12',         1690.00, current_date - INTERVAL '39 months', 24, current_date, NULL, (SELECT id FROM store WHERE name = 'Alta')),
    ('Electric kettle',     59.00, current_date - INTERVAL '10 months',  0, current_date, NULL, (SELECT id FROM store WHERE name = 'IKEA')),
    ('Samsung 55" TV',    2199.00, current_date - INTERVAL '14 months', 24, current_date, 'Gift for parents.', (SELECT id FROM store WHERE name = 'Elit Electronics')),
    ('Nespresso Vertuo',   399.00, current_date - INTERVAL '6 months',  24, current_date, NULL, (SELECT id FROM store WHERE name = 'Zoommer')),
    ('KALLAX shelf',       249.00, current_date - INTERVAL '30 months', 0, current_date, NULL, (SELECT id FROM store WHERE name = 'IKEA')),
    ('iPhone 17',         3299.00, current_date - INTERVAL '1 month',   12, current_date, NULL, (SELECT id FROM store WHERE name = 'Zoommer'));

UPDATE purchase SET warranty_until = (purchase_date + make_interval(months => warranty_months))::date;

INSERT INTO purchase_tag (purchase_id, tag_id)
SELECT p.id, t.id
FROM purchase p
JOIN tag t ON (p.name, t.name) IN (
    ('Bosch dishwasher', 'Home'), ('Bosch dishwasher', 'Kitchen'),
    ('Logitech MX Keys', 'Electronics'),
    ('Philips air fryer', 'Kitchen'),
    ('MacBook Air 13', 'Electronics'),
    ('MALM desk', 'Home'),
    ('Sony WH-1000XM5', 'Electronics'),
    ('Dyson V12', 'Home'),
    ('Electric kettle', 'Kitchen'),
    ('Samsung 55" TV', 'Electronics'), ('Samsung 55" TV', 'Gift'),
    ('Nespresso Vertuo', 'Kitchen'),
    ('KALLAX shelf', 'Home'),
    ('iPhone 17', 'Electronics')
);
