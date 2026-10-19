CREATE TABLE IF NOT EXISTS lab_trade_brand(id BIGINT PRIMARY KEY,brand_name VARCHAR(60) NOT NULL UNIQUE,website VARCHAR(160));
CREATE TABLE IF NOT EXISTS lab_trade_product (
 id BIGINT PRIMARY KEY, product_name VARCHAR(100) NOT NULL, category VARCHAR(40) NOT NULL,
 brand VARCHAR(60) NOT NULL, price DECIMAL(10,2) NOT NULL, stock INT NOT NULL, sale_status VARCHAR(20) NOT NULL,
 brand_id BIGINT, version INT NOT NULL DEFAULT 0, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_trade_product_brand FOREIGN KEY(brand_id) REFERENCES lab_trade_brand(id));
CREATE TABLE IF NOT EXISTS lab_trade_customer (
 id BIGINT PRIMARY KEY, customer_name VARCHAR(60) NOT NULL, city VARCHAR(40) NOT NULL);
CREATE TABLE IF NOT EXISTS lab_trade_order (
 id BIGINT PRIMARY KEY, order_no VARCHAR(40) NOT NULL UNIQUE, customer_id BIGINT NOT NULL,
 ordered_on DATE NOT NULL, order_status VARCHAR(20) NOT NULL,
 FOREIGN KEY(customer_id) REFERENCES lab_trade_customer(id));
CREATE TABLE IF NOT EXISTS lab_trade_order_line (
 id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
 quantity INT NOT NULL, deal_price DECIMAL(10,2) NOT NULL,
 FOREIGN KEY(order_id) REFERENCES lab_trade_order(id), FOREIGN KEY(product_id) REFERENCES lab_trade_product(id));

CREATE TABLE IF NOT EXISTS lab_trade_customer_profile (
 customer_id BIGINT PRIMARY KEY, member_no VARCHAR(30) NOT NULL UNIQUE, member_level VARCHAR(30) NOT NULL,
 registered_on DATE NOT NULL, FOREIGN KEY(customer_id) REFERENCES lab_trade_customer(id));
CREATE TABLE IF NOT EXISTS lab_trade_supplier (
 id BIGINT PRIMARY KEY, supplier_name VARCHAR(100) NOT NULL, city VARCHAR(40) NOT NULL);
CREATE TABLE IF NOT EXISTS lab_trade_supply (
 supplier_id BIGINT NOT NULL, product_id BIGINT NOT NULL, supply_price DECIMAL(10,2) NOT NULL,
 PRIMARY KEY(supplier_id,product_id), FOREIGN KEY(supplier_id) REFERENCES lab_trade_supplier(id),
 FOREIGN KEY(product_id) REFERENCES lab_trade_product(id));
