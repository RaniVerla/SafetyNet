# Fire Station Endpoint Implementation Summary

## Overview
A separate controller has been successfully created to handle the requirement for querying residents covered by fire stations.

## Endpoint Details
- **URL**: `http://localhost:8080/firestation?stationNumber=<station_number>`
- **Method**: GET
- **Query Parameter**: `stationNumber` (String)
- **Response Format**: JSON

## Example Request
```
GET http://localhost:8080/firestation?stationNumber=3
```

## Example Response
```json
{
  "residents": [
    {
      "firstName": "Jacob",
      "lastName": "Boyd",
      "address": "1509 Culver St",
      "phone": "841-874-6513"
    },
    {
      "firstName": "John",
      "lastName": "Boyd",
      "address": "1509 Culver St",
      "phone": "841-874-6512"
    }
  ],
  "adultCount": 1,
  "childCount": 1
}
```

## Components Created/Modified

### 1. New Model Class: FireStationResidents
**File**: `src/main/java/net/example/safetynet/model/FireStationResidents.java`

Contains:
- `residents`: List of resident information
- `adultCount`: Number of adults (age > 18)
- `childCount`: Number of children (age ≤ 18)
- **Nested Class**: `ResidentInfo` with fields:
  - `firstName`: Resident's first name
  - `lastName`: Resident's last name
  - `address`: Resident's address
  - `phone`: Resident's phone number

### 2. New Controller: FireStationQueryController
**File**: `src/main/java/net/example/safetynet/controller/FireStationQueryController.java`

- Maps to `/firestation` endpoint
- Handles GET requests with `stationNumber` query parameter
- Delegates to `FireStationService.getResidentsByStationNumber()`

### 3. Enhanced Service: FireStationService
**File**: `src/main/java/net/example/safetynet/service/FireStationService.java`

**New Methods**:

#### `getResidentsByStationNumber(String stationNumber)`
- Retrieves all fire stations from firestation.json
- Gets all persons from person.json
- Gets all medical records from medicalrecord.json
- Filters persons by addresses covered by the specified station
- Calculates adult/child counts based on medical records
- Returns `FireStationResidents` object with residents list and counts

#### `isChild(Person person, List<Medicalrecord> medicalRecordList)`
- Helper method to determine if a person is a child (≤ 18 years old)
- Parses birthdate from medical records in MM/dd/yyyy format
- Uses Java 8 Time API to calculate age from birthdate
- Returns true if age ≤ 18, false otherwise

**Additional File Paths**:
- Added `personFilePath` pointing to `src/main/resources/safetynet/person.json`
- Added `medicalRecordFilePath` pointing to `src/main/resources/safetynet/medicalrecord.json`

## Features

✅ **Returns residents covered by specified fire station**
- Uses address-based mapping from firestation.json
- Retrieves matching persons from person.json

✅ **Includes required resident information**
- First Name
- Last Name
- Address
- Phone Number

✅ **Calculates adult and child counts**
- Adults: Age > 18
- Children: Age ≤ 18
- Age calculated from birthdate in medical records

✅ **Error Handling**
- Graceful handling of missing medical records
- Logging for debugging
- Returns empty results if station not found

## Data Flow

1. Request arrives at `/firestation?stationNumber=3`
2. `FireStationQueryController.getResidentsByStation()` receives request
3. Calls `FireStationService.getResidentsByStationNumber()`
4. Service reads three JSON files:
   - firestation.json → Find addresses for station
   - person.json → Get all persons
   - medicalrecord.json → Get birthdates for age calculation
5. Filters persons by addresses covered by station
6. For each resident, determines if child or adult
7. Returns `FireStationResidents` with residents list and counts
8. Controller returns JSON response

## Testing

To test the endpoint:

```powershell
# Start the application
.\gradlew bootRun

# In another terminal, test the endpoint
Invoke-WebRequest -Uri "http://localhost:8080/firestation?stationNumber=3"

# Try different station numbers
Invoke-WebRequest -Uri "http://localhost:8080/firestation?stationNumber=1"
```

## Notes

- Uses separate controller (`FireStationQueryController`) as required
- Leverages existing services and utilities
- Maintains consistency with project patterns (Lombok, logging, etc.)
- Age calculation uses MM/dd/yyyy date format from data
- Returns sensible defaults (empty list, 0 counts) if no data found

