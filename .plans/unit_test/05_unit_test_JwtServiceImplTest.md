# Unit Test Plan: JwtServiceImplTest ✅ TEST PASSED (13/13)

**Source File:** `services/Impl/JwtServiceImpl.java`  
**Test File:** `test/java/com/kms/tripplanning/services/Impl/JwtServiceImplTest.java`  
**Type:** Unit Test (Mockito + real JWT generation with test secret key)  
**Mocked Dependencies:** `ObjectProvider<UserService>`  
**Notes:** Use `@Value` injection with test properties or ReflectionTestUtils to set `secretKey` and `expiration`.

---

## Test Cases

### 1. `generateToken`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `generateToken_shouldReturnNonNullToken` | Given valid `AuthUserDetails` → returns a non-null, non-empty JWT string | Happy path |
| 2 | `generateToken_shouldContainSubject` | Generated token's subject should equal `userDetails.getUsername()` (email) | Subject claim |
| 3 | `generateToken_shouldContainFirstAndLastNameClaims` | Generated token should contain `firstName` and `lastName` claims | Custom claims |
| 4 | `generateToken_shouldContainIdClaim` | Generated token's JTI should equal `userDetails.getId().toString()` | ID claim |
| 5 | `generateToken_shouldHaveCorrectExpiration` | Generated token expiration should be approximately `now + expiration` ms | Expiration |

### 2. `generateVerifyToken`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 6 | `generateVerifyToken_shouldContainIsVerifyEmailClaim` | Generated token should contain `isVerifyEmail=true` claim | Verify claim |
| 7 | `generateVerifyToken_shouldExpireIn5Minutes` | Token expiration should be ~5 minutes from now | Short expiration |

### 3. `isTokenValid`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 8 | `isTokenValid_shouldReturnTrue_forValidToken` | Given a freshly generated token → returns true | Valid token |
| 9 | `isTokenValid_shouldReturnFalse_forExpiredToken` | Given a token with past expiration → returns false or throws `BadCredentialsException` | Expired token |
| 10 | `isTokenValid_shouldThrowBadCredentials_forTamperedToken` | Given a tampered/malformed token → throws `BadCredentialsException` | Invalid token |

### 4. `getCredentialFromToken`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 11 | `getCredentialFromToken_shouldReturnAuthUserDetails` | Given valid token → extracts email from subject → calls `userService.loadUserByUsername()` → returns `AuthUserDetails` | Happy path |

### 5. `getClaimFromToken`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 12 | `getClaimFromToken_shouldExtractSubject` | Given valid token and `Claims::getSubject` resolver → returns the email | Subject extraction |
| 13 | `getClaimFromToken_shouldExtractCustomClaim` | Given valid token and custom claim resolver → returns the claim value | Custom claim extraction |

---

**Total: 13 test cases**
