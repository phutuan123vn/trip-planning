# Unit Test Plan: ApiExceptionHandlerTest ✅ TEST PASSED

**Source File:** `exception/ApiExceptionHandler.java`  
**Test File:** `test/java/com/kms/tripplanning/exception/ApiExceptionHandlerTest.java`  
**Type:** Unit Test (direct invocation)  
**Mocked Dependencies:** None (test the handler methods directly)

---

## Test Cases

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `handleException_shouldReturn500_withErrorResponse` | Given a generic `Exception` → returns `ResponseEntity` with 500 status, "Internal Server Error" message, "INTERNAL_SERVER_ERROR" code | Generic exception |
| 2 | `handleValidationException_shouldReturn400_withErrorResponse` | Given a `ValidationException` → returns 400 with "Validation Error" message | Validation exception |
| 3 | `handleMethodArgumentNotValidException_shouldReturn400_withFieldErrors` | Given a `MethodArgumentNotValidException` with field errors → returns 400 with list of `ValidationError` details | Bean validation |
| 4 | `handleNotFoundException_shouldReturn404_withErrorResponse` | Given a `NotFoundException` → returns 404 with "Not Found" message, "NOT_FOUND" code | Not found exception |
| 5 | `handleException_shouldIncludeExceptionMessage` | Verify the error response `details` field contains the exception message | Error details |

---

**Total: 5 test cases**
