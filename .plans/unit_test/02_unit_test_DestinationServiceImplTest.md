# Unit Test Plan: DestinationServiceImplTest ✅ TEST PASSED (17/17)

**Source File:** `services/Impl/DestinationServiceImpl.java`  
**Test File:** `test/java/com/kms/tripplanning/services/Impl/DestinationServiceImplTest.java`  
**Type:** Unit Test (Mockito)  
**Mocked Dependencies:** `DestinationRepository`, `CategoryRepository`

---

## Test Cases

### 1. `listDestination`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `listDestination_shouldReturnPageOfDestinationDetails` | Given valid filters, sort, page, size → returns `Page<DestinationDetail>` with correct data | Happy path |
| 2 | `listDestination_shouldReturnEmptyPage_whenNoResults` | Given filters that match nothing → returns empty page | No matching data |
| 3 | `listDestination_shouldPassCategoriesAsLoadRelation` | Verify `search()` is called with `List.of("categories")` as loadRelations | Eager loading |
| 4 | `listDestination_shouldApplySortCorrectly` | Given sortBy="rating", sortDirection="desc" → correct Sort applied to PageRequest | Sort handling |

### 2. `getDestinationById`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 5 | `getDestinationById_shouldReturnDestinationDetail_whenFound` | Given valid UUID → repository returns Destination → returns `DestinationDetail` with all fields mapped | Happy path |
| 6 | `getDestinationById_shouldThrowNotFoundException_whenNotFound` | Given UUID with no match → throws `NotFoundException` | Not found |

### 3. `createDestination`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 7 | `createDestination_shouldSaveAndReturnDetail_withoutCategories` | Given DestinationCreate with no categoryIds → saves destination, returns detail | No categories |
| 8 | `createDestination_shouldSaveAndReturnDetail_withCategories` | Given DestinationCreate with valid categoryIds → finds categories, sets them, saves | With categories |
| 9 | `createDestination_shouldThrowNotFoundException_whenCategoryIdsInvalid` | Given categoryIds where some don't exist → `findAllById` returns fewer → throws `NotFoundException` | Invalid category IDs |
| 10 | `createDestination_shouldMapAllFieldsCorrectly` | Verify name, city, country, rating, latitude, longitude, thumbnailUrl are all mapped from request to entity | Field mapping |

### 4. `deleteDestination`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 11 | `deleteDestination_shouldDeleteDestination_whenFound` | Given valid UUID → finds destination → calls `delete()` | Happy path |
| 12 | `deleteDestination_shouldThrowNotFoundException_whenNotFound` | Given UUID with no match → throws `NotFoundException` | Not found |

### 5. `updateDestination`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 13 | `updateDestination_shouldUpdateAllProvidedFields` | Given DestinationUpdate with all fields set → updates entity with new values | Full update |
| 14 | `updateDestination_shouldKeepExistingValues_whenFieldsAreNull` | Given DestinationUpdate with some null fields → keeps existing values for nulls | Partial update |
| 15 | `updateDestination_shouldUpdateCategories_whenCategoryIdsProvided` | Given valid categoryIds → finds categories, sets them on destination | Category update |
| 16 | `updateDestination_shouldThrowNotFoundException_whenCategoryIdsInvalid` | Given categoryIds where some don't exist → throws `NotFoundException` | Invalid category IDs |
| 17 | `updateDestination_shouldThrowNotFoundException_whenDestinationNotFound` | Given UUID with no match → throws `NotFoundException` | Not found |

---

**Total: 17 test cases**
