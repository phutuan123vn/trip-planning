# Unit Test Plan: TripControllerTest ✅ TEST PASSED (10/10)

**Source File:** `controller/TripController.java`  
**Test File:** `test/java/com/kms/tripplanning/controller/TripControllerTest.java`  
**Type:** WebMvcTest (MockMvc + Mockito)  
**Mocked Dependencies:** `TripService`, `JwtService` (for security filter)

---

## Test Cases

### 1. `POST /api/trips/list`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `list_shouldReturn200_withPageOfTrips` | Given valid PaginationRequest → returns 200 with `Page<TripDetail>` | Happy path |
| 2 | `list_shouldReturn401_whenNotAuthenticated` | No authentication → returns 401 | Unauthenticated |

### 2. `DELETE /api/trips/{tripId}`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 3 | `deleteTrip_shouldReturn204_whenFound` | Given valid UUID string → calls `tripService.deleteTrip()` → 204 | Happy path |
| 4 | `deleteTrip_shouldReturn404_whenNotFound` | Given UUID with no match → service throws NotFoundException → 404 | Not found |

### 3. `POST /api/trips/` (create)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 5 | `create_shouldReturn200_withTripDetail` | Given valid TripCreate body → returns created TripDetail | Happy path |
| 6 | `create_shouldReturn400_whenNameEmpty` | Given TripCreate with empty name → validation fails → 400 | Validation |

### 4. `POST /api/trips/{tripId}` (update)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 7 | `updateTrip_shouldReturn200_withUpdatedTrip` | Given valid UUID + TripUpdate body → returns updated TripDetail | Happy path |
| 8 | `updateTrip_shouldReturn404_whenNotFound` | Given UUID with no match → service throws NotFoundException → 404 | Not found |

### 5. `GET /api/trips/{tripId}`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 9 | `getTrip_shouldReturn200_withTripDetail` | Given valid UUID → returns TripDetail | Happy path |
| 10 | `getTrip_shouldReturn404_whenNotFound` | Given UUID with no match → 404 | Not found |

---

**Total: 10 test cases**
