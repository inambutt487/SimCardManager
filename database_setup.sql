-- Database: directdevhub_task
-- Table: simcards

-- Create the simcards table
CREATE TABLE IF NOT EXISTS `simcards` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `slot_number` int(11) NOT NULL,
  `carrier_name` varchar(255) DEFAULT NULL,
  `sim_state` varchar(50) NOT NULL DEFAULT 'READY',
  `network_type` varchar(50) DEFAULT NULL,
  `iccid` varchar(50) DEFAULT NULL,
  `imsi` varchar(50) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `country_code` varchar(10) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_slot_number` (`slot_number`),
  KEY `idx_carrier_name` (`carrier_name`),
  KEY `idx_sim_state` (`sim_state`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert dummy data for simcards
INSERT INTO `simcards` (`slot_number`, `carrier_name`, `sim_state`, `network_type`, `iccid`, `imsi`, `phone_number`, `country_code`, `is_active`) VALUES
(0, 'Verizon Wireless', 'READY', 'LTE', '89014103211118510720', '310004123456789', '+1234567890', 'US', 1),
(1, 'AT&T Mobility', 'READY', '5G', '89014103211118510721', '310004123456790', '+1234567891', 'US', 0),
(0, 'T-Mobile US', 'READY', '4G', '89014103211118510722', '310004123456791', '+1234567892', 'US', 0),
(1, 'Sprint Corporation', 'ABSENT', '3G', '89014103211118510723', '310004123456792', '+1234567893', 'US', 0),
(0, 'Cricket Wireless', 'PIN_REQUIRED', '4G', '89014103211118510724', '310004123456793', '+1234567894', 'US', 0),
(1, 'Metro by T-Mobile', 'READY', '4G', '89014103211118510725', '310004123456794', '+1234567895', 'US', 0),
(0, 'Boost Mobile', 'READY', '3G', '89014103211118510726', '310004123456795', '+1234567896', 'US', 0),
(1, 'Virgin Mobile', 'NETWORK_LOCKED', '4G', '89014103211118510727', '310004123456796', '+1234567897', 'US', 0),
(0, 'Straight Talk', 'READY', '4G', '89014103211118510728', '310004123456797', '+1234567898', 'US', 0),
(1, 'Mint Mobile', 'READY', '4G', '89014103211118510729', '310004123456798', '+1234567899', 'US', 0);

-- Create telecom_plans table
CREATE TABLE IF NOT EXISTS `telecom_plans` (
  `id` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `data` varchar(100) NOT NULL,
  `carrier_name` varchar(255) DEFAULT NULL,
  `plan_type` varchar(50) DEFAULT 'POSTPAID',
  `contract_length` int(11) DEFAULT NULL,
  `features` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_carrier_name` (`carrier_name`),
  KEY `idx_price` (`price`),
  KEY `idx_plan_type` (`plan_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert dummy data for telecom plans
INSERT INTO `telecom_plans` (`id`, `name`, `price`, `data`, `carrier_name`, `plan_type`, `contract_length`, `features`) VALUES
('plan_1', 'Basic Plan', 29.99, '5GB', 'Verizon Wireless', 'POSTPAID', 24, 'Unlimited talk, Unlimited text, 5GB data'),
('plan_2', 'Standard Plan', 49.99, '15GB', 'Verizon Wireless', 'POSTPAID', 24, 'Unlimited talk, Unlimited text, 15GB data, Mobile hotspot'),
('plan_3', 'Premium Plan', 79.99, 'Unlimited', 'Verizon Wireless', 'POSTPAID', 24, 'Unlimited talk, Unlimited text, Unlimited data, Mobile hotspot, 5G access'),
('plan_4', 'Student Plan', 19.99, '3GB', 'AT&T Mobility', 'POSTPAID', 12, 'Unlimited talk, Unlimited text, 3GB data'),
('plan_5', 'Family Plan', 99.99, '50GB', 'AT&T Mobility', 'POSTPAID', 24, 'Unlimited talk, Unlimited text, 50GB shared data, Mobile hotspot'),
('plan_6', 'Prepaid Basic', 25.00, '2GB', 'T-Mobile US', 'PREPAID', NULL, 'Unlimited talk, Unlimited text, 2GB data'),
('plan_7', 'Prepaid Unlimited', 50.00, 'Unlimited', 'T-Mobile US', 'PREPAID', NULL, 'Unlimited talk, Unlimited text, Unlimited data'),
('plan_8', 'Senior Plan', 35.00, '8GB', 'Sprint Corporation', 'POSTPAID', 24, 'Unlimited talk, Unlimited text, 8GB data'),
('plan_9', 'Business Plan', 89.99, 'Unlimited', 'Cricket Wireless', 'POSTPAID', 24, 'Unlimited talk, Unlimited text, Unlimited data, Business features'),
('plan_10', 'Budget Plan', 15.00, '1GB', 'Metro by T-Mobile', 'PREPAID', NULL, 'Unlimited talk, Unlimited text, 1GB data'); 