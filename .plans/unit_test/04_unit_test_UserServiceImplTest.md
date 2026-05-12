# Unit Test Plan: UserServiceImplTest ✅ TEST PASSED (14/14)

**Source File:** `services/Impl/UserServiceImpl.java`  
**Test File:** `test/java/com/kms/tripplanning/services/Impl/UserServiceImplTest.java`  
**Type:** Unit Test (Mockito + SecurityContext mocking)  
**Mocked Dependencies:** `UserRepository`, `PasswordEncoder`, `ObjectProvider<AuthenticationManager>`, `JwtService`, `ApplicationEventPublisher`  
**Static Mocks:** `SecurityUtils.getCurrentUser()` (for `getCurrentUser`)

---

## Test Cases

### 1. `loadUserByUsername`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `loadUserByUsername_shouldReturnAuthUserDetails_whenFound` | Given a valid email → `search()` returns a User → returns `AuthUserDetails` wrapping the user | Happy path |
| 2 | `loadUserByUsername_shouldThrowUsernameNotFoundException_whenNotFound` | Given an email with no match → `search()` returns empty → throws `UsernameNotFoundException` | Not found |

### 2. `registerUser`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 3 | `registerUser_shouldEncodePasswordAndSaveUser` | Given a Register request → encodes password → builds User entity → calls `save()` | Password encoding |
| 4 | `registerUser_shouldPublishUserCreatedEvent` | Given a Register request → after save, verify `eventPublisher.publishEvent()` is called with correct `UserCreatedEvent` | Event publishing |
| 5 | `registerUser_shouldGenerateVerifyToken` | Verify `jwtService.generateVerifyToken()` is called with `AuthUserDetails` wrapping the saved user | Token generation |
| 6 | `registerUser_shouldMapAllFieldsFromRequest` | Verify email, firstName, lastName are all set on the saved User entity | Field mapping |

### 3. `getCurrentUser`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 7 | `getCurrentUser_shouldReturnMeResponse` | Given authenticated user via SecurityUtils → returns `ResponseUser.Me` with correct id, firstName, lastName, email | Happy path |

### 4. `login`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 8 | `login_shouldReturnAuthResponse_whenCredentialsValid` | Given valid email/password → `authenticationManager.authenticate()` succeeds → generates JWT → returns `AuthResponse` | Happy path |
| 9 | `login_shouldThrowException_whenCredentialsInvalid` | Given invalid credentials → `authenticationManager.authenticate()` throws → exception propagates | Bad credentials |

### 5. `verifyEmail`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 10 | `verifyEmail_shouldActivateUser_whenTokenValid` | Given valid token with `isVerifyEmail` claim matching email → finds user → sets `isActive=true` → saves | Happy path |
| 11 | `verifyEmail_shouldThrowValidationException_whenTokenInvalid` | Given an invalid/expired token → `isTokenValid()` returns false → throws `ValidationException` | Invalid token |
| 12 | `verifyEmail_shouldThrowValidationException_whenMissingVerifyEmailClaim` | Given token without `isVerifyEmail` claim → throws `ValidationException` | Missing claim |
| 13 | `verifyEmail_shouldThrowValidationException_whenEmailMismatch` | Given token where subject email doesn't match request email → throws `ValidationException` | Email mismatch |
| 14 | `verifyEmail_shouldThrowValidationException_whenUserNotFound` | Given valid token but user ID doesn't exist → throws `ValidationException` | User not found |

---

**Total: 14 test cases**
