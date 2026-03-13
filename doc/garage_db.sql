/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80200
 Source Host           : localhost:3306
 Source Schema         : garage_db

 Target Server Type    : MySQL
 Target Server Version : 80200
 File Encoding         : 65001

 Date: 13/03/2026 20:55:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for driver_profile
-- ----------------------------
DROP TABLE IF EXISTS `driver_profile`;
CREATE TABLE `driver_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '驾驶员档案ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `driver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '驾驶员名称',
  `license_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '驾驶证号',
  `license_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '驾驶证类型',
  `valid_until` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '有效期至',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `license_no`(`license_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_license_no`(`license_no` ASC) USING BTREE,
  CONSTRAINT `driver_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `garage_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '驾驶员档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of driver_profile
-- ----------------------------
INSERT INTO `driver_profile` VALUES (1, 2, '普通用户', 'USR000001', 'C2', '2028-12-31', '13900000000', '1', '默认档案', '2026-03-09 20:14:29', '2026-03-09 20:14:29');
INSERT INTO `driver_profile` VALUES (2, 5, 'Smoke User', 'LIC0311201123', 'C1', '2030-12-31', '13812345678', '1', 'smoke', '2026-03-11 20:11:23', '2026-03-11 20:11:23');
INSERT INTO `driver_profile` VALUES (3, 6, 'Branch User', 'BR0311201350', 'C1', NULL, '13812345679', '1', NULL, '2026-03-11 20:13:51', '2026-03-11 20:13:51');
INSERT INTO `driver_profile` VALUES (4, 7, 'Owner Fix', 'OF0311201730', 'C1', NULL, '13812345670', '1', NULL, '2026-03-11 20:17:31', '2026-03-11 20:17:31');
INSERT INTO `driver_profile` VALUES (5, 8, 'Upd User', 'UP0311201856', 'B2', NULL, '13812345672', '1', 'b', '2026-03-11 20:18:57', '2026-03-11 20:18:57');
INSERT INTO `driver_profile` VALUES (6, 11, 'Smoke User', 'LIC0311203147', 'C1', '2030-12-31', '13812345674', '1', 'smoke', '2026-03-11 20:31:47', '2026-03-11 20:31:47');
INSERT INTO `driver_profile` VALUES (7, 12, 'Pay User', 'PAY0311215805', 'C1', NULL, '13812345666', '1', NULL, '2026-03-11 21:58:06', '2026-03-11 21:58:06');
INSERT INTO `driver_profile` VALUES (8, 13, 'Arch User', 'AR0312151134', 'C1', NULL, '13812345555', '1', NULL, '2026-03-12 15:11:34', '2026-03-12 15:11:34');

-- ----------------------------
-- Table structure for driverprofiles
-- ----------------------------
DROP TABLE IF EXISTS `driverprofiles`;
CREATE TABLE `driverprofiles`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  `userId` int NOT NULL COMMENT '用户ID',
  `driverName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '驾驶员姓名',
  `licenseNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '驾驶证号',
  `licenseType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'C1' COMMENT '驾驶证类型',
  `validUntil` date NULL DEFAULT NULL COMMENT '驾驶证有效期',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '状态（1有效|0无效）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `createdAt` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `licenseNo`(`licenseNo` ASC) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE,
  INDEX `idx_licenseNo`(`licenseNo` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `driverprofiles_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '驾驶档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of driverprofiles
-- ----------------------------
INSERT INTO `driverprofiles` VALUES (1, 2, '普通用户', 'USR000001', 'C2', '2028-12-31', '13900000000', '1', '默认档案', '2026-03-09 20:44:34', '2026-03-09 20:44:34');

-- ----------------------------
-- Table structure for garage_record
-- ----------------------------
DROP TABLE IF EXISTS `garage_record`;
CREATE TABLE `garage_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plate_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车牌号',
  `space_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位号',
  `in_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '进车时间',
  `out_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出车时间',
  `parking_minutes` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '停车分钟数',
  `total_fee` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '总费用',
  `pay_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付状态',
  `record_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '记录状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_plate_no`(`plate_no` ASC) USING BTREE,
  INDEX `idx_space_no`(`space_no` ASC) USING BTREE,
  INDEX `idx_in_time`(`in_time` ASC) USING BTREE,
  CONSTRAINT `garage_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `garage_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '停车记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of garage_record
-- ----------------------------
INSERT INTO `garage_record` VALUES (1, 2, '沪A12345', 'A-002', '2026-03-09 10:00:00', '2026-03-10 23:58:14', '2278', '200', '1', '1', '初始化数据', '2026-03-09 20:14:29', '2026-03-10 23:58:23');
INSERT INTO `garage_record` VALUES (2, 2, 'TEST7980', 'B-101', '2026-03-10 18:30:15', '2026-03-10 18:30:15', '0', '0', '1', '1', 'from reservation #1', '2026-03-10 18:30:15', '2026-03-10 18:30:16');
INSERT INTO `garage_record` VALUES (3, 2, 'TEST7980', 'D-301', '2026-03-10 23:57:41', '2026-03-11 21:25:44', '1288', '110', '1', '1', 'from reservation #2', '2026-03-10 23:57:42', '2026-03-11 21:26:06');
INSERT INTO `garage_record` VALUES (4, 5, 'YU0311201123', 'Z-0311201123', '2026-03-11 20:11:23', '2026-03-11 20:11:23', '0', '0', '1', '1', 'from reservation #3', '2026-03-11 20:11:24', '2026-03-11 20:11:24');
INSERT INTO `garage_record` VALUES (5, 6, 'BR0311201350', 'X-0311201350', '2026-03-11 20:13:51', '2026-03-11 20:13:51', '0', '0', '1', '1', 'direct-in', '2026-03-11 20:13:51', '2026-03-11 20:13:52');
INSERT INTO `garage_record` VALUES (6, 7, 'OF0311201730', 'N-0311201730', '2026-03-11 20:17:31', '2026-03-11 20:17:31', '0', '0', '1', '1', 'owner-fix', '2026-03-11 20:17:31', '2026-03-11 20:17:31');
INSERT INTO `garage_record` VALUES (7, 11, 'FN0311203147', 'W-0311203147', '2026-03-11 20:31:47', '2026-03-11 20:31:47', '0', '0', '1', '1', 'from reservation #6', '2026-03-11 20:31:48', '2026-03-11 20:31:48');
INSERT INTO `garage_record` VALUES (8, 2, '沪A12345', 'A-001', '2026-03-11 21:31:26', '2026-03-11 21:32:19', '0', '5', '1', '1', NULL, '2026-03-11 21:31:39', '2026-03-11 21:32:32');
INSERT INTO `garage_record` VALUES (9, 12, 'PA0311215805', 'P-0311215805', '2026-03-11 19:58:06', '2026-03-11 21:58:06', '120', '10', '1', '1', 'pay-flow；支付方式:微信支付, 金额:10元, 时间:2026-03-11 21:58:06', '2026-03-11 21:58:06', '2026-03-11 21:58:06');
INSERT INTO `garage_record` VALUES (10, 2, '沪A12345', 'D-301', '2026-03-11 22:03:59', '2026-03-13 11:24:30', '2240', '190', '1', '1', '来自预约 #7；支付方式:微信支付, 金额:190元, 时间:2026-03-13 11:24:30', '2026-03-11 22:04:00', '2026-03-13 11:24:59');
INSERT INTO `garage_record` VALUES (11, 13, 'AR0312151134', 'E-0312151134', '2026-03-12 13:11:34', '2026-03-12 15:11:34', '120', '10', '1', '1', 'arch-smoke；支付方式:支付宝, 金额:10元, 时间:2026-03-12 15:11:34', '2026-03-12 15:11:34', '2026-03-12 15:11:35');
INSERT INTO `garage_record` VALUES (12, 2, 'TEST7980', 'A-001', '2026-03-13 20:05:47', NULL, NULL, NULL, '0', '0', '来自预约 #8', '2026-03-13 20:05:48', '2026-03-13 20:05:48');

-- ----------------------------
-- Table structure for garage_reservation
-- ----------------------------
DROP TABLE IF EXISTS `garage_reservation`;
CREATE TABLE `garage_reservation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `vehicle_id` bigint NULL DEFAULT NULL,
  `plate_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `space_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reservation_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `reservation_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `check_in_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `cancel_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reservation_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_reservation_plate_no`(`plate_no` ASC) USING BTREE,
  INDEX `idx_reservation_space_no`(`space_no` ASC) USING BTREE,
  INDEX `idx_reservation_status`(`reservation_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of garage_reservation
-- ----------------------------
INSERT INTO `garage_reservation` VALUES (1, 2, 2, 'TEST7980', 'B-101', '2026-03-10 18:29:38', '2', '2026-03-10 18:30:15', NULL, 'smoke-test', '2026-03-10 18:29:39', '2026-03-10 18:30:15');
INSERT INTO `garage_reservation` VALUES (2, 2, 2, 'TEST7980', 'D-301', '2026-03-10 19:42:49', '2', '2026-03-10 23:57:41', NULL, '', '2026-03-10 19:42:49', '2026-03-10 23:57:42');
INSERT INTO `garage_reservation` VALUES (3, 5, 4, 'YU0311201123', 'Z-0311201123', '2026-03-11 20:11:23', '2', '2026-03-11 20:11:23', NULL, 'smoke reservation', '2026-03-11 20:11:23', '2026-03-11 20:11:24');
INSERT INTO `garage_reservation` VALUES (4, 6, 5, 'BR0311201350', 'Y-0311201350', '2026-03-11 20:13:51', '1', NULL, '2026-03-11 20:13:51', 'cancel-branch', '2026-03-11 20:13:51', '2026-03-11 20:13:51');
INSERT INTO `garage_reservation` VALUES (5, 7, 6, 'OF0311201730', 'M-0311201730', '2026-03-11 20:17:30', '1', NULL, '2026-03-11 20:17:31', 'owner-fix', '2026-03-11 20:17:31', '2026-03-11 20:17:31');
INSERT INTO `garage_reservation` VALUES (6, 11, 8, 'FN0311203147', 'W-0311203147', '2026-03-11 20:31:47', '2', '2026-03-11 20:31:47', NULL, 'smoke reservation', '2026-03-11 20:31:47', '2026-03-11 20:31:48');
INSERT INTO `garage_reservation` VALUES (7, 2, 1, '沪A12345', 'D-301', '2026-03-11 22:02:23', '2', '2026-03-11 22:03:59', NULL, NULL, '2026-03-11 22:02:24', '2026-03-11 22:04:00');
INSERT INTO `garage_reservation` VALUES (8, 2, 2, 'TEST7980', 'A-001', '2026-03-12 00:23:52', '2', '2026-03-13 20:05:47', NULL, NULL, '2026-03-12 00:23:52', '2026-03-13 20:05:48');

-- ----------------------------
-- Table structure for garage_space
-- ----------------------------
DROP TABLE IF EXISTS `garage_space`;
CREATE TABLE `garage_space`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '车位ID',
  `area_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域名称',
  `floor_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼层号',
  `space_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位号',
  `space_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车位类型',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车位状态',
  `owner_user_id` bigint NULL DEFAULT NULL COMMENT '业主用户ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `space_no`(`space_no` ASC) USING BTREE,
  INDEX `idx_space_no`(`space_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_owner_user_id`(`owner_user_id` ASC) USING BTREE,
  CONSTRAINT `garage_space_ibfk_1` FOREIGN KEY (`owner_user_id`) REFERENCES `garage_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车库车位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of garage_space
-- ----------------------------
INSERT INTO `garage_space` VALUES (1, 'A区', 'B1', 'A-001', '1', '1', 2, '固定车位', '2026-03-09 20:14:29', '2026-03-13 20:05:48');
INSERT INTO `garage_space` VALUES (2, 'A区', 'B1', 'A-002', '2', '0', NULL, '临停车位', '2026-03-09 20:14:29', '2026-03-10 23:58:23');
INSERT INTO `garage_space` VALUES (3, 'A区', 'B1', 'A-003', '3', '0', NULL, '充电车位', '2026-03-09 20:14:29', '2026-03-09 20:14:29');
INSERT INTO `garage_space` VALUES (4, 'B区', 'B2', 'B-101', '1', '0', 2, '', '2026-03-09 20:14:29', '2026-03-10 18:30:16');
INSERT INTO `garage_space` VALUES (5, 'B区', 'B2', 'B-102', '2', '2', NULL, '维护中', '2026-03-09 20:14:29', '2026-03-09 20:14:29');
INSERT INTO `garage_space` VALUES (6, 'C区', 'B3', 'C-201', '1', '3', NULL, '停用', '2026-03-09 20:14:29', '2026-03-09 20:14:29');
INSERT INTO `garage_space` VALUES (7, 'C区', 'B3', 'C-202', '2', '0', NULL, NULL, '2026-03-09 20:14:29', '2026-03-09 20:14:29');
INSERT INTO `garage_space` VALUES (8, 'D区', 'B4', 'D-301', '1', '0', 2, NULL, '2026-03-09 20:14:29', '2026-03-13 11:24:59');
INSERT INTO `garage_space` VALUES (9, 'a', '', 'a', '1', '0', NULL, '', '2026-03-10 20:47:34', '2026-03-10 20:47:34');

-- ----------------------------
-- Table structure for garage_user
-- ----------------------------
DROP TABLE IF EXISTS `garage_user`;
CREATE TABLE `garage_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色',
  `display_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '显示名称',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `license_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证件号',
  `license_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证件类型',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车库用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of garage_user
-- ----------------------------
INSERT INTO `garage_user` VALUES (1, 'admin', '$2a$10$PxVIIGVpkGPTh.wFtYFXFuEPHNwD2G145c5.v0Uvp.ojsbT4l4Eh2', 'admin', '系统管理员', '13800000000', 'ADM000001', 'C1', 0, '2026-03-09 20:14:29', '2026-03-10 17:35:15');
INSERT INTO `garage_user` VALUES (2, 'user1', '$2a$10$Ak345Ci9NIP9BBRch0A7n.VYDgQlLS8iUiFK8E7jaJdvB.mf6RA76', 'user', '普通用户', '13900000000', 'USR000001', 'C2', 0, '2026-03-09 20:14:29', '2026-03-10 17:35:15');
INSERT INTO `garage_user` VALUES (3, 'u1773230624837', '$2a$10$5Vh7AC8PsF85vGK7gL7f2u17VC1HHzcjJKRP6oz1A5tJ/Fob/mAF.', 'user', 'newu', '13912345678', NULL, NULL, 0, '2026-03-11 20:03:45', '2026-03-11 20:03:45');
INSERT INTO `garage_user` VALUES (4, 'u1773230705045', '$2a$10$BmXA9WEBbblErPkCbUWn1.wPfM5ZWbNZEmHTzlbJdatu2atgOhD5.', 'user', 'newu', '13912345678', NULL, NULL, 0, '2026-03-11 20:05:05', '2026-03-11 20:05:05');
INSERT INTO `garage_user` VALUES (5, 'smoke0311201123', '$2a$10$.v/tTm9gXnq1LV2B9ygyNOOqX.fLXsa3eRRiFI/WJZJdwHHFkLMAW', 'user', 'Smoke User', '13812345678', NULL, NULL, 0, '2026-03-11 20:11:23', '2026-03-11 20:11:23');
INSERT INTO `garage_user` VALUES (6, 'branch0311201350', '$2a$10$aC0DrViFCNEucrEC.DYw3OfT5P2GFA7t0rZzRWJjzY9wIv.2LJMUC', 'user', 'Branch User', '13812345679', NULL, NULL, 0, '2026-03-11 20:13:51', '2026-03-11 20:13:51');
INSERT INTO `garage_user` VALUES (7, 'ownerfix0311201730', '$2a$10$SRRgpW8kKmRrlPhWNbWsIu1UDB9q9yqwosKRvIt.ZnjaBoTaiHRqO', 'user', 'Owner Fix', '13812345670', NULL, NULL, 0, '2026-03-11 20:17:31', '2026-03-11 20:17:31');
INSERT INTO `garage_user` VALUES (8, 'upd0311201856', '$2a$10$Gc66lAAKWNCIpMJVR9RX/.FeGI72Z5a/a2rWWQEtccLKcy69K5NAa', 'user', 'Upd User 2', '13812345672', 'ABC123', 'C1', 0, '2026-03-11 20:18:57', '2026-03-11 20:18:57');
INSERT INTO `garage_user` VALUES (9, 'clr0311202048', '$2a$10$W/uRXKpSiMl2i7cWOGB.yuPIOgXK5hXCxTpzRl9E8by42.kFhRv3O', 'user', 'Clear User', '13812345675', 'CLR123', 'C1', 0, '2026-03-11 20:20:49', '2026-03-11 20:20:49');
INSERT INTO `garage_user` VALUES (10, 'clr20311202628', '$2a$10$SM0Qd6P1LhbXKI/relB1Q.1HTdn0dq7N0/DG0.WA6PSt7hgqCmvdy', 'user', 'Clear User', '', '', '', 0, '2026-03-11 20:26:29', '2026-03-11 20:26:29');
INSERT INTO `garage_user` VALUES (11, 'final0311203147', '$2a$10$MtbT7rOyrn7DDPkMVWASquZZTte7S31wQeIQOjhnQD4b2o4527PZi', 'user', 'Smoke User', '13812345674', NULL, NULL, 0, '2026-03-11 20:31:47', '2026-03-11 20:31:47');
INSERT INTO `garage_user` VALUES (12, 'pay0311215805', '$2a$10$iTWdZzDOD5yUlrs6qLau/.hEpFptx5QcmVnsih7wQbsvnW/wVIf8.', 'user', 'Pay User', '13812345666', NULL, NULL, 0, '2026-03-11 21:58:06', '2026-03-11 21:58:06');
INSERT INTO `garage_user` VALUES (13, 'arch0312151134', '$2a$10$MsO4rY/0qvmCFAXhhuOQYuA13yo6GiflwNTpEgM7V1/MCPDfNzHmq', 'user', 'Arch User', '13812345555', NULL, NULL, 0, '2026-03-12 15:11:34', '2026-03-12 15:11:34');

-- ----------------------------
-- Table structure for garage_vehicle
-- ----------------------------
DROP TABLE IF EXISTS `garage_vehicle`;
CREATE TABLE `garage_vehicle`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plate_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车牌号',
  `owner_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车主名称',
  `owner_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车主电话',
  `vehicle_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车辆类型',
  `member_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会员类型',
  `bind_space_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '绑定车位号',
  `expire_date` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '过期日期',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车辆状态',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `plate_no`(`plate_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_plate_no`(`plate_no` ASC) USING BTREE,
  INDEX `idx_bind_space_no`(`bind_space_no` ASC) USING BTREE,
  CONSTRAINT `garage_vehicle_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `garage_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车库车辆表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of garage_vehicle
-- ----------------------------
INSERT INTO `garage_vehicle` VALUES (1, 2, '沪A12345', '普通用户', '13900000000', '1', '2', 'A-001', '2026-12-31', '1', '2026-03-09 20:14:29', '2026-03-09 20:14:29');
INSERT INTO `garage_vehicle` VALUES (2, 2, 'TEST7980', 'user1', '13900000000', '1', '1', NULL, NULL, '1', '2026-03-10 18:29:38', '2026-03-10 18:29:38');
INSERT INTO `garage_vehicle` VALUES (3, 4, 'AB705045', 'newu', '13912345678', '1', '1', NULL, NULL, '1', '2026-03-11 20:05:05', '2026-03-11 20:05:05');
INSERT INTO `garage_vehicle` VALUES (4, 5, 'YU0311201123', 'Smoke User', '13812345678', '1', '1', NULL, NULL, '1', '2026-03-11 20:11:23', '2026-03-11 20:11:23');
INSERT INTO `garage_vehicle` VALUES (5, 6, 'BR0311201350', 'Branch User', '13812345679', '1', '1', NULL, NULL, '1', '2026-03-11 20:13:51', '2026-03-11 20:13:51');
INSERT INTO `garage_vehicle` VALUES (6, 7, 'OF0311201730', 'Owner Fix', '13812345670', '1', '1', NULL, NULL, '1', '2026-03-11 20:17:31', '2026-03-11 20:17:31');
INSERT INTO `garage_vehicle` VALUES (7, 8, 'UP0311201856', 'Upd User X', '13812345673', '1', '2', NULL, NULL, '1', '2026-03-11 20:18:57', '2026-03-11 20:18:57');
INSERT INTO `garage_vehicle` VALUES (8, 11, 'FN0311203147', 'Smoke User', '13812345674', '1', '1', NULL, NULL, '1', '2026-03-11 20:31:47', '2026-03-11 20:31:47');
INSERT INTO `garage_vehicle` VALUES (9, 12, 'PA0311215805', 'Pay User', '13812345666', '1', '1', NULL, NULL, '1', '2026-03-11 21:58:06', '2026-03-11 21:58:06');
INSERT INTO `garage_vehicle` VALUES (10, 13, 'AR0312151134', 'Arch User', '13812345555', '1', '1', NULL, NULL, '1', '2026-03-12 15:11:34', '2026-03-12 15:11:34');

-- ----------------------------
-- Table structure for records
-- ----------------------------
DROP TABLE IF EXISTS `records`;
CREATE TABLE `records`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `userId` int NOT NULL COMMENT '用户ID',
  `plateNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车牌号',
  `spaceNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位编号',
  `inTime` datetime NOT NULL COMMENT '入库时间',
  `outTime` datetime NULL DEFAULT NULL COMMENT '出库时间',
  `parkingMinutes` int NULL DEFAULT NULL COMMENT '停车时长（分钟）',
  `totalFee` decimal(10, 2) NULL DEFAULT NULL COMMENT '停车费用',
  `payStatus` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '支付状态（0未支付|1已支付）',
  `recordStatus` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '记录状态（0进行中|1已完成）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `createdAt` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE,
  INDEX `idx_plateNo`(`plateNo` ASC) USING BTREE,
  INDEX `idx_spaceNo`(`spaceNo` ASC) USING BTREE,
  INDEX `idx_recordStatus`(`recordStatus` ASC) USING BTREE,
  INDEX `idx_inTime`(`inTime` ASC) USING BTREE,
  CONSTRAINT `records_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '停车记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of records
-- ----------------------------
INSERT INTO `records` VALUES (1, 2, '沪A12345', 'A-002', '2026-03-09 20:44:34', NULL, NULL, NULL, '0', '0', '初始化数据', '2026-03-09 20:44:34', '2026-03-09 20:44:34');

-- ----------------------------
-- Table structure for spaces
-- ----------------------------
DROP TABLE IF EXISTS `spaces`;
CREATE TABLE `spaces`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '车位ID',
  `areaName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域名称',
  `floorNo` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楼层号',
  `spaceNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位编号',
  `spaceType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位类型（1固定|2临停|3充电）',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '状态（0空闲|1占用|2维护|3停用）',
  `ownerUserId` int NULL DEFAULT NULL COMMENT '所有者用户ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `createdAt` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `spaceNo`(`spaceNo` ASC) USING BTREE,
  INDEX `idx_areaName`(`areaName` ASC) USING BTREE,
  INDEX `idx_spaceNo`(`spaceNo` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `ownerUserId`(`ownerUserId` ASC) USING BTREE,
  CONSTRAINT `spaces_ibfk_1` FOREIGN KEY (`ownerUserId`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '停车位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spaces
-- ----------------------------
INSERT INTO `spaces` VALUES (1, 'A区', 'B1', 'A-001', '1', '0', 2, '固定车位', '2026-03-09 20:44:34', '2026-03-09 20:44:34');
INSERT INTO `spaces` VALUES (2, 'A区', 'B1', 'A-002', '2', '1', NULL, '临停车位', '2026-03-09 20:44:34', '2026-03-09 20:44:34');
INSERT INTO `spaces` VALUES (3, 'A区', 'B1', 'A-003', '3', '0', NULL, '充电车位', '2026-03-09 20:44:34', '2026-03-09 20:44:34');
INSERT INTO `spaces` VALUES (4, 'B区', 'B2', 'B-101', '1', '0', NULL, '', '2026-03-09 20:44:34', '2026-03-09 20:44:34');
INSERT INTO `spaces` VALUES (5, 'B区', 'B2', 'B-102', '2', '2', NULL, '维护中', '2026-03-09 20:44:34', '2026-03-09 20:44:34');
INSERT INTO `spaces` VALUES (6, 'C区', 'B3', 'C-201', '1', '3', NULL, '停用', '2026-03-09 20:44:34', '2026-03-09 20:44:34');
INSERT INTO `spaces` VALUES (7, 'C区', 'B3', 'C-202', '2', '0', NULL, '', '2026-03-09 20:44:34', '2026-03-09 20:44:34');
INSERT INTO `spaces` VALUES (8, 'D区', 'B4', 'D-301', '1', '0', NULL, '', '2026-03-09 20:44:34', '2026-03-09 20:44:34');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '角色（admin|user）',
  `displayName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示名称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '电话',
  `licenseNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '驾驶证号',
  `licenseType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'C1' COMMENT '驾驶证类型',
  `createdAt` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '123456', 'admin', '系统管理员', '13800000000', 'ADM000001', 'C1', '2026-03-09 20:44:34');
INSERT INTO `users` VALUES (2, 'user1', '123456', 'user', '普通用户', '13900000000', 'USR000001', 'C2', '2026-03-09 20:44:34');

-- ----------------------------
-- Table structure for vehicles
-- ----------------------------
DROP TABLE IF EXISTS `vehicles`;
CREATE TABLE `vehicles`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  `userId` int NOT NULL COMMENT '所有者用户ID',
  `plateNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车牌号',
  `ownerName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车主姓名',
  `ownerPhone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车主电话',
  `vehicleType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '车辆类型',
  `memberType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '会员类型（1临时|2正式）',
  `bindSpaceNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '绑定车位编号',
  `expireDate` date NULL DEFAULT NULL COMMENT '过期日期',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '状态（1启用|0禁用）',
  `createdAt` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `plateNo`(`plateNo` ASC) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE,
  INDEX `idx_plateNo`(`plateNo` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `vehicles_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车辆表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vehicles
-- ----------------------------
INSERT INTO `vehicles` VALUES (1, 2, '沪A12345', '普通用户', '13900000000', '1', '2', 'A-001', '2026-12-31', '1', '2026-03-09 20:44:34', '2026-03-09 20:44:34');

SET FOREIGN_KEY_CHECKS = 1;
