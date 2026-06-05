-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.4.3 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for hotel_booking
CREATE DATABASE IF NOT EXISTS `hotel_booking` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `hotel_booking`;

-- Dumping structure for table hotel_booking.amenities
CREATE TABLE IF NOT EXISTS `amenities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `icon_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.bookings
CREATE TABLE IF NOT EXISTS `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `num_guests` int NOT NULL DEFAULT '1',
  `status` enum('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'PENDING',
  `payment_status` enum('UNPAID','PARTIAL','PAID','REFUNDED') NOT NULL DEFAULT 'UNPAID',
  `expiry_time` datetime DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `deposit_amount` decimal(38,2) DEFAULT NULL,
  `special_requests` text,
  `cancellation_reason` text,
  `cancelled_at` datetime DEFAULT NULL,
  `checked_in_at` datetime DEFAULT NULL,
  `checked_out_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bookings_room_dates` (`room_id`,`check_in_date`,`check_out_date`),
  KEY `idx_bookings_user` (`user_id`),
  KEY `idx_bookings_status` (`status`),
  CONSTRAINT `fk_bookings_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bookings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_bookings_dates` CHECK ((`check_out_date` > `check_in_date`)),
  CONSTRAINT `chk_bookings_guests` CHECK ((`num_guests` >= 1))
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.booking_rules
CREATE TABLE IF NOT EXISTS `booking_rules` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `room_type_id` bigint DEFAULT NULL,
  `rule_name` varchar(255) DEFAULT NULL,
  `min_days_advance` int NOT NULL DEFAULT '0',
  `max_days_advance` int NOT NULL DEFAULT '365',
  `min_nights` int NOT NULL DEFAULT '1',
  `max_nights` int NOT NULL DEFAULT '30',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deposit_percentage` decimal(38,2) DEFAULT NULL,
  `auto_cancel_minutes` int DEFAULT '30',
  PRIMARY KEY (`id`),
  KEY `fk_booking_rules_room_type` (`room_type_id`),
  CONSTRAINT `fk_booking_rules_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.booking_status_history
CREATE TABLE IF NOT EXISTS `booking_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_id` bigint NOT NULL,
  `old_status` enum('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW') DEFAULT NULL,
  `new_status` enum('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW') NOT NULL,
  `changed_by` bigint DEFAULT NULL,
  `note` text,
  `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bsh_changed_by` (`changed_by`),
  KEY `idx_bsh_booking` (`booking_id`),
  CONSTRAINT `fk_bsh_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bsh_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.cancellation_policies
CREATE TABLE IF NOT EXISTS `cancellation_policies` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `room_type_id` bigint DEFAULT NULL,
  `policy_name` varchar(255) DEFAULT NULL,
  `hours_before_checkin` int NOT NULL,
  `refund_percent` decimal(38,2) DEFAULT NULL,
  `is_force_majeure` tinyint(1) NOT NULL DEFAULT '0',
  `description` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_cancel_policy_room_type` (`room_type_id`),
  CONSTRAINT `fk_cancel_policy_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for procedure hotel_booking.check_booking_conflict
DELIMITER //
CREATE PROCEDURE `check_booking_conflict`(
    IN  p_room_id       BIGINT,
    IN  p_check_in      DATE,
    IN  p_check_out     DATE,
    IN  p_exclude_id    BIGINT,         -- truyền NULL khi tạo mới
    OUT p_conflict_count INT
)
BEGIN
    SELECT COUNT(*) INTO p_conflict_count
    FROM bookings
    WHERE room_id = p_room_id
      AND status NOT IN ('CANCELLED', 'NO_SHOW')
      AND (id <> COALESCE(p_exclude_id, -1))
      -- Điều kiện giao nhau khoảng thời gian (Allen's interval overlap)
      AND p_check_in  < check_out_date
      AND p_check_out > check_in_date;
END//
DELIMITER ;

-- Dumping structure for table hotel_booking.payments
CREATE TABLE IF NOT EXISTS `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_id` bigint NOT NULL,
  `payment_type` enum('PAYMENT','REFUND') NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `payment_method` varchar(20) DEFAULT NULL,
  `status` enum('PENDING','SUCCESS','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
  `transaction_ref` varchar(100) DEFAULT NULL,
  `note` text,
  `processed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `amount_paid` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_payments_booking` (`booking_id`),
  CONSTRAINT `fk_payments_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.pricing_rules
CREATE TABLE IF NOT EXISTS `pricing_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_type_id` bigint DEFAULT NULL,
  `rule_name` varchar(255) DEFAULT NULL,
  `rule_type` enum('SEASONAL','WEEKEND','SPECIAL_EVENT','DISCOUNT') NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `price_modifier` decimal(12,2) DEFAULT NULL,
  `price_percent` decimal(5,2) DEFAULT NULL,
  `min_nights` int NOT NULL DEFAULT '1',
  `priority` int NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `day_of_week` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pricing_rules_room_type` (`room_type_id`),
  CONSTRAINT `fk_pricing_rules_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_pricing_modifier` CHECK ((((`price_modifier` is not null) and (`price_percent` is null)) or ((`price_modifier` is null) and (`price_percent` is not null))))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.refresh_tokens
CREATE TABLE IF NOT EXISTS `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `revoked` bit(1) NOT NULL,
  `token` varchar(512) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKghpmfn23vmxfu3spu3lfg4r2d` (`token`),
  KEY `FK1lih5y2npsf8u5o3vhdb9y0os` (`user_id`),
  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.roles
CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKofx66keruapi6vyqpv6f2or37` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.rooms
CREATE TABLE IF NOT EXISTS `rooms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_type_id` bigint NOT NULL,
  `room_number` varchar(255) NOT NULL,
  `floor` int DEFAULT NULL,
  `description` text,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_available` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_rooms_number` (`room_number`),
  KEY `fk_rooms_room_type` (`room_type_id`),
  CONSTRAINT `fk_rooms_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.room_images
CREATE TABLE IF NOT EXISTS `room_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_type_id` bigint NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `caption` varchar(255) DEFAULT NULL,
  `is_primary` tinyint(1) NOT NULL DEFAULT '0',
  `sort_order` int NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_room_images_room_type` (`room_type_id`),
  CONSTRAINT `fk_room_images_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.room_types
CREATE TABLE IF NOT EXISTS `room_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `base_price` decimal(38,2) DEFAULT NULL,
  `max_capacity` int NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cancellation_policy` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.room_type_amenities
CREATE TABLE IF NOT EXISTS `room_type_amenities` (
  `room_type_id` bigint NOT NULL,
  `amenity_id` bigint NOT NULL,
  PRIMARY KEY (`room_type_id`,`amenity_id`),
  KEY `fk_rta_amenity` (`amenity_id`),
  CONSTRAINT `fk_rta_amenity` FOREIGN KEY (`amenity_id`) REFERENCES `amenities` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rta_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('ADMIN','CUSTOMER','STAFF') NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `username` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table hotel_booking.user_roles
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for view hotel_booking.v_occupancy_current_month
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `v_occupancy_current_month` (
	`room_type_id` BIGINT NOT NULL,
	`room_type_name` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`total_rooms` BIGINT NOT NULL,
	`occupied_rooms` BIGINT NOT NULL,
	`occupancy_rate_pct` DECIMAL(26,2) NULL
) ENGINE=MyISAM;

-- Dumping structure for view hotel_booking.v_revenue_by_month
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `v_revenue_by_month` (
	`month` VARCHAR(1) NULL COLLATE 'utf8mb4_0900_ai_ci',
	`total_revenue` DECIMAL(34,2) NULL,
	`total_refund` DECIMAL(34,2) NULL,
	`net_revenue` DECIMAL(35,2) NULL,
	`num_bookings` BIGINT NOT NULL
) ENGINE=MyISAM;

-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `v_occupancy_current_month`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_occupancy_current_month` AS select `rt`.`id` AS `room_type_id`,`rt`.`name` AS `room_type_name`,count(distinct `r`.`id`) AS `total_rooms`,count(distinct (case when (`b`.`status` in ('CONFIRMED','CHECKED_IN','CHECKED_OUT')) then `r`.`id` end)) AS `occupied_rooms`,round(((count(distinct (case when (`b`.`status` in ('CONFIRMED','CHECKED_IN','CHECKED_OUT')) then `r`.`id` end)) * 100.0) / nullif(count(distinct `r`.`id`),0)),2) AS `occupancy_rate_pct` from ((`room_types` `rt` join `rooms` `r` on(((`r`.`room_type_id` = `rt`.`id`) and (`r`.`is_active` = 1)))) left join `bookings` `b` on(((`b`.`room_id` = `r`.`id`) and (`b`.`check_in_date` <= last_day(curdate())) and (`b`.`check_out_date` >= date_format(curdate(),'%Y-%m-01'))))) group by `rt`.`id`,`rt`.`name`;

-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `v_revenue_by_month`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_revenue_by_month` AS select date_format(`p`.`processed_at`,'%Y-%m') AS `month`,sum((case when (`p`.`payment_type` = 'PAYMENT') then `p`.`amount` else 0 end)) AS `total_revenue`,sum((case when (`p`.`payment_type` = 'REFUND') then `p`.`amount` else 0 end)) AS `total_refund`,(sum((case when (`p`.`payment_type` = 'PAYMENT') then `p`.`amount` else 0 end)) - sum((case when (`p`.`payment_type` = 'REFUND') then `p`.`amount` else 0 end))) AS `net_revenue`,count(distinct `b`.`id`) AS `num_bookings` from (`payments` `p` join `bookings` `b` on((`b`.`id` = `p`.`booking_id`))) where (`p`.`status` = 'SUCCESS') group by date_format(`p`.`processed_at`,'%Y-%m') order by `month` desc;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
