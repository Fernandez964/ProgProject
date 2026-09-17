import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    // --- Username Validation Tests ---

    @Test
    public void testCheckUserNameValid() {
        Login login = new Login();
        login.setUsername("kyl_1");
        assertTrue(login.checkUserName(), "Username containing underscore and <= 5 characters should be valid.");
    }

    @Test
    public void testCheckUserNameInvalidFormat() {
        Login login = new Login();
        login.setUsername("kyle!!!!!!!");
        assertFalse(login.checkUserName(), "Username over 5 characters without underscore should fail.");
    }

    // --- Password Complexity Tests ---

    @Test
    public void testCheckPasswordComplexityValid() {
        Login login = new Login();
        login.setPassword("Ch&&sec@ke99!");
        assertTrue(login.checkPasswordComplexity(), "Password with uppercase, digit, special char, and length >= 8 should pass.");
    }

    @Test
    public void testCheckPasswordComplexityInvalid() {
        Login login = new Login();
        login.setPassword("password");
        assertFalse(login.checkPasswordComplexity(), "Simple password without uppercase, numbers, or special chars should fail.");
    }

    // --- Cell Phone Validation Tests ---

    @Test
    public void testCheckCellPhoneNumberValid() {
        Login login = new Login();
        login.setCellPhoneNumber("+27838968976");
        assertTrue(login.checkCellPhoneNumber(), "Cell number starting with +27 followed by 9 digits should pass.");
    }

    @Test
    public void testCheckCellPhoneNumberInvalid() {
        Login login = new Login();
        login.setCellPhoneNumber("08966553");
        assertFalse(login.checkCellPhoneNumber(), "Local formatted or short cell number should fail.");
    }

    // --- User Registration Messages Tests ---

    @Test
    public void testRegisterUserUsernameFailedMessage() {
        Login login = new Login("Kyle", "Smith", "kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976");
        String expected = "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.";
        assertEquals(expected, login.registerUser());
    }

    @Test
    public void testRegisterUserPasswordFailedMessage() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "password", "+27838968976");
        String expected = "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        assertEquals(expected, login.registerUser());
    }

    @Test
    public void testRegisterUserCellPhoneFailedMessage() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "08966553");
        String expected = "Cell number is incorrectly formatted or does not contain an international code, please correct the number and try again.";
        assertEquals(expected, login.registerUser());
    }

    @Test
    public void testRegisterUserSuccessMessage() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        String expected = "Username successfully captured.\nPassword successfully captured.\nCell number successfully captured.";
        assertEquals(expected, login.registerUser());
    }

    // --- Login Verification & Status Tests ---

    @Test
    public void testLoginUserSuccess() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    public void testLoginUserFailure() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.loginUser("kyl_1", "WrongPassword"));
    }

    @Test
    public void testReturnLoginStatusSuccess() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        String expected = "Welcome Kyle, Smith it is great to see you again.";
        assertEquals(expected, login.returnLoginStatus(true));
    }

    @Test
    public void testReturnLoginStatusFailure() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        String expected = "Username or password incorrect, please try again.";
        assertEquals(expected, login.returnLoginStatus(false));
    }
}