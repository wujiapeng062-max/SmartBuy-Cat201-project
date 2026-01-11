/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : smartbuy_db

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 09/01/2026 16:15:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `cart_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NULL DEFAULT 1,
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cart_id`) USING BTREE,
  UNIQUE INDEX `unique_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart
-- ----------------------------

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`) USING BTREE,
  UNIQUE INDEX `category_name`(`category_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, 'Smartphones', 'Smartphones and accessories', '2026-01-06 18:52:05');
INSERT INTO `categories` VALUES (2, 'Laptops', 'Laptop', '2026-01-06 18:52:05');
INSERT INTO `categories` VALUES (3, 'Audio', 'Headphones, speakers, and other audio equipment', '2026-01-06 18:52:05');
INSERT INTO `categories` VALUES (4, 'Accessories', 'Charger, data cable, and other accessories', '2026-01-06 18:52:05');

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `order_item_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(10, 2) NOT NULL,
  `subtotal` decimal(10, 2) NOT NULL,
  PRIMARY KEY (`order_item_id`) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_order`(`order_id` ASC) USING BTREE,
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_items
-- ----------------------------

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `order_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `total_amount` decimal(10, 2) NOT NULL,
  `status` enum('Pending','Paid','Shipped','Completed','Cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'Pending',
  `shipping_address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_order_date`(`order_date` ASC) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `category_id` int NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `stock` int NULL DEFAULT 0,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `specs` json NULL COMMENT '商品规格参数（JSON格式）',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_available` tinyint(1) NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_brand`(`brand` ASC) USING BTREE,
  INDEX `idx_price`(`price` ASC) USING BTREE,
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (1, 'iPhone 15 Pro', 'Apple', 1, 999.99, 50, 'Apple latest flagship smartphone with A17 Pro chip', '{\"RAM\": \"8GB\", \"Camera\": \"48MP Main Camera\", \"Battery\": \"3274mAh\", \"Storage\": \"256GB\"}', 'images/iphone15pro.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (2, 'Samsung Galaxy S24', 'Samsung', 1, 899.99, 45, 'Samsung flagship smartphone with Snapdragon 8 Gen 3', '{\"RAM\": \"12GB\", \"Camera\": \"50MP Main Camera\", \"Battery\": \"4000mAh\", \"Storage\": \"256GB\"}', 'images/galaxys24.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (3, 'Xiaomi 14', 'Xiaomi', 1, 699.99, 60, 'High-performance flagship smartphone from Xiaomi', '{\"RAM\": \"12GB\", \"Camera\": \"50MP Leica Lens\", \"Battery\": \"4610mAh\", \"Storage\": \"256GB\"}', 'images/xiaomi14.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (4, 'MacBook Pro 14-inch', 'Apple', 2, 1999.99, 30, 'Professional laptop with Apple M3 Pro chip', '{\"CPU\": \"M3 Pro\", \"GPU\": \"14-core GPU\", \"RAM\": \"16GB\", \"SSD\": \"512GB\", \"Screen Size\": \"14.2 inches\"}', 'images/macbookpro14.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (5, 'Dell XPS 15', 'Dell', 2, 1599.99, 25, 'High-end business laptop from Dell', '{\"CPU\": \"Intel i7-13700H\", \"GPU\": \"RTX 4050\", \"RAM\": \"16GB\", \"SSD\": \"512GB\", \"Screen Size\": \"15.6 inches\"}', 'images/dellxps15.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (6, 'ThinkPad X1 Carbon', 'Lenovo', 2, 1399.99, 35, 'Premium ultralight business laptop from Lenovo', '{\"CPU\": \"Intel i7-1365U\", \"GPU\": \"Integrated Graphics\", \"RAM\": \"16GB\", \"SSD\": \"512GB\", \"Screen Size\": \"14 inches\"}', 'images/thinkpadx1.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (7, 'Sony WH-1000XM5', 'Sony', 3, 399.99, 40, 'Industry-leading noise canceling headphones', '{\"Type\": \"Over-Ear\", \"Driver\": \"30mm\", \"Battery Life\": \"30 hours\", \"Connectivity\": \"Bluetooth 5.2\"}', 'images/sonywh1000xm5.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (8, 'AirPods Pro 2nd Gen', 'Apple', 3, 249.99, 80, 'Apple premium wireless earbuds with active noise cancellation', '{\"Type\": \"True Wireless\", \"Driver\": \"Custom\", \"Battery Life\": \"6 hours\", \"Connectivity\": \"Bluetooth 5.3\"}', 'images/airpodspro2.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (9, 'Bose QuietComfort Ultra', 'Bose', 3, 429.99, 35, 'Premium noise cancelling headphones with immersive audio', '{\"Type\": \"Over-Ear\", \"Driver\": \"40mm\", \"Battery Life\": \"24 hours\", \"Connectivity\": \"Bluetooth 5.1\"}', 'images/boseqc.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (10, 'Anker PowerCore 26800', 'Anker', 4, 79.99, 100, 'High-capacity portable power bank with fast charging', '{\"Ports\": \"3 USB-A\", \"Output\": \"18W Max\", \"Capacity\": \"26800mAh\", \"Technology\": \"PowerIQ\"}', 'images/ankerpowercore.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (11, 'Belkin Lightning Cable', 'Belkin', 4, 19.99, 200, 'MFi-certified Lightning cable for iPhone and iPad', '{\"Length\": \"1 meter\", \"Material\": \"Nylon Braided\", \"Connector\": \"Lightning to USB-A\", \"Certification\": \"MFi\"}', 'images/belkinlightning.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `products` VALUES (12, 'Logitech MX Master 3S', 'Logitech', 4, 99.99, 75, 'Advanced wireless mouse for professionals', '{\"Sensor\": \"4000 DPI\", \"Buttons\": \"8\", \"Battery Life\": \"70 days\", \"Connectivity\": \"Bluetooth + USB Receiver\"}', 'images/logimxmaster.jpg', 1, '2026-01-06 18:52:05', '2026-01-06 18:52:05');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `role` enum('customer','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'customer',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', 'admin123', 'admin@smartbuy.com', 'System Admin', NULL, NULL, 'admin', '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `users` VALUES (2, 'john_doe', 'password123', 'john@example.com', 'John Doe', NULL, NULL, 'customer', '2026-01-06 18:52:05', '2026-01-06 18:52:05');
INSERT INTO `users` VALUES (3, 'jane_smith', 'password123', 'jane@example.com', 'Jane Smith', NULL, NULL, 'customer', '2026-01-06 18:52:05', '2026-01-06 18:52:05');

-- ----------------------------
-- View structure for sales_summary
-- ----------------------------
DROP VIEW IF EXISTS `sales_summary`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `sales_summary` AS select cast(`o`.`order_date` as date) AS `sale_date`,`c`.`category_name` AS `category_name`,count(distinct `o`.`order_id`) AS `order_count`,sum(`oi`.`quantity`) AS `total_quantity`,sum(`oi`.`subtotal`) AS `total_revenue` from (((`orders` `o` join `order_items` `oi` on((`o`.`order_id` = `oi`.`order_id`))) join `products` `p` on((`oi`.`product_id` = `p`.`product_id`))) join `categories` `c` on((`p`.`category_id` = `c`.`category_id`))) where (`o`.`status` <> 'Cancelled') group by cast(`o`.`order_date` as date),`c`.`category_name`;

SET FOREIGN_KEY_CHECKS = 1;
