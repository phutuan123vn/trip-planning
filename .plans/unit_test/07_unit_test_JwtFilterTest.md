# Unit Test Plan: JwtFilterTest ✅ TEST PASSED (6/6)

**Source File:** `filter/JwtFilter.java`  
**Test File:** `test/java/com/kms/tripplanning/filter/JwtFilterTest.java`  
**Type:** Unit Test (Mockito)  
**Mocked Dependencies:** `JwtService`, `HttpServletRequest`, `HttpServletResponse`, `FilterChain`

---

## Test Cases

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `doFilterInternal_shouldContinueChain_whenNoAuthHeader` | Request has no "Authorization" header → should call `filterChain.doFilter()` without setting authentication | No auth header |
| 2 | `doFilterInternal_shouldContinueChain_whenAuthHeaderNotBearer` | Request has "Authorization: Basic xxx" → should skip JWT processing, call `filterChain.doFilter()` | Non-Bearer header |
| 3 | `doFilterInternal_shouldContinueChain_whenTokenInvalid` | Request has "Authorization: Bearer <invalid>" → `jwtService.isTokenValid()` returns false → continue chain without auth | Invalid token |
| 4 | `doFilterInternal_shouldSetAuthentication_whenTokenValid` | Request has valid Bearer token → extracts user details → sets `UsernamePasswordAuthenticationToken` in SecurityContext | Happy path |
| 5 | `doFilterInternal_shouldSkipAuth_whenAlreadyAuthenticated` | SecurityContext already has an authenticated principal → should not call `jwtService.getCredentialFromToken()` | Already authenticated |
| 6 | `doFilterInternal_shouldExtractTokenFromHeader` | Verify token extraction strips "Bearer " prefix correctly | Token parsing |

---

**Total: 6 test cases**
