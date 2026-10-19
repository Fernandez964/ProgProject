import java.util.Random;

/**
 * Message class handles message creation, validation, hashing, and storage options.
 */
public class Message {

    private String messageID;
    private int messageNumber;
    private String recipientCell;
    private String messageContent;
    private String messageHash;
    private String messageStatus;

    // Constructor
    public Message(int messageNumber, String recipientCell, String messageContent) {
        this.messageNumber = messageNumber;
        this.recipientCell = recipientCell;
        this.messageContent = messageContent;
        this.messageID = generateMessageID();
        this.messageHash = createMessageHash();
    }

    /**
     * Generates a random 10-digit message ID string.
     */
    private String generateMessageID() {
        Random rand = new Random();
        long id = 1000000000L + rand.nextLong(9000000000L);
        return String.valueOf(id);
    }

    /**
     * Ensures that the message ID is not more than ten characters.
     */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    /**
     * Ensures that the recipient cell number is no more than ten characters long
     * and starts with an international code (+27).
     */
    public boolean checkRecipientCell() {
        if (recipientCell == null) {
            return false;
        }
        String regex = "^\\+27[0-9]{7}$"; // +27 followed by 7 digits = 10 characters total (+27 + 7 digits)
        return recipientCell.matches(regex) && recipientCell.length() <= 10;
    }

    /**
     * Validates message length requirement (less than 250 characters).
     */
    public boolean checkMessageLength() {
        return messageContent != null && messageContent.length() < 250;
    }

    /**
     * Creates and returns the Message Hash in all caps:
     * Format: [first 2 numbers of ID]:[message number]:[FIRST and LAST word of message]
     */
    public String createMessageHash() {
        if (messageID == null || messageID.length() < 2 || messageContent == null) {
            return "";
        }
        
        String prefix = messageID.substring(0, 2);
        
        // Extract first and last words
        String trimmed = messageContent.trim();
        // Remove punctuation for clean word extraction if needed, or split by spaces
        String[] words = trimmed.split("\\s+");
        String firstWord = words.length > 0 ? words[0].replaceAll("[^a-zA-Z]", "") : "";
        String lastWord = words.length > 1 ? words[words.length - 1].replaceAll("[^a-zA-Z]", "") : firstWord;
        
        String combinedWords = (firstWord + lastWord).toUpperCase();
        
        return prefix + ":" + messageNumber + ":" + combinedWords;
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
}