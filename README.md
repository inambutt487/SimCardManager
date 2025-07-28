# SIM Card Manager - API and Database Setup

## Overview
This project includes a complete SIM Card Manager Android app with PHP API endpoints and MySQL database setup.

## Database Setup

### 1. Database Configuration
- **Database Name**: `directdevhub_task`
- **Username**: `directdevhub_task`
- **Password**: `Allah786@2025`
- **Host**: `localhost`

### 2. Database Tables

#### simcards Table
```sql
CREATE TABLE `simcards` (
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
  PRIMARY KEY (`id`)
);
```

#### telecom_plans Table
```sql
CREATE TABLE `telecom_plans` (
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
  PRIMARY KEY (`id`)
);
```

### 3. Setup Instructions

1. **Import Database Schema**:
   ```bash
   mysql -u directdevhub_task -p directdevhub_task < database_setup.sql
   ```

2. **Upload PHP Files**:
   - Upload `simcards.php` to your web server
   - Upload `telecom_plans.php` to your web server

## API Endpoints

### Base URL
```
http://simcards.directdevhub.com/
```

### SIM Cards API

#### Get All SIM Cards
```
GET /simcards.php
```

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "slot_number": 0,
      "carrier_name": "Verizon Wireless",
      "sim_state": "READY",
      "network_type": "LTE",
      "iccid": "89014103211118510720",
      "imsi": "310004123456789",
      "phone_number": "+1234567890",
      "country_code": "US",
      "is_active": 1,
      "created_at": "2024-01-01 00:00:00",
      "updated_at": "2024-01-01 00:00:00"
    }
  ],
  "count": 10
}
```

#### Get SIM Card by ID
```
GET /simcards.php/{id}
```

#### Create SIM Card
```
POST /simcards.php
Content-Type: application/json

{
  "slot_number": 0,
  "carrier_name": "Verizon Wireless",
  "sim_state": "READY",
  "network_type": "LTE",
  "iccid": "89014103211118510720",
  "imsi": "310004123456789",
  "phone_number": "+1234567890",
  "country_code": "US",
  "is_active": 1
}
```

#### Update SIM Card
```
PUT /simcards.php/{id}
Content-Type: application/json

{
  "slot_number": 0,
  "carrier_name": "AT&T Mobility",
  "sim_state": "READY",
  "network_type": "5G"
}
```

#### Delete SIM Card
```
DELETE /simcards.php/{id}
```

### Telecom Plans API

#### Get All Telecom Plans
```
GET /telecom_plans.php
```

#### Get Plans by Carrier
```
GET /telecom_plans.php?carrier=Verizon
```

#### Get Plan by ID
```
GET /telecom_plans.php/{id}
```

#### Create Telecom Plan
```
POST /telecom_plans.php
Content-Type: application/json

{
  "id": "plan_11",
  "name": "New Plan",
  "price": 39.99,
  "data": "10GB",
  "carrier_name": "Verizon Wireless",
  "plan_type": "POSTPAID",
  "contract_length": 24,
  "features": "Unlimited talk, Unlimited text, 10GB data"
}
```

## Android App Integration

### Update API Base URL
The Android app is configured to use the real API endpoint:
```kotlin
private const val BASE_URL = "http://simcards.directdevhub.com/"
```

### Features
- ✅ Offline-first architecture with Room database
- ✅ Real-time SIM card information
- ✅ Telecom plans management
- ✅ Background data synchronization
- ✅ Modern Material Design UI
- ✅ Pull-to-refresh functionality
- ✅ Error handling and user feedback

## Dummy Data Included

### SIM Cards (10 records)
- Verizon Wireless, AT&T Mobility, T-Mobile US
- Various states: READY, ABSENT, PIN_REQUIRED, NETWORK_LOCKED
- Different network types: LTE, 5G, 4G, 3G

### Telecom Plans (10 records)
- Various carriers and plan types
- Price range: $15.00 - $99.99
- Data options: 1GB to Unlimited
- Plan types: POSTPAID and PREPAID

## Testing

### Test API Endpoints
```bash
# Get all SIM cards
curl http://simcards.directdevhub.com/simcards.php

# Get all telecom plans
curl http://simcards.directdevhub.com/telecom_plans.php

# Get plans by carrier
curl "http://simcards.directdevhub.com/telecom_plans.php?carrier=Verizon"
```

### Android App Testing
1. Build and install the Android app
2. Grant phone state permissions
3. View SIM card information
4. Browse telecom plans
5. Test offline functionality

## Security Notes
- API includes CORS headers for cross-origin requests
- Input validation and sanitization
- Prepared statements to prevent SQL injection
- Error handling with appropriate HTTP status codes

## File Structure
```
├── database_setup.sql          # Database schema and dummy data
├── simcards.php               # SIM cards API endpoint
├── telecom_plans.php          # Telecom plans API endpoint
├── README.md                  # This file
└── app/                       # Android app source code
    ├── src/main/java/
    │   └── com/ultranet/simcardmanager/
    │       ├── data/
    │       │   ├── api/       # Retrofit API interfaces
    │       │   ├── database/  # Room database components
    │       │   └── repository/# Repository classes
    │       ├── domain/
    │       │   ├── models/    # Data models
    │       │   └── usecases/  # Use cases
    │       └── ui/
    │           ├── activities/# Activities
    │           └── adapters/  # RecyclerView adapters
    └── src/main/res/
        └── layout/            # Layout files
``` 