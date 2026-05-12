# Unit Test Master Plan — Trip Planning Application

## Overview

This document summarizes all planned unit test files for the `trip-planning` project.  
**Total: 147 test cases** across **22 test files**.

---

## Test Files Summary

| # | Plan File | Test Class | Source Class | Test Count |
|---|---|---|---|---|
| 01 | `01_unit_test_CategoryServiceImplTest.md` | `CategoryServiceImplTest` | `services/Impl/CategoryServiceImpl` | 13 |
| 02 | `02_unit_test_DestinationServiceImplTest.md` | `DestinationServiceImplTest` | `services/Impl/DestinationServiceImpl` | 17 |
| 03 | `03_unit_test_TripServiceImplTest.md` | `TripServiceImplTest` | `services/Impl/TripServiceImpl` | 17 |
| 04 | `04_unit_test_UserServiceImplTest.md` | `UserServiceImplTest` | `services/Impl/UserServiceImpl` | 14 |
| 05 | `05_unit_test_JwtServiceImplTest.md` | `JwtServiceImplTest` | `services/Impl/JwtServiceImpl` | 13 |
| 06 | `06_unit_test_EmailServiceImplTest.md` | `EmailServiceImplTest` | `services/Impl/EmailServiceImpl` | 5 |
| 07 | `07_unit_test_JwtFilterTest.md` | `JwtFilterTest` | `filter/JwtFilter` | 6 |
| 08 | `08_unit_test_AuthControllerTest.md` | `AuthControllerTest` | `controller/AuthController` | 10 |
| 09 | `09_unit_test_CategoryControllerTest.md` | `CategoryControllerTest` | `controller/CategoryController` | 9 |
| 10 | `10_unit_test_DestinationControllerTest.md` | `DestinationControllerTest` | `controller/DestinationController` | 9 |
| 11 | `11_unit_test_TripControllerTest.md` | `TripControllerTest` | `controller/TripController` | 10 |
| 12 | `12_unit_test_ApiExceptionHandlerTest.md` | `ApiExceptionHandlerTest` | `exception/ApiExceptionHandler` | 5 |
| 13 | `13_unit_test_ApiWrapperConfigTest.md` | `ApiWrapperConfigTest` | `config/ApiWrapperConfig` | 9 |
| 14 | `14_unit_test_AuthUserDetailsTest.md` | `AuthUserDetailsTest` | `config/AuthUserDetails` | 14 |
| 15 | `15_unit_test_UtilsAndHelpers.md` | _(multiple classes)_ | Various utils/DTOs/listeners | 40 |

---

## Test Categories

### Service Layer (66 tests)
- `CategoryServiceImplTest` — 13 tests
- `DestinationServiceImplTest` — 17 tests
- `TripServiceImplTest` — 17 tests
- `UserServiceImplTest` — 14 tests
- `JwtServiceImplTest` — 13 (counted below in infra, but service layer)
- `EmailServiceImplTest` — 5 tests

### Controller Layer (38 tests)
- `AuthControllerTest` — 10 tests
- `CategoryControllerTest` — 9 tests
- `DestinationControllerTest` — 9 tests
- `TripControllerTest` — 10 tests

### Config & Security (29 tests)
- `ApiWrapperConfigTest` — 9 tests
- `AuthUserDetailsTest` — 14 tests
- `JwtFilterTest` — 6 tests

### Exception Handling (5 tests)
- `ApiExceptionHandlerTest` — 5 tests

### Utilities, DTOs, Listeners (40 tests)
- `FilterBuilderHelperTest` — 13 tests
- `SecurityUtilsTest` — 4 tests
- `AuditMixinTest` — 3 tests
- `AuditMixinListenerTest` — 5 tests
- `GenericFilterFactoryTest` — 2 tests
- `UserCreatedListenerTest` — 2 tests
- `ApiResponseTest` — 6 tests
- `ResponseCategoryTest` — 1 test
- `ResponseDestinationTest` — 2 tests
- `ResponseTripTest` — 2 tests

---

## Not Covered (intentionally)

The following files are **not planned** for unit testing:

| File | Reason |
|---|---|
| `TripPlanningApplication.java` | Main entry point, tested by `@SpringBootTest` context load |
| `DataSeeder.java` | Configuration/seeder — better tested via integration test |
| `SecurityConfig.java` | Configuration class — tested indirectly via controller tests |
| `MailConfig.java` | Configuration bean — tested indirectly via EmailServiceImplTest |
| `OpenApiConfig.java` | Configuration class — purely declarative |
| `ApiController.java` | Annotation definition — no logic |
| `GenericFilterRepositoryImpl.java` | Complex JPA query logic — better tested via integration test with H2 |
| `DestinationFilterRepositoryImpl.java` | Complex JPA query logic — better tested via integration test with H2 |
| `BaseRepositoryImpl.java` | Delegates to `GenericFilterRepositoryImpl` — tested via integration test |
| `BaseRepositoryFactoryBean.java` | Spring infrastructure — tested via integration test |
| Repository interfaces | Spring Data JPA interfaces — no custom implementation to test |

---

## Recommended Execution Order

1. **Utils & DTOs** (plan 15) — foundation with no dependencies
2. **Config** (plans 13, 14) — security models
3. **Services** (plans 01–06) — business logic
4. **Filter** (plan 07) — security filter
5. **Exception Handler** (plan 12)
6. **Controllers** (plans 08–11) — API layer

---

## Testing Stack

- **JUnit 5** (JUnit Platform)
- **Mockito** (mocking dependencies)
- **Spring MockMvc** (controller tests via `@WebMvcTest`)
- **H2 Database** (already in build.gradle for integration tests)
- **Spring Security Test** (`@WithMockUser`, SecurityContextHolder manipulation)
