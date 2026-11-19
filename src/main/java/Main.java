import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final Path MESSAGE_FILE = Path.of(MessageStore.DEFAULT_FILE_NAME);

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            run(scanner);
        } catch (EndOfInputException e) {
            System.out.println("\nInput ended. Goodbye!");
        }
    }

    private static void run(Scanner scanner) {
        Login user = new Login();

        System.out.println("=== USER REGISTRATION ===");
        user.setFirstName(readLine(scanner, "Enter First Name: "));
        user.setLastName(readLine(scanner, "Enter Last Name: "));
        user.setUsername(readLine(scanner, "Enter Username: "));
        user.setPassword(readRawLine(scanner, "Enter Password: "));
        user.setCellPhoneNumber(readLine(scanner, "Enter SA Cell Phone (+27...): "));

        System.out.println("\n" + user.registerUser());

        if (!user.checkUserName() || !user.checkPasswordComplexity() || !user.checkCellPhoneNumber()) {
            System.out.println("\nRegistration failed. Please rerun the program and correct errors.");
            return;
        }

        if (!login(scanner, user)) {
            return;
        }

        System.out.println("\nWelcome to QuickChat.");

        MessageStore store = new MessageStore(MESSAGE_FILE);
        loadMessages(store);

        boolean running = true;

        while (running) {
            System.out.println("\n--- QUICKCHAT MENU ---");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Save messages to file");
            System.out.println("4) Stored Messages");
            System.out.println("5) Load messages from file");
            System.out.println("6) Quit");

            int choice = readMenuChoice(scanner, "Choose an option: ", 1, 6);

            switch (choice) {
                case 1:
                    captureMessages(scanner, store, user.getCellPhoneNumber());
                    System.out.println("\nTotal number of messages processed: " + store.returnTotalMessages());
                    break;
                case 2:
                    store.printMessages();
                    break;
                case 3:
                    saveMessages(store);
                    break;
                case 4:
                    showStoredMessagesMenu(scanner, store);
                    break;
                case 5:
                    loadMessages(store);
                    break;
                case 6:
                    running = false;
                    System.out.println("Thank you for using QuickChat. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1, 2, 3, 4, 5 or 6.");
            }
        }
    }

    private static void showStoredMessagesMenu(Scanner scanner, MessageStore store) {
        boolean inSubMenu = true;

        while (inSubMenu) {
            System.out.println("\n--- STORED MESSAGES MENU ---");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message by Message ID");
            System.out.println("d) Search for all messages sent or stored for a particular recipient");
            System.out.println("e) Delete a message using its Message Hash");
            System.out.println("f) Display report of stored/sent messages");
            System.out.println("x) Return to main menu");

            String option = readLine(scanner, "Choose an option (a-f or x): ").toLowerCase();

            switch (option) {
                case "a":
                    System.out.println("\n" + store.getStoredSendersAndRecipients());
                    break;
                case "b":
                    String longest = store.getLongestStoredMessage();
                    if (longest.isEmpty()) {
                        System.out.println("\nNo stored messages available.");
                    } else {
                        System.out.println("\nLongest Stored Message:\n" + longest);
                    }
                    break;
                case "c":
                    String searchId = readLine(scanner, "Enter Message ID to search: ");
                    Message found = store.findMessageById(searchId);
                    if (found == null) {
                        System.out.println("\nNo message found with ID: " + searchId);
                    } else {
                        System.out.println("\nRecipient : " + found.getRecipientCell());
                        System.out.println("Message   : " + found.getMessageContent());
                    }
                    break;
                case "d":
                    String targetRecipient = readLine(scanner, "Enter recipient cell number: ");
                    List<String> matches = store.searchMessagesByRecipient(targetRecipient);
                    if (matches.isEmpty()) {
                        System.out.println("\nNo sent or stored messages found for " + targetRecipient + ".");
                    } else {
                        System.out.println("\nMessages for " + targetRecipient + ":");
                        for (int i = 0; i < matches.size(); i++) {
                            System.out.println((i + 1) + ". " + matches.get(i));
                        }
                    }
                    break;
                case "e":
                    String hashToDelete = readLine(scanner, "Enter Message Hash to delete: ");
                    System.out.println("\n" + store.deleteMessageByHash(hashToDelete));
                    break;
                case "f":
                    System.out.println("\n" + store.buildMessageReport());
                    break;
                case "x":
                    inSubMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose a, b, c, d, e, f, or x.");
            }
        }
    }

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

    private static void captureMessages(Scanner scanner, MessageStore store, String senderCell) {
        int numMessages = readInt(scanner, "How many messages do you wish to enter? ", 1, Integer.MAX_VALUE);
        int captured = 0;

        while (captured < numMessages) {
            System.out.println("\n--- Enter Message " + (captured + 1) + " ---");

            String recipient = readLine(scanner, "Enter Recipient Cell Number (e.g. +27838968976): ");
            if (!CellNumber.isValid(recipient)) {
                System.out.println("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.");
                continue;
            }
            System.out.println("Cell phone number successfully captured.");

            String msgText = readRawLine(scanner, "Enter Message (max 250 characters): ");
            Message msg = new Message(store.returnTotalMessages() + 1, senderCell, recipient, msgText);

            if (!msg.checkMessageLength()) {
                System.out.println(msg.lengthExceededMessage());
                continue;
            }

            System.out.println("\nMessage ID generated: " + msg.getMessageID());
            System.out.println("Message Hash: " + msg.getMessageHash());

            System.out.println("\nChoose action for this message:");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int actionChoice = readInt(scanner, "Select option (1-3): ", 1, 3);

            String actionResult = msg.sentMessage(actionChoice);
            System.out.println(actionResult);

            if (!"Sent".equals(msg.getMessageStatus()) && !"Stored".equals(msg.getMessageStatus())) {
                readInt(scanner, "Press 0 to delete the message: ", 0, 0);
            }

            store.recordMessage(msg);
            captured++;

            System.out.println("\n--- Message Summary ---");
            System.out.println("Message ID: " + msg.getMessageID());
            System.out.println("Message Hash: " + msg.getMessageHash());
            System.out.println("Sender: " + msg.getSenderCell());
            System.out.println("Recipient: " + msg.getRecipientCell());
            System.out.println("Message: " + msg.getMessageContent());
            System.out.println("Status: " + msg.getMessageStatus());
        }
    }

    private static void saveMessages(MessageStore store) {
        try {
            store.saveToFile();
            System.out.println("\nMessages saved to " + store.getFile().toAbsolutePath() + ".");
        } catch (IOException e) {
            System.out.println("\nCould not save the messages: " + e.getMessage());
        }
    }

    private static void loadMessages(MessageStore store) {
        try {
            int loaded = store.loadFromFile();
            if (loaded == 0) {
                System.out.println("\nNo messages were found in " + store.getFile().toAbsolutePath() + ".");
            } else {
                System.out.println("\nLoaded " + loaded + " message(s) from " + store.getFile().toAbsolutePath() + ".");
            }
        } catch (IOException e) {
            System.out.println("\nCould not load the messages: " + e.getMessage());
        }
    }

    private static String readLine(Scanner scanner, String prompt) {
        return readRawLine(scanner, prompt).trim();
    }

    private static String readRawLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new EndOfInputException();
        }
        return scanner.nextLine();
    }

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

    private static class EndOfInputException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
