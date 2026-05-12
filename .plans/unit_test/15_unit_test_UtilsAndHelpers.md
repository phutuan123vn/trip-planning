# Unit Test Plan: Utility & Helper Classes ✅ TEST PASSED

---

## 1. FilterBuilderHelperTest

**Source File:** `utils/FilterBuilderHelper.java`  
**Test File:** `test/java/com/kms/tripplanning/utils/FilterBuilderHelperTest.java`  
**Type:** Unit Test (plain JUnit)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `addFilter_withVarargs_shouldAddToMap` | Given key + varargs values → `build()` contains the entry | Single value |
| 2 | `addFilter_withMultipleValues_shouldAddAll` | Given key + multiple values → map entry contains all values | Multiple values |
| 3 | `addFilter_withNullValues_shouldNotAdd` | Given null values → map should not contain the key | Null guard |
| 4 | `addFilter_withEmptyVarargs_shouldNotAdd` | Given empty varargs → map should not contain the key | Empty guard |
| 5 | `addFilter_withStringList_shouldAddToMap` | Given key + `List<String>` → `build()` contains the entry | List overload |
| 6 | `addFilter_withEmptyList_shouldNotAdd` | Given empty list → map should not contain the key | Empty list guard |
| 7 | `addFilter_withNullList_shouldNotAdd` | Given null list → map should not contain the key | Null list guard |
| 8 | `removeFilter_shouldRemoveKey` | After adding a filter, `removeFilter(key)` → key no longer in map | Remove filter |
| 9 | `build_shouldReturnCurrentState` | Multiple adds → `build()` returns map with all entries | Build result |
| 10 | `buildSort_shouldReturnAscSort_byDefault` | Given sortBy="name", sortDirection=null → returns `Sort.by(ASC, "name")` | Default ASC |
| 11 | `buildSort_shouldReturnDescSort` | Given sortBy="name", sortDirection="desc" → returns `Sort.by(DESC, "name")` | DESC sort |
| 12 | `buildSort_shouldReturnUnsorted_whenSortByNull` | Given sortBy=null → returns `Sort.unsorted()` | No sort |
| 13 | `buildSort_shouldReturnUnsorted_whenSortByEmpty` | Given sortBy="" → returns `Sort.unsorted()` | Empty sort |

**Total: 13 test cases**

---

## 2. SecurityUtilsTest

**Source File:** `utils/SecurityUtils.java`  
**Test File:** `test/java/com/kms/tripplanning/utils/SecurityUtilsTest.java`  
**Type:** Unit Test (SecurityContextHolder manipulation)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `getCurrentUser_shouldReturnAuthUserDetails_whenAuthenticated` | Given SecurityContext with `AuthUserDetails` principal → returns it | Authenticated |
| 2 | `getCurrentUser_shouldReturnNull_whenNoAuthentication` | Given SecurityContext with null authentication → returns null | No auth |
| 3 | `getCurrentUser_shouldReturnNull_whenNotAuthenticated` | Given authentication that is not authenticated → returns null | Not authenticated |
| 4 | `getCurrentUser_shouldReturnNull_whenPrincipalNotAuthUserDetails` | Given authentication with a different principal type → returns null | Wrong principal type |

**Total: 4 test cases**

---

## 3. AuditMixinTest

**Source File:** `utils/AuditMixin.java`  
**Test File:** `test/java/com/kms/tripplanning/utils/AuditMixinTest.java`  
**Type:** Unit Test (plain JUnit)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `isDeleted_shouldReturnFalse_whenDeletedAtIsNull` | `deletedAt=null` → returns false | Not deleted |
| 2 | `isDeleted_shouldReturnTrue_whenDeletedAtIsSet` | `deletedAt` is set → returns true | Deleted |
| 3 | `markDeleted_shouldSetDeletedAt` | After `markDeleted()` → `deletedAt` is not null and `isDeleted()` returns true | Mark deleted |

**Total: 3 test cases**

---

## 4. AuditMixinListenerTest

**Source File:** `utils/AuditMixinListener.java`  
**Test File:** `test/java/com/kms/tripplanning/utils/AuditMixinListenerTest.java`  
**Type:** Unit Test (SecurityContextHolder manipulation)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `logUserBefore_shouldSetCreatedBy_onNewEntity` | Given entity with `createdBy=null` → sets `createdBy` to current user's ID | New entity |
| 2 | `logUserBefore_shouldSetUpdatedBy_onExistingEntity` | Given entity with `createdBy` already set (not deleted) → sets `updatedBy` | Existing entity |
| 3 | `logUserBefore_shouldSetDeletedBy_onDeletedEntity` | Given entity with `deletedAt` set and `deletedBy=null` → sets `deletedBy` | Deleted entity |
| 4 | `logUserBefore_shouldSkip_whenNotAuditMixin` | Given non-AuditMixin entity → does nothing | Non-audit entity |
| 5 | `logUserBefore_shouldSkip_whenNoCurrentUser` | Given no authenticated user → does nothing | No auth context |

**Total: 5 test cases**

---

## 5. GenericFilterFactoryTest

**Source File:** `utils/GenericFilterFactory.java`  
**Test File:** `test/java/com/kms/tripplanning/utils/GenericFilterFactoryTest.java`  
**Type:** Unit Test (Mockito for EntityManager)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `create_shouldReturnDestinationFilterRepo_forDestinationClass` | Given `Destination.class` → returns `DestinationFilterRepositoryImpl` | Destination entity |
| 2 | `create_shouldReturnGenericFilterRepo_forOtherClasses` | Given `Category.class` or `Trip.class` → returns `GenericFilterRepositoryImpl` | Other entities |

**Total: 2 test cases**

---

## 6. UserCreatedListenerTest

**Source File:** `listener/UserCreatedListener.java`  
**Test File:** `test/java/com/kms/tripplanning/listener/UserCreatedListenerTest.java`  
**Type:** Unit Test (Mockito)  
**Mocked Dependencies:** `EmailService`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `handleUserCreatedEvent_shouldSendVerificationEmail` | Given `UserCreatedEvent` with email and token → calls `emailService.sendVerificationEmail(email, token)` | Happy path |
| 2 | `handleUserCreatedEvent_shouldPassCorrectParameters` | Verify the email and token from the event are passed to the email service | Parameter correctness |

**Total: 2 test cases**

---

## 7. ApiResponseTest

**Source File:** `dto/ApiResponse.java`  
**Test File:** `test/java/com/kms/tripplanning/dto/ApiResponseTest.java`  
**Type:** Unit Test (plain JUnit)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `success_withList_shouldWrapDataWithDefaultPagination` | Given a non-empty list → wraps with data + pagination (page=1, totalElements=list.size) | List wrapping |
| 2 | `success_withEmptyList_shouldReturnEmptyDataWithZeroPagination` | Given empty list → data is empty, pagination size=0 | Empty list |
| 3 | `success_withListAndPagination_shouldUseProvidedPagination` | Given list + custom PaginationResponse → uses provided pagination | Custom pagination |
| 4 | `success_withSingleObject_shouldWrapInList` | Given a single object → wraps in `List.of(object)` | Single object |
| 5 | `success_withPage_shouldExtractContentAndPagination` | Given a Spring `Page` → extracts content and builds pagination from page metadata | Page wrapping |
| 6 | `error_shouldBuildErrorResponse` | Given message, code, details → creates ErrorResponse with all fields | Error response |

**Total: 6 test cases**

---

## 8. ResponseCategoryDetailTest

**Source File:** `dto/category/ResponseCategory.java`  
**Test File:** `test/java/com/kms/tripplanning/dto/category/ResponseCategoryTest.java`  
**Type:** Unit Test (plain JUnit)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `from_shouldMapIdAndName` | Given a Category entity → `CategoryDetail.from()` maps id (as string) and name | Mapping |

**Total: 1 test case**

---

## 9. ResponseDestinationDetailTest

**Source File:** `dto/destination/ResponseDestination.java`  
**Test File:** `test/java/com/kms/tripplanning/dto/destination/ResponseDestinationTest.java`  
**Type:** Unit Test (plain JUnit)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `from_shouldMapAllFields` | Given a Destination entity with categories → maps id, name, city, country, rating, latitude, longitude, thumbnailUrl | Full mapping |
| 2 | `from_shouldMapCategoriesToCategoryDetails` | Given Destination with categories → maps each category using `CategoryDetail.from()` | Category mapping |

**Total: 2 test cases**

---

## 10. ResponseTripDetailTest

**Source File:** `dto/trip/ResponseTrip.java`  
**Test File:** `test/java/com/kms/tripplanning/dto/trip/ResponseTripTest.java`  
**Type:** Unit Test (plain JUnit)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `from_shouldMapAllFields` | Given a Trip entity with destinations → maps id, name, startDate, endDate | Full mapping |
| 2 | `from_shouldMapDestinationsToDestinationDetails` | Given Trip with destinations → maps each using `DestinationDetail.from()` | Destination mapping |

**Total: 2 test cases**

---

**Grand Total for this file: 13 + 4 + 3 + 5 + 2 + 2 + 6 + 1 + 2 + 2 = 40 test cases**
