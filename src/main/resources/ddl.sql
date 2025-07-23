DROP TABLE IF EXISTS Payments;
DROP TABLE IF EXISTS OrderItems;
DROP TABLE IF EXISTS Cart;
DROP TABLE IF EXISTS UserItems;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS MenuItems;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Categories;

CREATE TABLE Users (
    user_id VARCHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK(role IN ('USER', 'ADMIN')),
    email VARCHAR(100) UNIQUE,
    created_at VARCHAR(26) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Categories (
    category_id VARCHAR(36) NOT NULL PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    image_path VARCHAR(255)
);

CREATE TABLE MenuItems (
    item_id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    price REAL NOT NULL,
    category_id VARCHAR(36),
    is_available INTEGER DEFAULT 1,
    image_path VARCHAR(255),
    default_portion_size VARCHAR(20) DEFAULT 'MEDIUM' CHECK(default_portion_size IN ('SMALL', 'MEDIUM', 'LARGE', 'EXTRA_LARGE')),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

CREATE TABLE Orders (
    order_id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    total_price REAL NOT NULL,
    status VARCHAR(20) NOT NULL CHECK(status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED')),
    created_at VARCHAR(26) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE Cart (
    cart_id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    item_id VARCHAR(36) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal REAL NOT NULL,
    is_ordered INTEGER DEFAULT 0 NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (item_id) REFERENCES MenuItems(item_id)
);

CREATE TABLE Payments (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL UNIQUE,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING' CHECK(payment_status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at VARCHAR(26) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES Cart(cart_id)
);