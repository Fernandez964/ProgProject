import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login user = new Login();

        System.out.println("=== USER REGISTRATION ===");
        
        System.out.print("Enter First Name: ");
        user.setFirstName(scanner.nextLine());

        System.out.print("Enter Last Name: ");
        user.setLastName(scanner.nextLine());

        System.out.print("Enter Username: ");
        user.setUsername(scanner.nextLine());

        System.out.print("Enter Password: ");
        user.setPassword(scanner.nextLine());

        System.out.print("Enter SA Cell Phone (+27...): ");
        user.setCellPhoneNumber(scanner.nextLine());

        // Perform registration check
        String registrationMessage = user.registerUser();
        System.out.println("\n" + registrationMessage);

        // Proceed to login only if all registration criteria pass
        if (user.checkUserName() && user.checkPasswordComplexity() && user.checkCellPhoneNumber()) {
            System.out.println("\n=== USER LOGIN ===");
            
            System.out.print("Enter Username: ");
            String loginUsername = scanner.nextLine();

            System.out.print("Enter Password: ");
            String loginPassword = scanner.nextLine();

            boolean loginSuccess = user.loginUser(loginUsername, loginPassword);
            String loginStatus = user.returnLoginStatus(loginSuccess);
            
            System.out.println("\n" + loginStatus);
        } else {
            System.out.println("\nRegistration failed. Please rerun the program and correct errors.");
        }

        scanner.close();
    }
}