import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Part3Test {

    @TempDir
    Path tempDir;

    private MessageStore createStoreWithTestData(Path jsonFile) {
        MessageStore store = new MessageStore(jsonFile);

        // Test Message 1: Sent
        Message msg1 = new Message(1, "+27834557896", "Did you get the cake?", "1000000001");
        msg1.sentMessage(1);

        // Test Message 2: Stored
        Message msg2 = new Message(2, "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", "1000000002");
        msg2.sentMessage(3);

        // Test Message 3: Disregard
        Message msg3 = new Message(3, "+27834484567",
                "Yooohooo, I am at your gate.", "1000000003");
        msg3.sentMessage(2);

        // Test Message 4: Sent (Developer/Sender: 0838884567)
        Message msg4 = new Message(4, "0838884567", "+27838884567",
                "It is dinner time!", "1000000004");
        msg4.sentMessage(1);

        store.recordMessage(msg1);
        store.recordMessage(msg2);
        store.recordMessage(msg3);
        store.recordMessage(msg4);

        return store;
    }

    @Test
    public void testSentMessagesArrayPopulated() {
        MessageStore store = createStoreWithTestData(tempDir.resolve("test-messages.json"));
        Message[] sent = store.getSentMessagesArray();

        assertEquals(2, sent.length);
        assertEquals("Did you get the cake?", sent[0].getMessageContent());
        assertEquals("It is dinner time!", sent[1].getMessageContent());
    }

    @Test
    public void testDisplayLongestStoredMessage() {
        MessageStore store = createStoreWithTestData(tempDir.resolve("test-messages.json"));
        String longest = store.getLongestStoredMessage();

        assertEquals("Where are you? You are late! I have asked you to be on time.", longest);
    }

    @Test
    public void testSearchByMessageIdForTestMessage4() {
        MessageStore store = createStoreWithTestData(tempDir.resolve("test-messages.json"));
        String foundContent = store.searchMessageById("1000000004");

        assertEquals("It is dinner time!", foundContent);
    }

    @Test
    public void testSearchMessagesByRecipient() {
        MessageStore store = createStoreWithTestData(tempDir.resolve("test-messages.json"));

        Message extra = new Message(5, "+27838884567",
                "Ok, I am leaving without you.", "1000000005");
        extra.sentMessage(1);
        store.recordMessage(extra);

        List<String> matches = store.searchMessagesByRecipient("+27838884567");

        assertEquals(3, matches.size());
        assertEquals("It is dinner time!", matches.get(0));
        assertEquals("Ok, I am leaving without you.", matches.get(1));
        assertEquals("Where are you? You are late! I have asked you to be on time.", matches.get(2));
    }

    @Test
    public void testDeleteMessageByHashTestMessage2() {
        MessageStore store = createStoreWithTestData(tempDir.resolve("test-messages.json"));
        Message target = store.findMessageById("1000000002");
        assertNotNull(target);

        String result = store.deleteMessageByHash(target.getMessageHash());

        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                result);
        assertEquals(0, store.getStoredMessagesArray().length);
    }

    @Test
    public void testDisplayMessageReport() {
        MessageStore store = createStoreWithTestData(tempDir.resolve("test-messages.json"));
        String report = store.buildMessageReport();

        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("It is dinner time!"));
        assertTrue(report.contains("+27838884567"));
        assertTrue(report.contains("Message Hash"));
    }

    @Test
    public void testStoredMessagesLoadedFromJsonIntoArray() throws Exception {
        Path jsonFile = tempDir.resolve("stored-test.json");
        MessageStore original = createStoreWithTestData(jsonFile);
        original.saveToFile();

        MessageStore loadedStore = new MessageStore(jsonFile);
        int count = loadedStore.loadFromFile();

        assertEquals(4, count);
        assertEquals(1, loadedStore.getStoredMessagesArray().length);
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                loadedStore.getStoredMessagesArray()[0].getMessageContent());
    }

    @Test
    public void testInvalidJsonFileThrowsIOException() throws Exception {
        Path badJson = tempDir.resolve("invalid.json");
        Files.writeString(badJson, "{ not valid json array", StandardCharsets.UTF_8);

        MessageStore store = new MessageStore(badJson);
        assertThrows(IOException.class, store::loadFromFile);
    }
}
