# Unit Test Plan: TripServiceImplTest ✅ TEST PASSED (17/17)

**Source File:** `services/Impl/TripServiceImpl.java`  
**Test File:** `test/java/com/kms/tripplanning/services/Impl/TripServiceImplTest.java`  
**Type:** Unit Test (Mockito + SecurityContext mocking)  
**Mocked Dependencies:** `TripRepository`, `DestinationRepository`  
**Static Mocks:** `SecurityUtils.getCurrentUser()` (for `searchTrips`)

---

## Test Cases

### 1. `createTrip`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `createTrip_shouldSaveAndReturnTripDetail_withoutDestinations` | Given TripCreate with no destinationIds → saves trip, returns TripDetail | No destinations |
| 2 | `createTrip_shouldSaveAndReturnTripDetail_withDestinations` | Given TripCreate with valid destinationIds → finds destinations, sets them, saves | With destinations |
| 3 | `createTrip_shouldThrowNotFoundException_whenDestinationIdsInvalid` | Given destinationIds where some don't exist → `findAllById` returns fewer → throws `NotFoundException` | Invalid destination IDs |
| 4 | `createTrip_shouldMapNameAndDatesCorrectly` | Verify name, startDate, endDate are mapped from request to entity | Field mapping |

### 2. `updateTrip`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 5 | `updateTrip_shouldUpdateAllProvidedFields` | Given TripUpdate with all fields set → updates entity | Full update |
| 6 | `updateTrip_shouldKeepExistingValues_whenFieldsAreNull` | Given TripUpdate with null name/dates → keeps existing values | Partial update |
| 7 | `updateTrip_shouldUpdateDestinations_whenDestinationIdsProvided` | Given valid destinationIds → finds destinations, sets them | Destination update |
| 8 | `updateTrip_shouldThrowNotFoundException_whenDestinationIdsInvalid` | Given destinationIds where some don't exist → throws `NotFoundException` | Invalid destination IDs |
| 9 | `updateTrip_shouldThrowNotFoundException_whenTripNotFound` | Given UUID with no match → throws `NotFoundException` | Not found |

### 3. `deleteTrip`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 10 | `deleteTrip_shouldMarkDeletedAndSave_whenFound` | Given valid UUID → finds trip, calls `markDeleted()`, saves | Happy path (soft delete) |
| 11 | `deleteTrip_shouldThrowNotFoundException_whenNotFound` | Given UUID with no match → throws `NotFoundException` | Not found |

### 4. `getTripById`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 12 | `getTripById_shouldReturnTripDetail_whenFound` | Given valid UUID → builds filter with id, calls `search()` with loadRelations `["destinations", "destinations.categories"]`, returns `TripDetail` | Happy path |
| 13 | `getTripById_shouldThrowNotFoundException_whenNotFound` | Given UUID with no match → `search` returns empty → throws `NotFoundException` | Not found |

### 5. `searchTrips`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 14 | `searchTrips_shouldReturnPageOfTripDetails` | Given valid filters/sort/page/size → adds `createdBy` filter from SecurityUtils → returns `Page<TripDetail>` | Happy path |
| 15 | `searchTrips_shouldAddCreatedByFilterFromCurrentUser` | Verify that `filterBuilder.addFilter("createdBy", userId)` is called with the authenticated user's ID | Security context integration |
| 16 | `searchTrips_shouldLoadDestinationsAndCategoriesRelations` | Verify `search()` is called with `List.of("destinations", "destinations.categories")` | Eager loading |
| 17 | `searchTrips_shouldReturnEmptyPage_whenNoResults` | Given filters matching nothing → returns empty page | No results |

---

**Total: 17 test cases**
