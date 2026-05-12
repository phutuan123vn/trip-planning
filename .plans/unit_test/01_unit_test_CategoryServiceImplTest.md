# Unit Test Plan: CategoryServiceImplTest ✅ TEST PASSED (13/13)

**Source File:** `services/Impl/CategoryServiceImpl.java`  
**Test File:** `test/java/com/kms/tripplanning/services/Impl/CategoryServiceImplTest.java`  
**Type:** Unit Test (Mockito)  
**Mocked Dependencies:** `CategoryRepository`

---

## Test Cases

### 1. `listCategories`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `listCategories_shouldReturnPageOfCategories` | Given valid filters, sortBy, sortDirection, page, size → returns a `Page<CategoryDetail>` | Happy path with results |
| 2 | `listCategories_shouldReturnEmptyPage_whenNoResults` | Given filters that match nothing → returns an empty page | No matching data |
| 3 | `listCategories_shouldApplyFiltersCorrectly` | Verify `FilterBuilderHelper.addFilter()` is called for each filter entry and `categoryRepository.search()` receives the built filter map | Filter delegation |
| 4 | `listCategories_shouldApplySortCorrectly` | Given sortBy="name" and sortDirection="desc" → verify PageRequest is created with correct Sort | Sort handling |
| 5 | `listCategories_shouldHandleEmptyFilters` | Given empty filters map → should still call search without error | Edge case |

### 2. `getCategoryById`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 6 | `getCategoryById_shouldReturnCategoryDetail_whenFound` | Given a valid UUID → repository returns a Category → returns `CategoryDetail` with correct id/name | Happy path |
| 7 | `getCategoryById_shouldThrowNotFoundException_whenNotFound` | Given a UUID with no matching category → `findById` returns empty → throws `NotFoundException` | Not found |

### 3. `createCategory`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 8 | `createCategory_shouldSaveAndReturnCategoryDetail` | Given a valid `CategoryCreate` request → builds Category entity, calls `save()`, returns `CategoryDetail` | Happy path |
| 9 | `createCategory_shouldSetNameFromRequest` | Verify the saved Category entity has the correct name from the request | Field mapping |

### 4. `deleteCategory`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 10 | `deleteCategory_shouldDeleteCategory_whenFound` | Given a valid UUID → finds category → calls `delete()` | Happy path |
| 11 | `deleteCategory_shouldThrowNotFoundException_whenNotFound` | Given a UUID with no matching category → throws `NotFoundException` | Not found |

### 5. `updateCategory`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 12 | `updateCategory_shouldUpdateNameAndReturnDetail` | Given valid UUID and CategoryCreate → finds category, updates name, saves, returns updated detail | Happy path |
| 13 | `updateCategory_shouldThrowNotFoundException_whenNotFound` | Given a UUID with no matching category → throws `NotFoundException` | Not found |

---

**Total: 13 test cases**
