# SIM Card Manager - Android Application

## 📱 Overview

A comprehensive Android application for managing SIM cards, telecom plans, and SIM switching operations. Built with modern Android development practices and MVVM architecture.

## 🏗️ Architecture

### MVVM (Model-View-ViewModel) Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        VIEW LAYER                              │
├─────────────────────────────────────────────────────────────────┤
│  Activities/Fragments                                          │
│  • MainActivity                                               │
│  • TelecomPlansActivity                                       │
│  • SimSwitchDialogFragment                                    │
│                                                                │
│  Responsibilities:                                             │
│  • UI rendering and user interaction                          │
│  • Lifecycle management                                       │
│  • Permission handling                                        │
│  • Observing ViewModel LiveData                               │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     VIEWMODEL LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│  ViewModels                                                   │
│  • SimCardViewModel                                           │
│  • TelecomPlansViewModel                                      │
│                                                                │
│  Responsibilities:                                             │
│  • Manages UI state and business logic                        │
│  • Handles data operations through Repository                 │
│  • Provides LiveData for reactive UI updates                  │
│  • Survives configuration changes                             │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       MODEL LAYER                              │
├─────────────────────────────────────────────────────────────────┤
│  Repositories                                                 │
│  • TelephonyRepository                                        │
│  • SimSwitchRepository                                        │
│  • TelecomRepository                                          │
│                                                                │
│  Data Sources                                                 │
│  • Room Database (Local)                                      │
│  • Retrofit API (Remote)                                      │
│  • Android TelephonyManager (System)                          │
│                                                                │
│  Responsibilities:                                             │
│  • Data operations and business logic                         │
│  • API communication                                          │
│  • Database management                                        │
│  • Offline-first approach                                     │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow

```
User Action → View → ViewModel → Repository → Data Source
     ↑                                                      │
     └────────── LiveData ←───────────←───────────←─────────┘
```

## 🚀 Features

### ✅ Core Features
- **SIM Card Management**: Real-time SIM card information retrieval
- **Multi-SIM Support**: Detection and management of multiple SIM slots
- **Telecom Plans**: Browse and manage telecom plans from API
- **SIM Switching**: Manual SIM switching with background sync
- **Offline Support**: Works without internet connection
- **Permission Handling**: Comprehensive permission management

### ✅ Technical Features
- **MVVM Architecture**: Clean separation of concerns
- **Room Database**: Local caching and offline support
- **Retrofit**: API communication with offline-first approach
- **WorkManager**: Battery-efficient background tasks
- **Coroutines**: Asynchronous operations
- **ViewBinding**: Type-safe view access
- **LiveData**: Reactive UI updates

## 📋 Prerequisites

### Android Development
- Android Studio Arctic Fox or later
- Android SDK 24+ (API Level 24)
- Kotlin 1.8+
- Gradle 7.0+

### Backend Requirements
- PHP 7.4+
- MySQL 5.7+
- Web server (Apache/Nginx)

## 🛠️ Setup Instructions

### 1. Android Application Setup

```bash
# Clone the repository
git clone <repository-url>
cd SimCardManager

# Open in Android Studio
# Sync Gradle files
# Build the project
```

### 2. Database Setup

```sql
-- Create database
CREATE DATABASE directdevhub_task;

-- Import schema and data
mysql -u directdevhub_task -p directdevhub_task < database_setup.sql
```

### 3. API Setup

```bash
# Upload PHP files to web server
# Update API base URL in TelecomRetrofitClient.kt
private const val BASE_URL = "https://your-domain.com/task/"
```

### 4. Configuration

#### Database Configuration
Update database credentials in PHP files:
```php
$host = 'localhost';
$dbname = 'directdevhub_task';
$username = 'your_username';
$password = 'your_password';
```

#### API Configuration
Update API base URL in Android app:
```kotlin
// app/src/main/java/com/ultranet/simcardmanager/data/api/TelecomRetrofitClient.kt
private const val BASE_URL = "https://your-domain.com/task/"
```

## 📚 API Documentation

### Base URL
```
https://your-domain.com/task/
```

### SIM Cards API

#### Get All SIM Cards
```http
GET /simcards.php
```

**Response:**
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
```http
GET /simcards.php/{id}
```

#### Create SIM Card
```http
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

### Telecom Plans API

#### Get All Telecom Plans
```http
GET /telecom_plans.php
```

#### Get Plans by Carrier
```http
GET /telecom_plans.php?carrier=Verizon
```

#### Get Plan by ID
```http
GET /telecom_plans.php/{id}
```

## 🔧 Telecom-Specific Challenges & Solutions

### 1. Android Version Compatibility

**Challenge**: Different Android versions have varying telephony APIs.

**Solution**:
```kotlin
// Handle different Android versions
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
    // Use SubscriptionManager for multi-SIM support
    val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
} else {
    // Fallback for older Android versions
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE)
}
```

### 2. Permission Management

**Challenge**: Telephony features require specific permissions.

**Solution**:
```kotlin
// Comprehensive permission handling
val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.READ_PHONE_NUMBERS
)

// Check permissions before operations
if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
    // Proceed with telephony operations
} else {
    // Provide fallback data
}
```

### 3. Multi-SIM Support

**Challenge**: Different devices support different numbers of SIM slots.

**Solution**:
```kotlin
// Detect multi-SIM support
fun isMultiSimSupported(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_SUBSCRIPTION)
    } else {
        false // Assume single SIM for older devices
    }
}
```

### 4. Network Type Detection

**Challenge**: Different network types require different handling.

**Solution**:
```kotlin
// Map network types to human-readable strings
private fun getNetworkTypeString(networkType: Int): String {
    return when (networkType) {
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_NR -> "5G"
        TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
        else -> "UNKNOWN"
    }
}
```

### 5. Carrier-Specific Operations

**Challenge**: Different carriers have different APIs and requirements.

**Solution**:
```kotlin
// Carrier-specific sync logic
when {
    carrierName.contains("AT&T", ignoreCase = true) -> {
        // AT&T specific sync logic
    }
    carrierName.contains("Verizon", ignoreCase = true) -> {
        // Verizon specific sync logic
    }
    else -> {
        // Generic sync logic
    }
}
```

## ⚡ Performance Optimization

### Battery Efficiency

#### 1. WorkManager for Background Tasks
```kotlin
// Battery-efficient background processing
val workRequest = OneTimeWorkRequestBuilder<BalanceSyncWorker>()
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()
```

#### 2. Coroutines for Async Operations
```kotlin
// Non-blocking operations
viewModelScope.launch {
    val simCards = telephonyRepository.getSimCardInfo()
    _simCards.value = simCards
}
```

#### 3. Efficient Database Operations
```kotlin
// Room database with efficient queries
@Query("SELECT * FROM sim_switch_events ORDER BY timestamp DESC")
fun getAllSimSwitchEvents(): Flow<List<SimSwitchEvent>>
```

### Memory Management

#### 1. Lifecycle-Aware Observers
```kotlin
// Prevents memory leaks
viewModel.simCards.observe(this) { simCards ->
    // UI updates
}
```

#### 2. ViewBinding for Efficient View Access
```kotlin
// Type-safe view access
private lateinit var binding: ActivityMainBinding
binding.tvCarrierName.text = simCard.carrierName
```

#### 3. Efficient Data Structures
```kotlin
// Use data classes for efficient memory usage
data class SimCardInfo(
    val slotNumber: Int,
    val carrierName: String?,
    val simState: String,
    val networkType: String?
)
```

## 🧪 Testing

### Unit Tests
```bash
# Run unit tests
./gradlew test
```

### Instrumented Tests
```bash
# Run instrumented tests
./gradlew connectedAndroidTest
```

### API Testing
```bash
# Test API endpoints
curl https://your-domain.com/task/simcards.php
curl https://your-domain.com/task/telecom_plans.php
```

## 🔒 Security Considerations

### 1. Permission Handling
- Runtime permission requests
- Graceful fallback for denied permissions
- User-friendly permission rationale

### 2. API Security
- Input validation and sanitization
- Prepared statements to prevent SQL injection
- CORS headers for cross-origin requests

### 3. Data Protection
- Local database encryption
- Secure credential storage
- Network security (HTTPS)

## 📱 Device Compatibility

### Supported Android Versions
- **Minimum**: Android 7.0 (API 24)
- **Target**: Android 14 (API 34)
- **Recommended**: Android 8.0+ (API 26+)

### Device Requirements
- **Telephony Support**: Required
- **Multi-SIM**: Optional (graceful degradation)
- **Network**: Required for full functionality

## 🚀 Deployment

### Android App
1. Build release APK:
```bash
./gradlew assembleRelease
```

2. Sign the APK with your keystore
3. Upload to Google Play Store or distribute directly

### Backend API
1. Upload PHP files to web server
2. Configure database connection
3. Set up SSL certificate for HTTPS
4. Configure CORS headers

## 📊 Monitoring & Analytics

### Performance Monitoring
- WorkManager execution tracking
- Database operation monitoring
- API call performance metrics

### Error Tracking
- Crash reporting integration
- Network error logging
- Permission denial tracking

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- Create an issue on GitHub
- Contact: support@example.com
- Documentation: [Wiki Link]

---

**Note**: This application is designed for educational and demonstration purposes. Real-world deployment requires additional security measures and carrier-specific API integrations. 