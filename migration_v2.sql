-- v2 migration: reservation support + monitor websocket support model alignment

CREATE TABLE IF NOT EXISTS `garage_reservation` (
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
);

-- Optional: if your historical data has reservation-like records, map status by hand.
-- Status conventions:
-- garage_space.status: 0 free, 1 occupied, 2 maintenance, 3 disabled, 4 reserved
-- garage_reservation.reservation_status: 0 active, 1 canceled, 2 checked-in
