# API Setup Instructions

## Current Status
- ✅ Base URL updated: `https://careconnectportal.directdevhub.com/task/`
- ✅ Android API interfaces updated to use `.php` endpoints
- ⚠️ PHP files need to be updated on server

## Files to Upload

### 1. Update telecom_plans.php
Replace the current `telecom_plans.php` on the server with the updated version in `telecom_plans_updated.php`.

**Key Changes:**
- Added fallback logic for any GET request to return all plans
- Improved endpoint detection
- Added debug logging
- Better error handling

### 2. Update simcards.php
The `simcards.php` file also needs the same updates. The updated version is in the main `simcards.php` file.

## Testing the API

### Test Current Endpoint
```bash
curl -X GET "https://careconnectportal.directdevhub.com/task/telecom_plans.php"
```

### Expected Response (after upload)
```json
{
  "success": true,
  "data": [
    {
      "id": "plan_1",
      "name": "Basic Plan",
      "price": "29.99",
      "data": "5GB",
      "carrier_name": "Verizon Wireless",
      "plan_type": "POSTPAID",
      "contract_length": 24,
      "features": "Unlimited talk, Unlimited text, 5GB data",
      "created_at": "2024-01-01 00:00:00",
      "updated_at": "2024-01-01 00:00:00"
    }
  ],
  "count": 10
}
```

### Test with Carrier Filter
```bash
curl -X GET "https://careconnectportal.directdevhub.com/task/telecom_plans.php?carrier=Verizon"
```

### Test SIM Cards API
```bash
curl -X GET "https://careconnectportal.directdevhub.com/task/simcards.php"
```

## Android App Configuration

### Updated API Endpoints
- **Base URL**: `https://careconnectportal.directdevhub.com/task/`
- **SIM Cards**: `simcards.php`
- **Telecom Plans**: `telecom_plans.php`

### Files Updated
1. `RetrofitClient.kt` - Updated base URL
2. `TelecomRetrofitClient.kt` - Updated base URL
3. `ApiService.kt` - Updated endpoints to use `.php`
4. `TelecomApiService.kt` - Updated endpoints to use `.php`

## Database Setup

### Import Database Schema
```bash
mysql -u directdevhub_task -p directdevhub_task < database_setup.sql
```

### Database Credentials
- **Host**: localhost
- **Database**: directdevhub_task
- **Username**: directdevhub_task
- **Password**: Allah786@2025

## Troubleshooting

### If API returns "Endpoint not found"
1. Check if PHP files are uploaded correctly
2. Verify database connection
3. Check server error logs
4. Test with simple GET request

### If Database connection fails
1. Verify database credentials
2. Check if database exists
3. Ensure MySQL service is running
4. Test connection manually

### If Android app can't connect
1. Check internet permissions
2. Verify base URL is correct
3. Test API endpoints manually
4. Check network security config

## Complete Test Sequence

1. **Test Database Connection**
   ```bash
   mysql -u directdevhub_task -p directdevhub_task -e "SELECT COUNT(*) FROM telecom_plans;"
   ```

2. **Test API Endpoints**
   ```bash
   # Test telecom plans
   curl -X GET "https://careconnectportal.directdevhub.com/task/telecom_plans.php"
   
   # Test SIM cards
   curl -X GET "https://careconnectportal.directdevhub.com/task/simcards.php"
   ```

3. **Test Android App**
   - Build and install the app
   - Grant permissions
   - Check if data loads correctly

## File Upload Instructions

### Via FTP/SFTP
1. Connect to your server
2. Navigate to `/task/` directory
3. Upload `telecom_plans_updated.php` as `telecom_plans.php`
4. Upload updated `simcards.php`

### Via cPanel File Manager
1. Open cPanel File Manager
2. Navigate to `/public_html/task/` or `/task/`
3. Edit existing files or upload new ones
4. Ensure file permissions are correct (644)

## Security Notes
- API includes CORS headers for cross-origin requests
- Input validation and sanitization implemented
- Prepared statements prevent SQL injection
- Error handling with appropriate HTTP status codes 