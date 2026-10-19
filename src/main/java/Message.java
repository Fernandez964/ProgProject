import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Message class handles message creation, validation, hashing, and storage options.
 */
public class Message {

    /** Maximum number of characters allowed in a message (must be less than this). */
    public static final int MAX_MESSAGE_LENGTH = 250;

    /** Maximum number of characters allowed in a message ID. */
    public static final int MAX_ID_LENGTH = 10;

    /** Message IDs already issued in this run, so no two messages share one. */
    private static final Set<String> USED_MESSAGE_IDS = Collections.synchronizedSet(new HashSet<String>());

    private final String messageID;
    private final int messageNumber;
    private final String recipientCell;
    private final String messageContent;
    private final String messageHash;
    private String messageStatus;

    // Constructor
    public Message(int messageNumber, String recipientCell, String messageContent) {
        this.messageNumber = messageNumber;
        this.recipientCell = recipientCell;
        this.messageContent = messageContent;
        this.messageID = generateMessageID();
        this.messageHash = buildMessageHash();
    }

    /**
     * Generates a random 10-digit message ID string that has not been used before.
     */
    private String generateMessageID() {
        Random rand = new Random();
        String id;
        do {
            id = String.valueOf(1000000000L + Math.floorMod(rand.nextLong(), 9000000000L));
        } while (!USED_MESSAGE_IDS.add(id));
        return id;
    }

    /**
     * Ensures that the message ID is not more than ten characters.
     */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= MAX_ID_LENGTH;
    }

    /**
     * Ensures that the recipient cell number uses the international format (+27
     * followed by 9 digits). The rule is shared with the registration check via
     * {@link CellNumber}, so a number accepted at registration is accepted here.
     */
    public boolean checkRecipientCell() {
        return CellNumber.isValid(recipientCell);
    }

    /**
     * Validates message length requirement (less than 250 characters).
     */
    public boolean checkMessageLength() {
        return messageContent != null && messageContent.length() < MAX_MESSAGE_LENGTH;
    }

    /**
     * Creates and returns the Message Hash in all caps:
     * Format: [first 2 numbers of ID]:[message number]:[FIRST and LAST word of message]
     */
    public String createMessageHash() {
        return buildMessageHash();
    }

    /**
     * Builds the message hash. Kept separate from {@link #createMessageHash()} so the
     * constructor can call it without exposing a partially built object to subclasses.
     */
    private String buildMessageHash() {
        if (messageID == null || messageID.length() < 2 || messageContent == null) {
            return "";
        }

        String prefix = messageID.substring(0, 2);

        // Extract first and last words
        String[] words = messageContent.trim().split("\\s+");
        if (words.length == 0 || (words.length == 1 && words[0].isEmpty())) {
            return prefix + ":" + messageNumber + ":";
        }

        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);

        // A single word message contributes that word once, not twice
        String combinedWords = words.length == 1 ? firstWord : firstWord + lastWord;

        return prefix + ":" + messageNumber + ":" + combinedWords.toUpperCase();
    }

    /**
     * Removes punctuation from a word while keeping letters and digits.
     * Falls back to the unpunctuated word when it contains no letters or digits.
     */
    private String cleanWord(String word) {
        String cleaned = word.replaceAll("[^a-zA-Z0-9]", "");
        return cleaned.isEmpty() ? word : cleaned;
    }

    /**
     * Allows the user to choose if they want to send, store, or disregard the message.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                this.messageStatus = "Sent";
                return "Message successfully sent.";
            case 2:
                this.messageStatus = "Disregarded";
                return "Press 0 to delete the message";
            case 3:
                this.messageStatus = "Stored";
                return "Message successfully stored.";
            default:
                this.messageStatus = "Pending";
                return "Invalid option selected.";
        }
    }

    /**
     * Stores the message in a JSON format representation (Research requirement).
     */
    public String storeMessageJSON() {
        return "{\n" +
               "  \"messageID\": \"" + messageID + "\",\n" +
               "  \"messageNumber\": " + messageNumber + ",\n" +
               "  \"recipientCell\": \"" + recipientCell + "\",\n" +
               "  \"messageContent\": \"" + messageContent + "\",\n" +
               "  \"messageHash\": \"" + messageHash + "\",\n" +
               "  \"status\": \"" + (messageStatus != null ? messageStatus : "Pending") + "\"\n" +
               "}";
    }

    // Getters
    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipientCell() { return recipientCell; }
    public String getMessageContent() { return messageContent; }
    public String getMessageHash() { return messageHash; }
    public String getMessageStatus() { return messageStatus; }
}
