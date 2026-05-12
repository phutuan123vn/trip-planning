# Unit Test Plan: AuthUserDetailsTest ✅ TEST PASSED

**Source File:** `config/AuthUserDetails.java`  
**Test File:** `test/java/com/kms/tripplanning/config/AuthUserDetailsTest.java`  
**Type:** Unit Test (plain JUnit, no mocks needed — just construct User + AuthUserDetails)

---

## Test Cases

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `getAuthorities_shouldReturnRoleUser` | Returns `List.of(SimpleGrantedAuthority("ROLE_USER"))` | Authority mapping |
| 2 | `getPassword_shouldReturnUserPassword` | Returns the User entity's password | Password delegation |
| 3 | `getUsername_shouldReturnUserEmail` | Returns the User entity's email | Username = email |
| 4 | `getFirstName_shouldReturnUserFirstName` | Returns firstName from User | Field delegation |
| 5 | `getLastName_shouldReturnUserLastName` | Returns lastName from User | Field delegation |
| 6 | `getId_shouldReturnUserId` | Returns the User entity's UUID | ID delegation |
| 7 | `isEnabled_shouldReturnTrue_whenActiveAndNotDeletedAndNotLocked` | User with `isActive=true, isLocked=false, deletedAt=null` → returns true | Enabled user |
| 8 | `isEnabled_shouldReturnFalse_whenNotActive` | User with `isActive=false` → returns false | Inactive user |
| 9 | `isEnabled_shouldReturnFalse_whenLocked` | User with `isLocked=true` → returns false | Locked user |
| 10 | `isEnabled_shouldReturnFalse_whenDeleted` | User with `deletedAt` set → returns false | Deleted user |
| 11 | `isAccountNonLocked_shouldReturnTrue_whenNotLockedAndNotDeleted` | Happy path → returns true | Non-locked |
| 12 | `isAccountNonLocked_shouldReturnFalse_whenLocked` | User with `isLocked=true` → returns false | Locked |
| 13 | `isAccountNonExpired_shouldReturnTrue` | Always returns true | Static value |
| 14 | `isCredentialsNonExpired_shouldReturnTrue` | Always returns true | Static value |

---

**Total: 14 test cases**
