import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds session and JSON-loaded messages, maintains typed arrays for Part 3,
 * and provides search, delete, longest-message, and reporting operations.
 */
public class MessageStore {

    public static final String DEFAULT_FILE_NAME = "quickchat-messages.json";

    private final List<Message> sentMessages = new ArrayList<>();
    private final List<Message> storedMessages = new ArrayList<>();
    private final List<Message> disregardedMessages = new ArrayList<>();
    private final List<String> messageHashes = new ArrayList<>();
    private final List<String> messageIDs = new ArrayList<>();

    private final Path file;
    private int totalMessages;

    public MessageStore() {
        this(Path.of(DEFAULT_FILE_NAME));
    }

    public MessageStore(Path file) {
        this.file = file;
    }

    public void recordMessage(Message message) {
        if (message == null) {
            return;
        }

        totalMessages++;
        addUnique(messageIDs, message.getMessageID());
        addUnique(messageHashes, message.getMessageHash());

        String status = message.getMessageStatus();
        if ("Sent".equalsIgnoreCase(status)) {
            addMessageIfAbsent(sentMessages, message);
        } else if ("Stored".equalsIgnoreCase(status)) {
            addMessageIfAbsent(storedMessages, message);
        } else if ("Disregarded".equalsIgnoreCase(status) || "Disregard".equalsIgnoreCase(status)) {
            addMessageIfAbsent(disregardedMessages, message);
        }
    }

    private void addUnique(List<String> target, String value) {
        if (value != null && !value.isBlank() && !target.contains(value)) {
            target.add(value);
        }
    }

    private void addMessageIfAbsent(List<Message> target, Message candidate) {
        for (Message existing : target) {
            if (existing.getMessageID().equals(candidate.getMessageID())) {
                return;
            }
        }
        target.add(candidate);
    }

    public Message[] getSentMessagesArray() {
        return sentMessages.toArray(new Message[0]);
    }

    public Message[] getDisregardedMessagesArray() {
        return disregardedMessages.toArray(new Message[0]);
    }

    public Message[] getStoredMessagesArray() {
        return storedMessages.toArray(new Message[0]);
    }

    public String[] getMessageHashesArray() {
        return messageHashes.toArray(new String[0]);
    }

    public String[] getMessageIDsArray() {
        return messageIDs.toArray(new String[0]);
    }

    public int returnTotalMessages() {
        return totalMessages;
    }

    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<>(sentMessages);
        messages.addAll(storedMessages);
        return messages;
    }

    public List<Message> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    public List<Message> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    public List<Message> getDisregardedMessages() {
        return new ArrayList<>(disregardedMessages);
    }

    public void clear() {
        sentMessages.clear();
        storedMessages.clear();
        disregardedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        totalMessages = 0;
    }

    public String getStoredSendersAndRecipients() {
        Message[] storedArray = getStoredMessagesArray();
        if (storedArray.length == 0) {
            return "No stored messages found.";
        }

        StringBuilder sb = new StringBuilder("=== STORED MESSAGES: SENDER & RECIPIENT ===\n");
        for (int i = 0; i < storedArray.length; i++) {
            Message msg = storedArray[i];
            String sender = (msg.getSenderCell() == null || msg.getSenderCell().isBlank())
                    ? "Not recorded"
                    : msg.getSenderCell();
            sb.append((i + 1))
              .append(". Sender: ").append(sender)
              .append(" | Recipient: ").append(msg.getRecipientCell())
              .append("\n");
        }
        return sb.toString().trim();
    }

    public String getLongestStoredMessage() {
        Message[] storedArray = getStoredMessagesArray();
        if (storedArray.length == 0) {
            return "";
        }

        Message longest = storedArray[0];
        for (int i = 1; i < storedArray.length; i++) {
            String currentContent = storedArray[i].getMessageContent();
            String longestContent = longest.getMessageContent();
            int currentLen = currentContent == null ? 0 : currentContent.length();
            int longestLen = longestContent == null ? 0 : longestContent.length();
            if (currentLen > longestLen) {
                longest = storedArray[i];
            }
        }
        return longest.getMessageContent();
    }

    public String searchMessageById(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return "";
        }
        String targetId = messageId.trim();

        for (Message msg : getAllTrackedMessages()) {
            if (targetId.equals(msg.getMessageID())) {
                return msg.getMessageContent();
            }
        }
        return "";
    }

    public Message findMessageById(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return null;
        }
        String targetId = messageId.trim();

        for (Message msg : getAllTrackedMessages()) {
            if (targetId.equals(msg.getMessageID())) {
                return msg;
            }
        }
        return null;
    }

    public List<String> searchMessagesByRecipient(String recipient) {
        List<String> matches = new ArrayList<>();
        if (recipient == null || recipient.isBlank()) {
            return matches;
        }

        String normalizedTarget = CellNumber.normalize(recipient);
        for (Message msg : getMessages()) {
            String msgRecipient = CellNumber.normalize(msg.getRecipientCell());
            if (normalizedTarget.equals(msgRecipient) || recipient.trim().equals(msg.getRecipientCell())) {
                matches.add(msg.getMessageContent());
            }
        }
        return matches;
    }

    public String deleteMessageByHash(String messageHash) {
        if (messageHash == null || messageHash.isBlank()) {
            return "Message hash not found.";
        }

        String targetHash = messageHash.trim();
        Message removed = removeFromListByHash(sentMessages, targetHash);
        if (removed == null) {
            removed = removeFromListByHash(storedMessages, targetHash);
        }
        if (removed == null) {
            removed = removeFromListByHash(disregardedMessages, targetHash);
        }

        if (removed == null) {
            return "No message found with hash: " + targetHash;
        }

        messageHashes.remove(removed.getMessageHash());
        messageIDs.remove(removed.getMessageID());
        if (totalMessages > 0) {
            totalMessages--;
        }

        return "Message: \"" + removed.getMessageContent() + "\" successfully deleted.";
    }

    private Message removeFromListByHash(List<Message> list, String targetHash) {
        for (int i = 0; i < list.size(); i++) {
            Message current = list.get(i);
            if (targetHash.equalsIgnoreCase(current.getMessageHash())) {
                return list.remove(i);
            }
        }
        return null;
    }

    public String buildMessageReport() {
        List<Message> reportMessages = getMessages();
        if (reportMessages.isEmpty()) {
            return "No messages available for report.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("                 QUICKCHAT MESSAGE REPORT               \n");
        sb.append("========================================================\n");

        for (int i = 0; i < reportMessages.size(); i++) {
            Message msg = reportMessages.get(i);
            sb.append("Message ").append(i + 1).append(" [").append(msg.getMessageStatus()).append("]\n");
            sb.append("  Message Hash : ").append(msg.getMessageHash()).append("\n");
            sb.append("  Recipient    : ").append(msg.getRecipientCell()).append("\n");
            sb.append("  Message      : ").append(msg.getMessageContent()).append("\n");
            sb.append("--------------------------------------------------------\n");
        }

        return sb.toString().trim();
    }

    private List<Message> getAllTrackedMessages() {
        List<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        all.addAll(disregardedMessages);
        return all;
    }

    public void printMessages() {
        List<Message> messages = getMessages();
        if (messages.isEmpty()) {
            System.out.println("No messages have been sent yet.");
            return;
        }
        for (Message message : messages) {
            printMessage(message);
        }
    }

    private void printMessage(Message message) {
        System.out.println("\n--- Message Summary ---");
        System.out.println("Message ID: " + message.getMessageID());
        System.out.println("Message Number: " + message.getMessageNumber());
        System.out.println("Message Hash: " + message.getMessageHash());
        System.out.println("Recipient: " + message.getRecipientCell());
        System.out.println("Message: " + message.getMessageContent());
        System.out.println("Status: " + message.getMessageStatus());
    }

    public String toJSON() {
        StringBuilder sb = new StringBuilder("[\n");
        List<Message> messages = getAllTrackedMessages();

        for (int i = 0; i < messages.size(); i++) {
            String[] lines = messages.get(i).storeMessageJSON().split("\n", -1);
            for (int line = 0; line < lines.length; line++) {
                if (line > 0) {
                    sb.append('\n');
                }
                sb.append("  ").append(lines[line]);
            }
            if (i < messages.size() - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }

        sb.append("]");
        return sb.toString();
    }

    public boolean saveToFile() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(file, toJSON(), StandardCharsets.UTF_8);
        return true;
    }

    public int loadFromFile() throws IOException {
        clear();

        if (!Files.exists(file)) {
            return 0;
        }

        String content = Files.readString(file, StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return 0;
        }

        List<String> elements;
        try {
            elements = Json.parseArray(content);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid JSON format in " + file.getFileName() + ": " + e.getMessage(), e);
        }

        int loaded = 0;
        for (String element : elements) {
            Message message = Message.fromJSON(element);
            if (message != null) {
                recordMessage(message);
                loaded++;
            }
        }

        return loaded;
    }

    public Path getFile() {
        return file;
    }
}
