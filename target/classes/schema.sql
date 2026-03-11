DROP TABLE IF EXISTS garage_record;
DROP TABLE IF EXISTS garage_reservation;
DROP TABLE IF EXISTS driver_profile;
DROP TABLE IF EXISTS garage_vehicle;
DROP TABLE IF EXISTS garage_space;
DROP TABLE IF EXISTS garage_user;

CREATE TABLE garage_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  role VARCHAR(32) NOT NULL,
  display_name VARCHAR(64),
  phone VARCHAR(32),
  license_no VARCHAR(64),
  license_type VARCHAR(32),
  is_deleted INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_username (username),
  INDEX idx_user_role (role)
);

CREATE TABLE garage_space (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  area_name VARCHAR(64),
  floor_no VARCHAR(32),
  space_no VARCHAR(64) NOT NULL UNIQUE,
  space_type VARCHAR(16),
  status VARCHAR(16),
  owner_user_id BIGINT,
  remark VARCHAR(255),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_space_no (space_no),
  INDEX idx_space_status (status),
  INDEX idx_space_owner_user_id (owner_user_id)
);

CREATE TABLE garage_vehicle (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  plate_no VARCHAR(32) NOT NULL UNIQUE,
  owner_name VARCHAR(64),
  owner_phone VARCHAR(32),
  vehicle_type VARCHAR(16),
  member_type VARCHAR(16),
  bind_space_no VARCHAR(64),
  expire_date VARCHAR(32),
  status VARCHAR(16),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_vehicle_user_id (user_id),
  INDEX idx_vehicle_plate_no (plate_no),
  INDEX idx_vehicle_bind_space_no (bind_space_no)
);

CREATE TABLE driver_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  driver_name VARCHAR(64),
  license_no VARCHAR(64) NOT NULL UNIQUE,
  license_type VARCHAR(32),
  valid_until VARCHAR(32),
  phone VARCHAR(32),
  status VARCHAR(16),
  remark VARCHAR(255),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_profile_user_id (user_id),
  INDEX idx_profile_license_no (license_no)
);

CREATE TABLE garage_reservation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  vehicle_id BIGINT,
  plate_no VARCHAR(32) NOT NULL,
  space_no VARCHAR(64) NOT NULL,
  reservation_time VARCHAR(32),
  reservation_status VARCHAR(16),
  check_in_time VARCHAR(32),
  cancel_time VARCHAR(32),
  remark VARCHAR(255),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_reservation_user_id (user_id),
  INDEX idx_reservation_plate_no (plate_no),
  INDEX idx_reservation_space_no (space_no),
  INDEX idx_reservation_status (reservation_status)
);

CREATE TABLE garage_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  plate_no VARCHAR(32) NOT NULL,
  space_no VARCHAR(64) NOT NULL,
  in_time VARCHAR(32),
  out_time VARCHAR(32),
  parking_minutes VARCHAR(32),
  total_fee VARCHAR(32),
  pay_status VARCHAR(16),
  record_status VARCHAR(16),
  remark VARCHAR(255),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_record_user_id (user_id),
  INDEX idx_record_plate_no (plate_no),
  INDEX idx_record_space_no (space_no),
  INDEX idx_record_status (record_status),
  INDEX idx_record_in_time (in_time)
);
