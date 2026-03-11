INSERT INTO garage_user (id, username, password, role, display_name, phone, license_no, license_type, is_deleted)
VALUES
  (1, 'admin', '123456', 'admin', 'System Admin', '13800000000', 'ADM000001', 'C1', 0),
  (2, 'user1', '123456', 'user', 'Normal User', '13900000000', 'USR000001', 'C2', 0);

INSERT INTO garage_space (id, area_name, floor_no, space_no, space_type, status, owner_user_id, remark)
VALUES
  (1, 'A区', 'B1', 'A-001', '1', '0', 2, '固定车位'),
  (2, 'A区', 'B1', 'A-002', '2', '1', 2, '占用中'),
  (3, 'A区', 'B1', 'A-003', '3', '4', 2, '预约中'),
  (4, 'B区', 'B2', 'B-101', '1', '0', NULL, NULL),
  (5, 'B区', 'B2', 'B-102', '2', '2', NULL, '维护中'),
  (6, 'C区', 'B3', 'C-201', '1', '3', NULL, '停用'),
  (7, 'C区', 'B3', 'C-202', '2', '0', NULL, NULL),
  (8, 'D区', 'B4', 'D-301', '1', '0', NULL, NULL);

INSERT INTO garage_vehicle (id, user_id, plate_no, owner_name, owner_phone, vehicle_type, member_type, bind_space_no, expire_date, status)
VALUES
  (1, 2, '渝A12345', 'Normal User', '13900000000', '1', '2', 'A-001', '2026-12-31', '1');

INSERT INTO driver_profile (id, user_id, driver_name, license_no, license_type, valid_until, phone, status, remark)
VALUES
  (1, 2, 'Normal User', 'USR000001', 'C2', '2028-12-31', '13900000000', '1', '默认档案');

INSERT INTO garage_reservation (id, user_id, vehicle_id, plate_no, space_no, reservation_time, reservation_status, check_in_time, cancel_time, remark)
VALUES
  (1, 2, 1, '渝A12345', 'A-003', '2026-03-09 11:30:00', '0', NULL, NULL, '初始化预约数据');

INSERT INTO garage_record (id, user_id, plate_no, space_no, in_time, out_time, parking_minutes, total_fee, pay_status, record_status, remark)
VALUES
  (1, 2, '渝A12345', 'A-002', '2026-03-09 10:00:00', NULL, NULL, NULL, '0', '0', '初始化在库数据');
