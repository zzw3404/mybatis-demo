-- Chapter 05: relationship mapping and supplier/product/customer schema.
-- Safe to rerun: creates missing tables and inserts only missing seed rows.
USE mybatis_db;

-- Required by the supplied chapter05 employee-skill many-to-many examples.
CREATE TABLE IF NOT EXISTS skill (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_skill_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS emp_skill (
    empno INT NOT NULL,
    skill_id INT NOT NULL,
    PRIMARY KEY (empno, skill_id),
    CONSTRAINT fk_emp_skill_emp FOREIGN KEY (empno) REFERENCES emp (empno),
    CONSTRAINT fk_emp_skill_skill FOREIGN KEY (skill_id) REFERENCES skill (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO skill (id, name, description) VALUES
(1, 'SQL', 'Relational database querying and design'),
(2, 'Java', 'Java application development'),
(3, 'MyBatis', 'Persistence mapping with MyBatis'),
(4, 'Communication', 'Team communication and collaboration');

INSERT IGNORE INTO emp_skill (empno, skill_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 4),
(4, 1), (4, 2), (4, 3),
(8, 1), (8, 2), (8, 3),
(9, 2), (9, 4),
(13, 1), (13, 2), (13, 3);

-- Screenshot domain: suppliers provide products; customers buy sourced products;
-- service records track after-sale service for a customer's purchased product.
CREATE TABLE IF NOT EXISTS supplier (
    supplier_id INT NOT NULL AUTO_INCREMENT,
    supplier_name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(80),
    phone VARCHAR(30),
    address VARCHAR(255),
    PRIMARY KEY (supplier_id),
    UNIQUE KEY uk_supplier_name (supplier_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product (
    product_id INT NOT NULL AUTO_INCREMENT,
    product_name VARCHAR(120) NOT NULL,
    category VARCHAR(80),
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (product_id),
    UNIQUE KEY uk_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS supplier_product (
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    supply_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (supplier_id, product_id),
    CONSTRAINT fk_supplier_product_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (supplier_id),
    CONSTRAINT fk_supplier_product_product FOREIGN KEY (product_id) REFERENCES product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customer (
    customer_id INT NOT NULL AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    address VARCHAR(255),
    PRIMARY KEY (customer_id),
    KEY idx_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS purchase (
    purchase_id INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    purchased_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (purchase_id),
    KEY idx_purchase_customer (customer_id),
    KEY idx_purchase_supplier_product (supplier_id, product_id),
    CONSTRAINT fk_purchase_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_purchase_supplier_product FOREIGN KEY (supplier_id, product_id)
        REFERENCES supplier_product (supplier_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_service (
    service_id INT NOT NULL AUTO_INCREMENT,
    purchase_id INT NOT NULL,
    service_type VARCHAR(80) NOT NULL,
    service_note VARCHAR(500),
    serviced_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (service_id),
    KEY idx_product_service_purchase (purchase_id),
    CONSTRAINT fk_product_service_purchase FOREIGN KEY (purchase_id) REFERENCES purchase (purchase_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO supplier (supplier_id, supplier_name, contact_name, phone, address) VALUES
(1, '华东电子供应商', '王经理', '13800000001', '上海市'),
(2, '北方数码供应商', '李经理', '13800000002', '北京市'),
(3, '南方办公用品供应商', '陈经理', '13800000003', '广州市');

INSERT IGNORE INTO product (product_id, product_name, category, unit_price) VALUES
(1, '商务笔记本电脑', '电脑设备', 5999.00),
(2, '无线鼠标', '电脑配件', 129.00),
(3, '机械键盘', '电脑配件', 399.00),
(4, '激光打印机', '办公设备', 1899.00);

INSERT IGNORE INTO supplier_product (supplier_id, product_id, supply_price) VALUES
(1, 1, 5200.00), (2, 1, 5150.00),
(1, 2, 85.00), (2, 2, 82.00), (3, 2, 88.00),
(1, 3, 310.00), (3, 3, 320.00),
(2, 4, 1600.00), (3, 4, 1580.00);

INSERT IGNORE INTO customer (customer_id, customer_name, phone, address) VALUES
(1, '星河科技有限公司', '13900000001', '上海市浦东新区'),
(2, '远航设计工作室', '13900000002', '北京市朝阳区'),
(3, '明日教育中心', '13900000003', '广州市天河区');

INSERT IGNORE INTO purchase (purchase_id, customer_id, supplier_id, product_id, quantity, unit_price, purchased_at) VALUES
(1, 1, 1, 1, 2, 5799.00, '2026-09-01 10:00:00'),
(2, 1, 2, 2, 5, 119.00, '2026-09-01 10:05:00'),
(3, 2, 3, 3, 2, 369.00, '2026-09-02 11:30:00'),
(4, 3, 3, 4, 1, 1799.00, '2026-09-03 09:15:00'),
(5, 2, 1, 2, 3, 119.00, '2026-09-04 14:20:00');

INSERT IGNORE INTO product_service (service_id, purchase_id, service_type, service_note, serviced_at) VALUES
(1, 1, '安装调试', '完成电脑初始化和系统设置', '2026-09-02 13:00:00'),
(2, 4, '维修检测', '完成打印机进纸检测', '2026-09-05 16:30:00');
