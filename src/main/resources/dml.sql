INSERT INTO Users (user_id, username, password, role, email)
VALUES
    ('c8a3e883-5049-46c7-94b1-9f1cdb9a4f95', 'Admin', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'ADMIN', 'admin@gmail.com'),
    ('6ba7b810-9dad-11d1-80b4-00c04fd430c1', 'Sashka', '32c6b1625a1aae8ba1cbdb24c20b6c24ed42ab7389c54c6a5a53d59fff0f2b59', 'USER', 'sashka@gmail.com'),
    ('6ba7b811-9dad-11d1-80b4-00c04fd430c2', 'user2', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'USER', 'ivan.p@email.com'),
    ('6ba7b812-9dad-11d1-80b4-00c04fd430c3', 'user3', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'USER', 'mariya.s@email.com'),
    ('6ba7b813-9dad-11d1-80b4-00c04fd430c4', 'user4', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'USER', 'andriy.b@email.com'),
    ('6ba7b814-9dad-11d1-80b4-00c04fd430c5', 'admin2', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'ADMIN', 'oleg.k@email.com'),
    ('6ba7b815-9dad-11d1-80b4-00c04fd430c6', 'user5', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'USER', 'yuliya.m@email.com'),
    ('6ba7b816-9dad-11d1-80b4-00c04fd430c7', 'user6', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'USER', 'dmytro.sh@email.com'),
    ('6ba7b817-9dad-11d1-80b4-00c04fd430c8', 'user7', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'USER', 'nataliya.l@email.com'),
    ('6ba7b818-9dad-11d1-80b4-00c04fd430c9', 'user8', '3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2', 'USER', 'viktor.g@email.com');

INSERT INTO Categories (category_id, category_name, image_path)
VALUES
    ('7c4f14d0-9dad-11d1-80b4-00c04fd430d0', 'Бургери', '/images/categories/burgers.png'),
    ('7c4f14d1-9dad-11d1-80b4-00c04fd430d1', 'Піца', '/images/categories/pizza.png'),
    ('7c4f14d2-9dad-11d1-80b4-00c04fd430d2', 'Салати', '/images/categories/salads.png'),
    ('7c4f14d3-9dad-11d1-80b4-00c04fd430d3', 'Напої', '/images/categories/drinks.png'),
    ('7c4f14d4-9dad-11d1-80b4-00c04fd430d4', 'Десерти', '/images/categories/desserts.png');

INSERT INTO MenuItems (item_id, name, description, price, category_id, is_available, image_path, default_portion_size)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'Бургер Класичний', 'Бургер з яловичиною і овочами', 140.50, '7c4f14d0-9dad-11d1-80b4-00c04fd430d0', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Чізбургер', 'Бургер з яловичиною і сиром', 150.00, '7c4f14d0-9dad-11d1-80b4-00c04fd430d0', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440003', 'Маргарита', 'Класична піца з томатним соусом і моцарелою', 150.50, '7c4f14d1-9dad-11d1-80b4-00c04fd430d1', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440004', 'Пепероні', 'Піца з пепероні та сиром', 180.75, '7c4f14d1-9dad-11d1-80b4-00c04fd430d1', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440005', 'Цезар', 'Салат з куркою, пармезаном і сухариками', 120.00, '7c4f14d2-9dad-11d1-80b4-00c04fd430d2', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440006', 'Грецький салат', 'Салат з огірками, фетою і оливками', 110.25, '7c4f14d2-9dad-11d1-80b4-00c04fd430d2', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440007', 'Кола', 'Газований напій', 40.00, '7c4f14d3-9dad-11d1-80b4-00c04fd430d3', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440008', 'Сік апельсиновий', 'Свіжий апельсиновий сік', 50.00, '7c4f14d3-9dad-11d1-80b4-00c04fd430d3', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440009', 'Чізкейк', 'Класичний чізкейк з ягідним соусом', 100.00, '7c4f14d4-9dad-11d1-80b4-00c04fd430d4', TRUE, NULL, 'MEDIUM'),
    ('550e8400-e29b-41d4-a716-446655440010', 'Тірамісу', 'Італійський десерт з маскарпоне', 105.50, '7c4f14d4-9dad-11d1-80b4-00c04fd430d4', TRUE, NULL, 'SMALL');

INSERT INTO Orders (order_id, user_id, total_price, status, created_at)
VALUES
    ('9e6f0680-9dad-11d1-80b4-00c04fd430f0', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', 281.00, 'PENDING', '2025-04-30 12:30:00+00'),
    ('9e6f0681-9dad-11d1-80b4-00c04fd430f1', '6ba7b811-9dad-11d1-80b4-00c04fd430c2', 150.50, 'CONFIRMED', '2025-04-30 13:00:00+00'),
    ('9e6f0682-9dad-11d1-80b4-00c04fd430f2', '6ba7b812-9dad-11d1-80b4-00c04fd430c3', 300.00, 'PENDING', '2025-04-30 13:30:00+00'),
    ('9e6f0683-9dad-11d1-80b4-00c04fd430f3', '6ba7b813-9dad-11d1-80b4-00c04fd430c4', 110.25, 'DELIVERED', '2025-04-30 14:00:00+00');

INSERT INTO Cart (cart_id, user_id, item_id, quantity, subtotal, is_ordered)
VALUES
    ('1c4f14d0-aaaa-11d1-80b4-00c04fd43000', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '550e8400-e29b-41d4-a716-446655440001', 2, 281.00, TRUE),
    ('1c4f14d1-aaaa-11d1-80b4-00c04fd43001', '6ba7b811-9dad-11d1-80b4-00c04fd430c2', '550e8400-e29b-41d4-a716-446655440003', 1, 150.50, TRUE),
    ('1c4f14d2-aaaa-11d1-80b4-00c04fd43002', '6ba7b812-9dad-11d1-80b4-00c04fd430c3', '550e8400-e29b-41d4-a716-446655440009', 3, 300.00, TRUE),
    ('1c4f14d3-aaaa-11d1-80b4-00c04fd43003', '6ba7b813-9dad-11d1-80b4-00c04fd430c4', '550e8400-e29b-41d4-a716-446655440006', 1, 110.25, TRUE);

INSERT INTO Payments (id, cart_id, payment_method, payment_status, created_at)
VALUES
    ('cf7e1790-9dad-11d1-80b4-00c04fd430c0', '1c4f14d0-aaaa-11d1-80b4-00c04fd43000', 'CASH', 'PENDING', '2025-04-30 12:30:00+00'),
    ('cf7e1791-9dad-11d1-80b4-00c04fd430c1', '1c4f14d1-aaaa-11d1-80b4-00c04fd43001', 'CARD', 'COMPLETED', '2025-04-30 13:00:00+00'),
    ('cf7e1792-9dad-11d1-80b4-00c04fd430c2', '1c4f14d2-aaaa-11d1-80b4-00c04fd43002', 'CASH', 'PENDING', '2025-04-30 13:30:00+00'),
    ('cf7e1793-9dad-11d1-80b4-00c04fd430c3', '1c4f14d3-aaaa-11d1-80b4-00c04fd43003', 'CARD', 'COMPLETED', '2025-04-30 14:00:00+00');