import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    // --- Message ID Tests ---

    @Test
    public void testMessageIDIsTenCharacters() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals(10, message.getMessageID().length(), "Message ID should be exactly ten characters.");
        assertTrue(message.checkMessageID(), "Message ID should pass the length check.");
    }

    @Test
    public void testMessageIDsAreUnique() {
        Message first = new Message(1, "+27838968976", "First message");
        Message second = new Message(2, "+27838968976", "Second message");
        assertNotEquals(first.getMessageID(), second.getMessageID(), "Each message should get its own ID.");
    }

    // --- Recipient Cell Validation Tests ---

    @Test
    public void testCheckRecipientCellValid() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertTrue(message.checkRecipientCell(), "A number in international format should pass.");
    }

    @Test
    public void testCheckRecipientCellInvalid() {
        assertFalse(new Message(1, "0839653210", "Hello there").checkRecipientCell(),
                "A local format number should fail.");
        assertFalse(new Message(1, "+2783896897", "Hello there").checkRecipientCell(),
                "A number with too few digits should fail.");
        assertFalse(new Message(1, "+278389689760", "Hello there").checkRecipientCell(),
                "A number with too many digits should fail.");
        assertFalse(new Message(1, null, "Hello there").checkRecipientCell(),
                "A missing number should fail.");
    }

    @Test
    public void testRecipientCellMatchesRegistrationFormat() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        Message message = new Message(1, login.getCellPhoneNumber(), "Hello there");
        assertTrue(login.checkCellPhoneNumber(), "The registered number should be valid.");
        assertTrue(message.checkRecipientCell(), "The registered number should also be a valid recipient.");
    }

    // --- Message Length Tests ---

    @Test
    public void testCheckMessageLengthValid() {
        Message message = new Message(1, "+27838968976", "Short message");
        assertTrue(message.checkMessageLength(), "A message under 250 characters should pass.");
    }

    @Test
    public void testCheckMessageLengthTooLong() {
        String tooLong = "a".repeat(250);
        Message message = new Message(1, "+27838968976", tooLong);
        assertFalse(message.checkMessageLength(), "A message of 250 characters should fail.");
    }

    // --- Message Hash Tests ---

    @Test
    public void testCreateMessageHashFormat() {
        Message message = new Message(7, "+27838968976", "Hello world this is a test");
        String expected = message.getMessageID().substring(0, 2) + ":7:HELLOTEST";
        assertEquals(expected, message.getMessageHash(), "Hash should hold the first two ID digits, the number and the first and last word.");
    }

    @Test
    public void testCreateMessageHashWithSingleWord() {
        Message message = new Message(1, "+27838968976", "Hello");
        String expected = message.getMessageID().substring(0, 2) + ":1:HELLO";
        assertEquals(expected, message.getMessageHash(), "A single word message should only contribute that word once.");
    }

    @Test
    public void testCreateMessageHashKeepsDigitsInWords() {
        Message message = new Message(1, "+27838968976", "2024 was great");
        String expected = message.getMessageID().substring(0, 2) + ":1:2024GREAT";
        assertEquals(expected, message.getMessageHash(), "Words that contain digits should keep them.");
    }

    // --- Send / Store / Disregard Tests ---

    @Test
    public void testSentMessageStatusSent() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("Message successfully sent.", message.sentMessage(1));
        assertEquals("Sent", message.getMessageStatus(), "Status should be Sent.");
    }

    @Test
    public void testSentMessageStatusDisregarded() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("Press 0 to delete the message", message.sentMessage(2));
        assertEquals("Disregarded", message.getMessageStatus(), "Status should be Disregarded.");
    }

    @Test
    public void testSentMessageStatusStored() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("Message successfully stored.", message.sentMessage(3));
        assertEquals("Stored", message.getMessageStatus(), "Status should be Stored.");
    }

    @Test
    public void testSentMessageInvalidOption() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("Invalid option selected.", message.sentMessage(9));
        assertEquals("Pending", message.getMessageStatus(), "Status should stay Pending.");
    }

    // --- JSON Storage Tests ---

    @Test
    public void testStoreMessageJSONContainsAllFields() {
        Message message = new Message(4, "+27838968976", "Hello there");
        message.sentMessage(1);
        String json = message.storeMessageJSON();
        assertTrue(json.contains("\"messageID\": \"" + message.getMessageID() + "\""), "JSON should contain the message ID.");
        assertTrue(json.contains("\"messageNumber\": 4"), "JSON should contain the message number.");
        assertTrue(json.contains("\"recipientCell\": \"+27838968976\""), "JSON should contain the recipient cell.");
        assertTrue(json.contains("\"messageContent\": \"Hello there\""), "JSON should contain the message content.");
        assertTrue(json.contains("\"messageHash\": \"" + message.getMessageHash() + "\""), "JSON should contain the message hash.");
        assertTrue(json.contains("\"status\": \"Sent\""), "JSON should contain the status.");
    }
}
