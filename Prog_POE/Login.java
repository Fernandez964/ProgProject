/**
 * Login class handles user registration validation and login functionality.
 */
public class Login {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String cellPhoneNumber;

    // Default constructor
    public Login() {
        this.firstName = "";
        this.lastName = "";
        this.username = "";
        this.password = "";
        this.cellPhoneNumber = "";
    }

    // Parameterized constructor
    public Login(String firstName, String lastName, String username, String password, String cellPhoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.cellPhoneNumber = cellPhoneNumber;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }

    public void setCellPhoneNumber(String cellPhoneNumber) {
        this.cellPhoneNumber = cellPhoneNumber;
    }

    /**
     * Checks if the username contains an underscore and is no more than 5 characters long.
     * @return true if valid, false otherwise.
     */
    public boolean checkUserName() {
        if (username != null && username.contains("_") && username.length() <= 5) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the password meets complexity rules:
     * - At least 8 characters long
     * - Contains a capital letter
     * - Contains a number
     * - Contains a special character
     * @return true if valid, false otherwise.
     */
    public boolean checkPasswordComplexity() {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);

            if (Character.isUpperCase(ch)) {
                hasCapital = true;
            } else if (Character.isDigit(ch)) {
                hasNumber = true;
            } else if (!Character.isLetterOrDigit(ch)) {
                hasSpecial = true;
            }
        }

        if (hasCapital && hasNumber && hasSpecial) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the South African cell phone number contains international country code (+27)
     * followed by 9 digits (10 digits total national number length).
     * 
     * REGEX ATTRIBUTION / REFERENCE:
     * Pattern follows South African International E.164 telecommunication formatting rules.
     * Reference: https://regex101.com/ (South Africa Mobile Number Regex Standard)
     * 
     * @return true if valid, false otherwise.
     */
    public boolean checkCellPhoneNumber() {
        if (cellPhoneNumber == null) {
            return false;
        }

        // ^\\+27  : Starts with international country code '+27'
        // [0-9]{9}$ : Followed by exactly 9 numerical digits
        String phoneRegex = "^\\+27[0-9]{9}$";

        if (cellPhoneNumber.matches(phoneRegex)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Evaluates registration conditions and returns the exact assessment messages.
     * @return Status message string.
     */
    public String registerUser() {
        if (!checkUserName()) {
            return "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.";
        }
        
        if (!checkPasswordComplexity()) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }
        
        if (!checkCellPhoneNumber()) {
            return "Cell number is incorrectly formatted or does not contain an international code, please correct the number and try again.";
        }

        return "Username successfully captured.\nPassword successfully captured.\nCell number successfully captured.";
    }

    /**
     * Verifies if entered credentials match stored user credentials.
     * @param enteredUsername Entered username.
     * @param enteredPassword Entered password.
     * @return true if correct, false otherwise.
     */
    public boolean loginUser(String enteredUsername, String enteredPassword) {
        if (username != null && password != null &&
            username.equals(enteredUsername) && password.equals(enteredPassword)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the welcome or error status message based on login result.
     * @param isLoggedIn Boolean result of login attempt.
     * @return Status message string.
     */
    public String returnLoginStatus(boolean isLoggedIn) {
        if (isLoggedIn) {
            return "Welcome " + firstName + ", " + lastName + " it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }
}