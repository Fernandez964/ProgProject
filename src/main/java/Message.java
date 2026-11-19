import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Message class handles message creation, validation, hashing, and storage options.
 */
public class Message {

    public static final int MAX_MESSAGE_LENGTH = 250;
    public static final int MAX_ID_LENGTH = 10;

    private static final Set<String> USED_MESSAGE_IDS = Collections.synchronizedSet(new HashSet<String>());

    private final String messageID;
    private final int messageNumber;
    private final String senderCell;
    private final String recipientCell;
    private final String messageContent;
    private final String messageHash;
    private String messageStatus;

    public Message(int messageNumber, String recipientCell, String messageContent) {
        this(messageNumber, "", recipientCell, messageContent, null);
    }

    public Message(int messageNumber, String senderCell, String recipientCell, String messageContent) {
        this(messageNumber, senderCell, recipientCell, messageContent, null);
    }

    public Message(int messageNumber, String recipientCell, String messageContent, String messageID) {
        this(messageNumber, "", recipientCell, messageContent, messageID);
    }

    public Message(int messageNumber, String senderCell, String recipientCell,
                   String messageContent, String messageID) {
        this.messageNumber = messageNumber;
        this.senderCell = senderCell == null ? "" : senderCell.trim();
        this.recipientCell = CellNumber.normalize(recipientCell);
        this.messageContent = messageContent;
        this.messageID = (messageID == null || messageID.trim().isEmpty())
                ? generateMessageID()
                : messageID.trim();
        this.messageHash = buildMessageHash();
        USED_MESSAGE_IDS.add(this.messageID);
    }

    public static Message withMessageId(int messageNumber, String recipientCell,
                                        String messageContent, String messageID) {
        return new Message(messageNumber, "", recipientCell, messageContent, messageID);
    }

    private Message(String messageID, int messageNumber, String senderCell, String recipientCell,
                    String messageContent, String messageHash, String messageStatus) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.senderCell = senderCell == null ? "" : senderCell.trim();
        this.recipientCell = CellNumber.normalize(recipientCell);
        this.messageContent = messageContent;
        this.messageHash = (messageHash == null || messageHash.isEmpty())
                ? buildMessageHash()
                : messageHash;
        this.messageStatus = messageStatus;
        USED_MESSAGE_IDS.add(messageID);
    }

    public static Message fromJSON(String json) {
        Map<String, String> values;
        try {
            values = Json.parseObject(json);
        } catch (IllegalArgumentException e) {
            return null;
        }

        String messageID = values.get("messageID");
        String recipientCell = values.get("recipientCell");
        String messageContent = values.get("messageContent");
        String messageNumber = values.get("messageNumber");

        if (messageID == null || recipientCell == null || messageContent == null || messageNumber == null) {
            return null;
        }

        int number;
        try {
            number = Integer.parseInt(messageNumber.trim());
        } catch (NumberFormatException e) {
            return null;
        }

        String sender = values.get("senderCell");
        if (sender == null) {
            sender = values.get("developer");
        }
        String status = values.get("status");
        return new Message(messageID, number, sender, recipientCell,
                messageContent, values.get("messageHash"), status);
    }

    private String generateMessageID() {
        Random rand = new Random();
        String id;
        do {
            id = String.valueOf(1000000000L + Math.floorMod(rand.nextLong(), 9000000000L));
        } while (!USED_MESSAGE_IDS.add(id));
        return id;
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= MAX_ID_LENGTH;
    }

    public boolean checkRecipientCell() {
        return CellNumber.isValid(recipientCell);
    }

    public boolean checkMessageLength() {
        return messageContent != null && messageContent.length() <= MAX_MESSAGE_LENGTH;
    }

    public String lengthExceededMessage() {
        int excess = messageContent == null ? 0 : messageContent.length() - MAX_MESSAGE_LENGTH;
        return "Message exceeds 250 characters by " + excess + " characters; please reduce the size.";
    }

    public String createMessageHash() {
        return buildMessageHash();
    }

    private String buildMessageHash() {
        if (messageID == null || messageID.length() < 2 || messageContent == null) {
            return "";
        }

        String prefix = messageID.substring(0, 2);
        String[] words = messageContent.trim().split("\\s+");
        if (words.length == 0 || (words.length == 1 && words[0].isEmpty())) {
            return prefix + ":" + messageNumber + ":";
        }

        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);
        String combinedWords = words.length == 1 ? firstWord : firstWord + lastWord;

        return prefix + ":" + messageNumber + ":" + combinedWords.toUpperCase();
    }

    private String cleanWord(String word) {
        String cleaned = word.replaceAll("[^a-zA-Z0-9]", "");
        return cleaned.isEmpty() ? word : cleaned;
    }

    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                this.messageStatus = "Sent";
                return "Message ready to send.";
            case 2:
                this.messageStatus = "Disregarded";
                return "Press 0 to delete the message.";
            case 3:
                this.messageStatus = "Stored";
                return "Message ready to send.";
            default:
                this.messageStatus = "Pending";
                return "Invalid option selected.";
        }
    }

    public String storeMessageJSON() {
        return "{\n" +
               "  \"messageID\": \"" + Json.escape(messageID) + "\",\n" +
               "  \"messageNumber\": " + messageNumber + ",\n" +
               "  \"senderCell\": \"" + Json.escape(senderCell) + "\",\n" +
               "  \"recipientCell\": \"" + Json.escape(recipientCell) + "\",\n" +
               "  \"messageContent\": \"" + Json.escape(messageContent) + "\",\n" +
               "  \"messageHash\": \"" + Json.escape(messageHash) + "\",\n" +
               "  \"status\": \"" + Json.escape(messageStatus != null ? messageStatus : "Pending") + "\"\n" +
               "}";
    }

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getSenderCell() { return senderCell; }
    public String getRecipientCell() { return recipientCell; }
    public String getMessageContent() { return messageContent; }
    public String getMessageHash() { return messageHash; }
    public String getMessageStatus() { return messageStatus; }
}
