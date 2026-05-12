# Unit Test Plan: EmailServiceImplTest ✅ TEST PASSED (5/5)

**Source File:** `services/Impl/EmailServiceImpl.java`  
**Test File:** `test/java/com/kms/tripplanning/services/Impl/EmailServiceImplTest.java`  
**Type:** Unit Test (Mockito)  
**Mocked Dependencies:** `TransactionalEmailsApi`

---

## Test Cases

### 1. `sendVerificationEmail`

| # | Test Method Name | Description | Scenario |
|---|---|---|---|
| 1 | `sendVerificationEmail_shouldCallSendTransacEmail` | Given a valid email and token → constructs `SendSmtpEmail` with correct recipients, subject, body → calls `apiInstance.sendTransacEmail()` | Happy path |
| 2 | `sendVerificationEmail_shouldSetCorrectSender` | Verify the sender is set with name "Trip Planning App" and correct email address | Sender configuration |
| 3 | `sendVerificationEmail_shouldContainTokenInHtmlContent` | Verify the HTML content includes the verification token in the link URL | Token in email body |
| 4 | `sendVerificationEmail_shouldSetCorrectRecipient` | Verify the recipient list contains exactly one `SendSmtpEmailTo` with the provided email | Recipient mapping |
| 5 | `sendVerificationEmail_shouldHandleApiException_gracefully` | Given `sendTransacEmail()` throws an exception → should not propagate, should log error | Error handling |

---

**Total: 5 test cases**
