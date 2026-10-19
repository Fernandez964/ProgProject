import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the messages of a session and persists them as JSON.
 *
 * The store keeps the messages that were sent and the ones that were stored
 * for later, prints them on request and can write them to, and read them
 * from, a JSON file so they survive a restart of the application.
 */
public class MessageStore {

    /** File used when no other location is given. */
    public static final String DEFAULT_FILE_NAME = "quickchat-messages.json";

    private final List<Message> sentMessages = new ArrayList<>();
    private final List<Message> storedMessages = new ArrayList<>();
    private final Path file;
    private int totalMessages;

    /**
     * Creates a store that uses the default file in the working directory.
     */
    public MessageStore() {
        this(Path.of(DEFAULT_FILE_NAME));
    }

    /**
     * Creates a store that uses the given file.
     *
     * @param file the JSON file the messages are written to and read from.
     */
    public MessageStore(Path file) {
        this.file = file;
    }

    /**
     * Records a message the user has completed.
     *
     * Sent and stored messages are kept so they can be shown and saved, while
     * disregarded messages are only counted.
     *
     * @param message the message to record.
     */
    public void recordMessage(Message message) {
        if (message == null) {
            return;
        }

        totalMessages++;

        if ("Sent".equals(message.getMessageStatus())) {
            sentMessages.add(message);
        } else if ("Stored".equals(message.getMessageStatus())) {
            storedMessages.add(message);
        }
    }

    /**
     * Prints every message that is kept in the store.
     */
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

    /**
     * Prints a single message in detail.
     *
     * @param message the message to print.
     */
    private void printMessage(Message message) {
        System.out.println("\n--- Message Summary ---");
        System.out.println("Message ID: " + message.getMessageID());
        System.out.println("Message Number: " + message.getMessageNumber());
        System.out.println("Message Hash: " + message.getMessageHash());
        System.out.println("Recipient: " + message.getRecipientCell());
        System.out.println("Message: " + message.getMessageContent());
        System.out.println("Status: " + message.getMessageStatus());
    }

    /**
     * Returns the total number of messages that were processed.
     *
     * @return the number of messages the user completed in this session.
     */
    public int returnTotalMessages() {
        return totalMessages;
    }

    /**
     * Returns the messages that are kept in the store, sent ones first.
     *
     * @return a copy of the stored messages.
     */
    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<>(sentMessages);
        messages.addAll(storedMessages);
        return messages;
    }

    /**
     * Returns the messages that were sent.
     *
     * @return a copy of the sent messages.
     */
    public List<Message> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    /**
     * Returns the messages that were stored for later.
     *
     * @return a copy of the stored messages.
     */
    public List<Message> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    /**
     * Removes every message from the store.
     */
    public void clear() {
        sentMessages.clear();
        storedMessages.clear();
        totalMessages = 0;
    }

    /**
     * Builds the JSON text that is written to the file.
     *
     * @return a JSON array holding every message in the store.
     */
    public String toJSON() {
        StringBuilder sb = new StringBuilder("[\n");
        List<Message> messages = getMessages();

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

    /**
     * Writes the messages to the JSON file.
     *
     * @return true when the file was written.
     * @throws IOException when the file cannot be written.
     */
    public boolean saveToFile() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(file, toJSON(), StandardCharsets.UTF_8);
        return true;
    }

    /**
     * Reads the messages from the JSON file.
     *
     * The messages that are currently in the store are replaced by the ones
     * that are read from the file.
     *
     * @return the number of messages that were read.
     * @throws IOException when the file cannot be read.
     */
    public int loadFromFile() throws IOException {
        clear();

        if (!Files.exists(file)) {
            return 0;
        }

        String content = Files.readString(file, StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return 0;
        }

        int loaded = 0;
        for (String element : Json.parseArray(content)) {
            Message message = Message.fromJSON(element);
            if (message != null) {
                recordMessage(message);
                loaded++;
            }
        }

        return loaded;
    }

    /**
     * Returns the file the messages are written to and read from.
     *
     * @return the JSON file path.
     */
    public Path getFile() {
        return file;
    }
}
