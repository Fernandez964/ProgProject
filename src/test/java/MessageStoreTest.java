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

        assertEquals(2, store.returnTotalMessages());
        assertEquals(1, store.getSentMessages().size());
        assertEquals(1, store.getStoredMessages().size());
        assertEquals(2, store.getMessages().size());
    }

    @Test
    public void testDisregardedMessageIsCountedButNotKept() {
        MessageStore store = new MessageStore(tempDir.resolve("messages.json"));

        Message disregarded = new Message(1, "+27838968976", "Hello there");
        disregarded.sentMessage(2);

        store.recordMessage(disregarded);

        assertEquals(1, store.returnTotalMessages());
        assertTrue(store.getMessages().isEmpty());
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

        assertTrue(Files.exists(file));

        MessageStore loadedStore = new MessageStore(file);
        int loaded = loadedStore.loadFromFile();

        assertEquals(2, loaded);
        assertEquals(2, loadedStore.returnTotalMessages());
        assertEquals(1, loadedStore.getSentMessages().size());
        assertEquals(1, loadedStore.getStoredMessages().size());

        Message loadedSent = loadedStore.getSentMessages().get(0);
        assertEquals(sent.getMessageID(), loadedSent.getMessageID());
        assertEquals(sent.getMessageNumber(), loadedSent.getMessageNumber());
        assertEquals(sent.getMessageHash(), loadedSent.getMessageHash());
        assertEquals(sent.getRecipientCell(), loadedSent.getRecipientCell());
        assertEquals(sent.getMessageContent(), loadedSent.getMessageContent());
        assertEquals("Sent", loadedSent.getMessageStatus());
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
        assertTrue(content.trim().startsWith("["));
        assertTrue(content.trim().endsWith("]"));
        assertEquals(1, Json.parseArray(content).size());
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
        assertEquals(1, loadedStore.loadFromFile());
        assertEquals(content, loadedStore.getMessages().get(0).getMessageContent());
    }

    @Test
    public void testLoadFromMissingFileReturnsNothing() throws Exception {
        MessageStore store = new MessageStore(tempDir.resolve("missing.json"));

        assertEquals(0, store.loadFromFile());
        assertEquals(0, store.returnTotalMessages());
    }

    @Test
    public void testPrintMessagesShowsTheStoredMessages() {
        MessageStore store = new MessageStore(tempDir.resolve("messages.json"));

        Message sent = new Message(1, "+27838968976", "Hello there");
        sent.sentMessage(1);
        store.recordMessage(sent);

        String output = captureOutput(store::printMessages);

        assertTrue(output.contains(sent.getMessageID()));
        assertTrue(output.contains("Hello there"));
        assertTrue(output.contains("Sent"));
    }

    @Test
    public void testPrintMessagesOnEmptyStore() {
        MessageStore store = new MessageStore(tempDir.resolve("messages.json"));

        String output = captureOutput(store::printMessages);

        assertTrue(output.contains("No messages have been sent yet."));
    }

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
