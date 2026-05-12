# Unit Test Plan: AuthControllerTest ✅ TEST PASSED (10/10)

**Source File:** `controller/AuthController.java`  
**Test File:** `test/java/com/kms/tripplanning/controller/AuthControllerTest.java`  
**Type:** WebMvcTest (MockMvc + Mockito)  
**Mocked Dependencies:** `UserService`, `JwtService` (for security filter)

---

## Test Cases

### 1. `POST /api/auth/register`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `register_shouldReturn204_whenValidRequest` | Given valid Register JSON body → calls `userService.registerUser()` → returns 204 No Content | Happy path |
| 2 | `register_shouldReturn400_whenEmailInvalid` | Given invalid email format → validation fails → returns 400 with validation errors | Invalid email |
| 3 | `register_shouldReturn400_whenPasswordTooShort` | Given password < 8 chars → validation fails → returns 400 | Weak password |
| 4 | `register_shouldReturn400_whenPasswordMissingSpecialChar` | Given password without special character → validation fails → returns 400 | Password pattern |
| 5 | `register_shouldReturn400_whenFirstNameBlank` | Given blank firstName → validation fails → returns 400 | Missing field |

### 2. `POST /api/auth/login`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 6 | `login_shouldReturn200_withAuthResponse` | Given valid Login body → `userService.login()` returns AuthResponse → returns 200 with token | Happy path |
| 7 | `login_shouldReturn400_whenEmailBlank` | Given blank email → validation fails → returns 400 | Missing email |

### 3. `GET /api/auth/me`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 8 | `getCurrentUser_shouldReturn200_withMeResponse` | Given authenticated user → `userService.getCurrentUser()` returns Me → returns 200 | Happy path |
| 9 | `getCurrentUser_shouldReturn401_whenNotAuthenticated` | Given no authentication → security blocks → returns 401 | Unauthenticated |

### 4. `POST /api/auth/verify-email`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 10 | `verifyEmail_shouldReturn204_whenValid` | Given valid VerifyEmail body → calls `userService.verifyEmail()` → returns 204 | Happy path |

---

**Total: 10 test cases**
