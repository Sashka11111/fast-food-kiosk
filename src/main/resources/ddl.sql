-- Видаляємо таблиці в правильному порядку (спочатку залежні, потім основні)
DROP TABLE IF EXISTS Payments;
DROP TABLE IF EXISTS OrderItems;
DROP TABLE IF EXISTS Cart;
DROP TABLE IF EXISTS UserItems;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS MenuItems;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Categories;

CREATE TABLE Users (
    user_id UUID NOT NULL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER' NOT NULL CHECK(Users.role IN ('USER', 'ADMIN')),
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Categories (
    category_id UUID NOT NULL PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    image_path VARCHAR(255)
);

CREATE TABLE MenuItems (
    item_id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    category_id UUID,
    is_available BOOLEAN DEFAULT TRUE,
    image_path VARCHAR(255),
    default_portion_size VARCHAR(20) DEFAULT 'MEDIUM' CHECK(MenuItems.default_portion_size IN ('SMALL', 'MEDIUM', 'LARGE', 'EXTRA_LARGE')),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE CASCADE
);

CREATE TABLE Orders (
    order_id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (Orders.status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE Cart (
    cart_id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    item_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal REAL NOT NULL,
    is_ordered BOOLEAN DEFAULT FALSE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES MenuItems(item_id) ON DELETE CASCADE
);

-- Таблиця UserItems для нової системи кошика
CREATE TABLE UserItems (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    menu_item_id UUID NOT NULL REFERENCES MenuItems(item_id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (UserItems.quantity > 0),
    portion_size VARCHAR(20) NOT NULL CHECK(UserItems.portion_size IN ('SMALL', 'MEDIUM', 'LARGE', 'EXTRA_LARGE')),
    price_per_item DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Індекси для оптимізації запитів UserItems
CREATE INDEX idx_user_items_user_id ON UserItems(user_id);
CREATE INDEX idx_user_items_menu_item_id ON UserItems(menu_item_id);
CREATE INDEX idx_user_items_created_at ON UserItems(created_at);

-- Унікальний індекс для запобігання дублікатів одного товару з однаковим розміром порції у кошику
CREATE UNIQUE INDEX idx_user_items_unique ON UserItems(user_id, menu_item_id, portion_size);

CREATE TABLE Payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE REFERENCES Orders(order_id),
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING' CHECK (Payments.payment_status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
