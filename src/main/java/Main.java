import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    /** Number of login attempts allowed before the program ends. */
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            run(scanner);
        } catch (EndOfInputException e) {
            System.out.println("\nInput ended. Goodbye!");
        }
    }

    /**
     * Runs the registration, login and messaging flow.
     */
    private static void run(Scanner scanner) {
        Login user = new Login();

        System.out.println("=== USER REGISTRATION ===");
        user.setFirstName(readLine(scanner, "Enter First Name: "));
        user.setLastName(readLine(scanner, "Enter Last Name: "));
        user.setUsername(readLine(scanner, "Enter Username: "));
        user.setPassword(readRawLine(scanner, "Enter Password: "));
        user.setCellPhoneNumber(readLine(scanner, "Enter SA Cell Phone (+27...): "));

        System.out.println("\n" + user.registerUser());

        // Proceed to login only if registration succeeds
        if (!user.checkUserName() || !user.checkPasswordComplexity() || !user.checkCellPhoneNumber()) {
            System.out.println("\nRegistration failed. Please rerun the program and correct errors.");
            return;
        }

        if (!login(scanner, user)) {
            return;
        }

        System.out.println("\nWelcome to QuickChat.");

        List<Message> sentMessagesList = new ArrayList<>();
        List<Message> storedMessagesList = new ArrayList<>();
        int totalMessagesSent = 0;
        boolean running = true;

        while (running) {
            System.out.println("\n--- QUICKCHAT MENU ---");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");

            int choice = readMenuChoice(scanner, "Choose an option: ", 1, 3);

            switch (choice) {
                case 1:
                    totalMessagesSent = captureMessages(scanner, sentMessagesList, storedMessagesList, totalMessagesSent);
                    System.out.println("\nTotal number of messages processed: " + totalMessagesSent);
                    break;

                case 2:
                    showMessages(sentMessagesList, storedMessagesList);
                    break;

                case 3:
                    running = false;
                    System.out.println("Thank you for using QuickChat. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice. Please select 1, 2, or 3.");
            }
        }
    }

    /**
     * Guides the user through the login process, allowing a few attempts.
     *
     * @return true when the user logged in, false when the attempts were used up.
     */
    private static boolean login(Scanner scanner, Login user) {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            System.out.println("\n=== USER LOGIN ===");
            String loginUsername = readLine(scanner, "Enter Username: ");
            String loginPassword = readRawLine(scanner, "Enter Password: ");

            boolean loginSuccess = user.loginUser(loginUsername, loginPassword);
            System.out.println("\n" + user.returnLoginStatus(loginSuccess));

            if (loginSuccess) {
                return true;
            }

            int attemptsLeft = MAX_LOGIN_ATTEMPTS - attempt;
            if (attemptsLeft > 0) {
                System.out.println("Attempts remaining: " + attemptsLeft);
            }
        }

        System.out.println("\nToo many failed login attempts. Goodbye!");
        return false;
    }

    /**
     * Captures the requested number of messages, validating each one before it is accepted.
     *
     * @return the running total of messages captured.
     */
    private static int captureMessages(Scanner scanner, List<Message> sentMessagesList,
                                       List<Message> storedMessagesList, int totalMessagesSent) {
        int numMessages = readInt(scanner, "How many messages do you wish to enter? ", 1, Integer.MAX_VALUE);
        int captured = 0;

        while (captured < numMessages) {
            System.out.println("\n--- Enter Message " + (captured + 1) + " ---");

            // Check recipient cell validation before asking for the message text
            String recipient = readLine(scanner, "Enter Recipient Cell Number (e.g. +27838968976): ");
            if (!CellNumber.isValid(recipient)) {
                System.out.println("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.");
                continue;
            }
            System.out.println("Cell phone number successfully captured.");

            String msgText = readRawLine(scanner, "Enter Message (max 250 characters): ");
            Message msg = new Message(totalMessagesSent + 1, recipient, msgText);

            // Check message length validation
            if (!msg.checkMessageLength()) {
                int excess = msgText.length() - Message.MAX_MESSAGE_LENGTH;
                System.out.println("Please enter a message of less than 250 characters. (Exceeds by " + excess + " characters)");
                continue;
            }

            System.out.println("\nMessage ID generated: " + msg.getMessageID());
            System.out.println("Message Hash: " + msg.getMessageHash());

            // Send / Store / Disregard options
            System.out.println("\nChoose action for this message:");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int actionChoice = readInt(scanner, "Select option (1-3): ", 1, 3);

            String actionResult = msg.sentMessage(actionChoice);
            System.out.println(actionResult);

            if ("Sent".equals(msg.getMessageStatus())) {
                sentMessagesList.add(msg);
            } else if ("Stored".equals(msg.getMessageStatus())) {
                storedMessagesList.add(msg);
            } else {
                // Disregarded messages are only kept when the user confirms the deletion
                readInt(scanner, "Press 0 to delete the message: ", 0, 0);
            }

            captured++;
            totalMessagesSent++;

            // Display full summary of the message
            System.out.println("\n--- Message Summary ---");
            System.out.println("Message ID: " + msg.getMessageID());
            System.out.println("Message Hash: " + msg.getMessageHash());
            System.out.println("Recipient: " + msg.getRecipientCell());
            System.out.println("Message: " + msg.getMessageContent());
            System.out.println("Status: " + msg.getMessageStatus());
        }

        return totalMessagesSent;
    }

    /**
     * Prints the messages that were sent and the ones that were stored for later.
     */
    private static void showMessages(List<Message> sentMessagesList, List<Message> storedMessagesList) {
        if (sentMessagesList.isEmpty() && storedMessagesList.isEmpty()) {
            System.out.println("No messages have been sent yet.");
            return;
        }

        for (Message msg : sentMessagesList) {
            printMessage(msg);
        }

        for (Message msg : storedMessagesList) {
            printMessage(msg);
        }
    }

    /**
     * Prints a single stored message in detail.
     */
    private static void printMessage(Message msg) {
        System.out.println("\n--- Message Summary ---");
        System.out.println("Message ID: " + msg.getMessageID());
        System.out.println("Message Number: " + msg.getMessageNumber());
        System.out.println("Message Hash: " + msg.getMessageHash());
        System.out.println("Recipient: " + msg.getRecipientCell());
        System.out.println("Message: " + msg.getMessageContent());
        System.out.println("Status: " + msg.getMessageStatus());
    }

    /**
     * Reads a line of text and trims surrounding whitespace.
     */
    private static String readLine(Scanner scanner, String prompt) {
        return readRawLine(scanner, prompt).trim();
    }

    /**
     * Reads a line of text exactly as it was typed.
     */
    private static String readRawLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new EndOfInputException();
        }
        return scanner.nextLine();
    }

    /**
     * Reads a number within the given range, re-prompting until the input is valid.
     */
    private static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new EndOfInputException();
            }
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, please enter a number.");
            }
        }
    }

    /**
     * Reads a menu choice, returning -1 when the input is not a valid option.
     */
    private static int readMenuChoice(Scanner scanner, String prompt, int min, int max) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new EndOfInputException();
        }
        String input = scanner.nextLine().trim();

        try {
            int value = Integer.parseInt(input);
            if (value >= min && value <= max) {
                return value;
            }
        } catch (NumberFormatException e) {
            return -1;
        }

        return -1;
    }

    /**
     * Thrown when the input stream ends before the user finished entering data.
     */
    private static class EndOfInputException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
