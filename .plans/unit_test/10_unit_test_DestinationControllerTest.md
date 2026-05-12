# Unit Test Plan: DestinationControllerTest ✅ TEST PASSED (9/9)

**Source File:** `controller/DestinationController.java`  
**Test File:** `test/java/com/kms/tripplanning/controller/DestinationControllerTest.java`  
**Type:** WebMvcTest (MockMvc + Mockito)  
**Mocked Dependencies:** `DestinationService`, `JwtService` (for security filter)

---

## Test Cases

### 1. `POST /api/destinations/list`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `list_shouldReturn200_withPageOfDestinations` | Given valid PaginationRequest → returns 200 with `Page<DestinationDetail>` | Happy path |
| 2 | `list_shouldReturn401_whenNotAuthenticated` | No authentication → returns 401 | Unauthenticated |

### 2. `POST /api/destinations/{destinationId}` (update)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 3 | `update_shouldReturn200_withUpdatedDestination` | Given valid UUID + DestinationUpdate body → returns updated detail | Happy path |
| 4 | `update_shouldReturn404_whenNotFound` | Given UUID with no match → service throws NotFoundException → 404 | Not found |

### 3. `POST /api/destinations/` (create)

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 5 | `create_shouldReturn200_withDestinationDetail` | Given valid DestinationCreate body → returns created detail | Happy path |

### 4. `DELETE /api/destinations/{destinationId}`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 6 | `delete_shouldReturn200_whenFound` | Given valid UUID → calls deleteDestination → 200 | Happy path |
| 7 | `delete_shouldReturn404_whenNotFound` | Given UUID with no match → service throws NotFoundException → 404 | Not found |

### 5. `GET /api/destinations/{destinationId}`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 8 | `getById_shouldReturn200_withDestinationDetail` | Given valid UUID → returns detail | Happy path |
| 9 | `getById_shouldReturn404_whenNotFound` | Given UUID with no match → 404 | Not found |

---

**Total: 9 test cases**
