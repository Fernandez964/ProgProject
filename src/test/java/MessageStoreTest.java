import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MessageStoreTest {

    @TempDir
    Path tempDir;

    @Test
    public void testRecordMessageCountsAndRoutesMessages() {
        MessageStore store = new MessageStore(tempDir.resolve("messages.json"));

        Message sent = new Message(1, "+27838968976", "Hello there");
        sent.sentMessage(1);
        Message stored = new Message(2, "+27838968976", "Send later");
        stored.sentMessage(3);

        store.recordMessage(sent);
        store.recordMessage(stored);

        assertEquals(2, store.returnTotalMessages(), "Both messages should be counted.");
        assertEquals(1, store.getSentMessages().size(), "One message was sent.");
        assertEquals(1, store.getStoredMessages().size(), "One message was stored.");
        assertEquals(2, store.getMessages().size(), "Both messages are kept in the store.");
    }

    @Test
    public void testDisregardedMessageIsCountedButNotKept() {
        MessageStore store = new MessageStore(tempDir.resolve("messages.json"));

        Message disregarded = new Message(1, "+27838968976", "Hello there");
        disregarded.sentMessage(2);

        store.recordMessage(disregarded);

        assertEquals(1, store.returnTotalMessages(), "The message was processed.");
        assertTrue(store.getMessages().isEmpty(), "A disregarded message is not kept.");
    }

    @Test
    public void testSaveAndLoadMessages() throws Exception {
        Path file = tempDir.resolve("messages.json");
        MessageStore store = new MessageStore(file);

        Message sent = new Message(1, "+27838968976", "Hello there");
        sent.sentMessage(1);
        Message stored = new Message(2, "+27838968976", "Send later");
        stored.sentMessage(3);

        store.recordMessage(sent);
        store.recordMessage(stored);
        store.saveToFile();

        assertTrue(Files.exists(file), "The message file should be created.");

        MessageStore loadedStore = new MessageStore(file);
        int loaded = loadedStore.loadFromFile();

        assertEquals(2, loaded, "Both messages should be read back.");
        assertEquals(2, loadedStore.returnTotalMessages(), "The total should match the messages that were read.");
        assertEquals(1, loadedStore.getSentMessages().size(), "One message was sent.");
        assertEquals(1, loadedStore.getStoredMessages().size(), "One message was stored.");

        Message loadedSent = loadedStore.getSentMessages().get(0);
        assertEquals(sent.getMessageID(), loadedSent.getMessageID(), "The ID should survive the round trip.");
        assertEquals(sent.getMessageNumber(), loadedSent.getMessageNumber(), "The number should survive the round trip.");
        assertEquals(sent.getMessageHash(), loadedSent.getMessageHash(), "The hash should survive the round trip.");
        assertEquals(sent.getRecipientCell(), loadedSent.getRecipientCell(), "The recipient should survive the round trip.");
        assertEquals(sent.getMessageContent(), loadedSent.getMessageContent(), "The content should survive the round trip.");
        assertEquals("Sent", loadedSent.getMessageStatus(), "The status should survive the round trip.");
    }

    @Test
    public void testSavedFileHoldsValidJson() throws Exception {
        Path file = tempDir.resolve("messages.json");
        MessageStore store = new MessageStore(file);

        Message sent = new Message(1, "+27838968976", "Hello there");
        sent.sentMessage(1);
        store.recordMessage(sent);
        store.saveToFile();

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.trim().startsWith("["), "The file should hold a JSON array.");
        assertTrue(content.trim().endsWith("]"), "The file should hold a JSON array.");
        assertEquals(1, Json.parseArray(content).size(), "The file should hold one message object.");
    }

    @Test
    public void testMessagesWithSpecialCharactersSurviveTheRoundTrip() throws Exception {
        Path file = tempDir.resolve("messages.json");
        MessageStore store = new MessageStore(file);

        String content = "He said \"hi\" and left\nagain";
        Message sent = new Message(1, "+27838968976", content);
        sent.sentMessage(1);
        store.recordMessage(sent);
        store.saveToFile();

        MessageStore loadedStore = new MessageStore(file);
        assertEquals(1, loadedStore.loadFromFile(), "The message should be read back.");
        assertEquals(content, loadedStore.getMessages().get(0).getMessageContent(),
                "Quotes and line breaks should survive the round trip.");
    }

    @Test
    public void testLoadFromMissingFileReturnsNothing() throws Exception {
        MessageStore store = new MessageStore(tempDir.resolve("missing.json"));

        assertEquals(0, store.loadFromFile(), "A missing file should not fail.");
        assertEquals(0, store.returnTotalMessages(), "A missing file should not add messages.");
    }

    @Test
    public void testPrintMessagesShowsTheStoredMessages() {
        MessageStore store = new MessageStore(tempDir.resolve("messages.json"));

        Message sent = new Message(1, "+27838968976", "Hello there");
        sent.sentMessage(1);
        store.recordMessage(sent);

        String output = captureOutput(store::printMessages);

        assertTrue(output.contains(sent.getMessageID()), "The message ID should be printed.");
        assertTrue(output.contains("Hello there"), "The message content should be printed.");
        assertTrue(output.contains("Sent"), "The status should be printed.");
    }

    @Test
    public void testPrintMessagesOnEmptyStore() {
        MessageStore store = new MessageStore(tempDir.resolve("messages.json"));

        String output = captureOutput(store::printMessages);

        assertTrue(output.contains("No messages have been sent yet."), "An empty store should say so.");
    }

    /**
     * Runs the given action while the standard output is captured.
     */
    private String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            action.run();
        } finally {
            System.setOut(originalOut);
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }
}
