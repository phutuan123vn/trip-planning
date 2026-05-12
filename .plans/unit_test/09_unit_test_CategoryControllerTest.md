# Unit Test Plan: CategoryControllerTest ✅ TEST PASSED (9/9)

**Source File:** `controller/CategoryController.java`  
**Test File:** `test/java/com/kms/tripplanning/controller/CategoryControllerTest.java`  
**Type:** WebMvcTest (MockMvc + Mockito)  
**Mocked Dependencies:** `CategoryService`, `JwtService` (for security filter)

---

## Test Cases

### 1. `POST /api/categories/list`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `list_shouldReturn200_withPageOfCategories` | Given valid PaginationRequest → returns 200 with `Page<CategoryDetail>` | Happy path |
| 2 | `list_shouldReturn401_whenNotAuthenticated` | No authentication → returns 401 | Unauthenticated |

### 2. `GET /api/categories/{categoryId}`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 3 | `getById_shouldReturn200_withCategoryDetail` | Given valid UUID → `categoryService.getCategoryById()` returns detail → 200 | Happy path |
| 4 | `getById_shouldReturn404_whenNotFound` | Given UUID with no match → service throws `NotFoundException` → 404 | Not found |

### 3. `POST /api/categories/`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 5 | `create_shouldReturn200_withCategoryDetail` | Given valid CategoryCreate body → returns created CategoryDetail | Happy path |

### 4. `POST /api/categories/{categoryId}`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 6 | `update_shouldReturn200_withUpdatedCategory` | Given valid UUID + CategoryCreate body → returns updated CategoryDetail | Happy path |
| 7 | `update_shouldReturn404_whenNotFound` | Given UUID with no match → service throws `NotFoundException` → 404 | Not found |

### 5. `DELETE /api/categories/{categoryId}`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 8 | `delete_shouldReturn200_whenFound` | Given valid UUID → calls `deleteCategory()` → 200 | Happy path |
| 9 | `delete_shouldReturn404_whenNotFound` | Given UUID with no match → service throws `NotFoundException` → 404 | Not found |

---

**Total: 9 test cases**
