-- MySQL init script for garage_db
CREATE DATABASE IF NOT EXISTS `garage_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `garage_db`;

DROP TABLE IF EXISTS `garage_record`;
DROP TABLE IF EXISTS `garage_reservation`;
DROP TABLE IF EXISTS `driver_profile`;
DROP TABLE IF EXISTS `garage_vehicle`;
DROP TABLE IF EXISTS `garage_space`;
DROP TABLE IF EXISTS `garage_user`;

CREATE TABLE `garage_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL UNIQUE,
  `password` VARCHAR(128) NOT NULL,
  `role` VARCHAR(32) NOT NULL,
  `display_name` VARCHAR(64),
  `phone` VARCHAR(32),
  `license_no` VARCHAR(64),
  `license_type` VARCHAR(32),
  `is_deleted` INT DEFAULT 0,
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_username` (`username`),
  INDEX `idx_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `garage_space` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `area_name` VARCHAR(64),
  `floor_no` VARCHAR(32),
  `space_no` VARCHAR(64) NOT NULL UNIQUE,
  `space_type` VARCHAR(16),
  `status` VARCHAR(16),
  `owner_user_id` BIGINT,
  `remark` VARCHAR(255),
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_space_no` (`space_no`),
  INDEX `idx_space_status` (`status`),
  INDEX `idx_space_owner_user_id` (`owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `garage_vehicle` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plate_no` VARCHAR(32) NOT NULL UNIQUE,
  `owner_name` VARCHAR(64),
  `owner_phone` VARCHAR(32),
  `vehicle_type` VARCHAR(16),
  `member_type` VARCHAR(16),
  `bind_space_no` VARCHAR(64),
  `expire_date` VARCHAR(32),
  `status` VARCHAR(16),
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_vehicle_user_id` (`user_id`),
  INDEX `idx_vehicle_plate_no` (`plate_no`),
  INDEX `idx_vehicle_bind_space_no` (`bind_space_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `driver_profile` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `driver_name` VARCHAR(64),
  `license_no` VARCHAR(64) NOT NULL UNIQUE,
  `license_type` VARCHAR(32),
  `valid_until` VARCHAR(32),
  `phone` VARCHAR(32),
  `status` VARCHAR(16),
  `remark` VARCHAR(255),
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_profile_user_id` (`user_id`),
  INDEX `idx_profile_license_no` (`license_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `garage_reservation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `vehicle_id` BIGINT,
  `plate_no` VARCHAR(32) NOT NULL,
  `space_no` VARCHAR(64) NOT NULL,
  `reservation_time` VARCHAR(32),
  `reservation_status` VARCHAR(16),
  `check_in_time` VARCHAR(32),
  `cancel_time` VARCHAR(32),
  `remark` VARCHAR(255),
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_reservation_user_id` (`user_id`),
  INDEX `idx_reservation_plate_no` (`plate_no`),
  INDEX `idx_reservation_space_no` (`space_no`),
  INDEX `idx_reservation_status` (`reservation_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `garage_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plate_no` VARCHAR(32) NOT NULL,
  `space_no` VARCHAR(64) NOT NULL,
  `in_time` VARCHAR(32),
  `out_time` VARCHAR(32),
  `parking_minutes` VARCHAR(32),
  `total_fee` VARCHAR(32),
  `pay_status` VARCHAR(16),
  `record_status` VARCHAR(16),
  `remark` VARCHAR(255),
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_record_user_id` (`user_id`),
  INDEX `idx_record_plate_no` (`plate_no`),
  INDEX `idx_record_space_no` (`space_no`),
  INDEX `idx_record_status` (`record_status`),
  INDEX `idx_record_in_time` (`in_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `garage_user` (`id`, `username`, `password`, `role`, `display_name`, `phone`, `license_no`, `license_type`, `is_deleted`)
VALUES
  (1, 'admin', '123456', 'admin', 'System Admin', '13800000000', 'ADM000001', 'C1', 0),
  (2, 'user1', '123456', 'user', 'Normal User', '13900000000', 'USR000001', 'C2', 0);

INSERT INTO `garage_space` (`id`, `area_name`, `floor_no`, `space_no`, `space_type`, `status`, `owner_user_id`, `remark`)
VALUES
  (1, 'A区', 'B1', 'A-001', '1', '0', 2, '固定车位'),
  (2, 'A区', 'B1', 'A-002', '2', '1', 2, '占用中'),
  (3, 'A区', 'B1', 'A-003', '3', '4', 2, '预约中'),
  (4, 'B区', 'B2', 'B-101', '1', '0', NULL, NULL),
  (5, 'B区', 'B2', 'B-102', '2', '2', NULL, '维护中'),
  (6, 'C区', 'B3', 'C-201', '1', '3', NULL, '停用'),
  (7, 'C区', 'B3', 'C-202', '2', '0', NULL, NULL),
  (8, 'D区', 'B4', 'D-301', '1', '0', NULL, NULL);

INSERT INTO `garage_vehicle` (`id`, `user_id`, `plate_no`, `owner_name`, `owner_phone`, `vehicle_type`, `member_type`, `bind_space_no`, `expire_date`, `status`)
VALUES
  (1, 2, '渝A12345', 'Normal User', '13900000000', '1', '2', 'A-001', '2026-12-31', '1');

INSERT INTO `driver_profile` (`id`, `user_id`, `driver_name`, `license_no`, `license_type`, `valid_until`, `phone`, `status`, `remark`)
VALUES
  (1, 2, 'Normal User', 'USR000001', 'C2', '2028-12-31', '13900000000', '1', '默认档案');

INSERT INTO `garage_reservation` (`id`, `user_id`, `vehicle_id`, `plate_no`, `space_no`, `reservation_time`, `reservation_status`, `check_in_time`, `cancel_time`, `remark`)
VALUES
  (1, 2, 1, '渝A12345', 'A-003', '2026-03-09 11:30:00', '0', NULL, NULL, '初始化预约数据');

INSERT INTO `garage_record` (`id`, `user_id`, `plate_no`, `space_no`, `in_time`, `out_time`, `parking_minutes`, `total_fee`, `pay_status`, `record_status`, `remark`)
VALUES
  (1, 2, '渝A12345', 'A-002', '2026-03-09 10:00:00', NULL, NULL, NULL, '0', '0', '初始化在库数据');
