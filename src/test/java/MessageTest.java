import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

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

    @Test
    public void testCheckRecipientCellValid() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertTrue(message.checkRecipientCell(), "A number in international format should pass.");
    }

    @Test
    public void testCheckRecipientCellAcceptsLocalFormat() {
        Message message = new Message(1, "0838968976", "Hello there");
        assertTrue(message.checkRecipientCell(), "A number in local format should pass.");
    }

    @Test
    public void testLocalCellNumberIsNormalisedToInternational() {
        Message message = new Message(1, "0838968976", "Hello there");
        assertEquals("+27838968976", message.getRecipientCell(),
                "A local number should be stored in the international format.");
    }

    @Test
    public void testInternationalCellNumberIsKept() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("+27838968976", message.getRecipientCell(),
                "An international number should be kept as it is.");
    }

    @Test
    public void testCheckRecipientCellInvalid() {
        assertFalse(new Message(1, "+2783896897", "Hello there").checkRecipientCell());
        assertFalse(new Message(1, "+278389689760", "Hello there").checkRecipientCell());
        assertFalse(new Message(1, "27838968976", "Hello there").checkRecipientCell());
        assertFalse(new Message(1, null, "Hello there").checkRecipientCell());
    }

    @Test
    public void testRecipientCellMatchesRegistrationFormat() {
        Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        Message message = new Message(1, login.getCellPhoneNumber(), "Hello there");
        assertTrue(login.checkCellPhoneNumber());
        assertTrue(message.checkRecipientCell());
    }

    @Test
    public void testCheckMessageLengthValid() {
        Message message = new Message(1, "+27838968976", "Short message");
        assertTrue(message.checkMessageLength());
    }

    @Test
    public void testCheckMessageLengthAllows250Characters() {
        Message message = new Message(1, "+27838968976", "a".repeat(250));
        assertTrue(message.checkMessageLength());
    }

    @Test
    public void testCheckMessageLengthTooLong() {
        String tooLong = "a".repeat(251);
        Message message = new Message(1, "+27838968976", tooLong);
        assertFalse(message.checkMessageLength());
    }

    @Test
    public void testLengthExceededMessage() {
        Message message = new Message(1, "+27838968976", "a".repeat(253));
        assertEquals("Message exceeds 250 characters by 3 characters; please reduce the size.",
                message.lengthExceededMessage());
    }

    @Test
    public void testCreateMessageHashFormat() {
        Message message = new Message(7, "+27838968976", "Hello world this is a test");
        String expected = message.getMessageID().substring(0, 2) + ":7:HELLOTEST";
        assertEquals(expected, message.getMessageHash());
    }

    @Test
    public void testCreateMessageHashWithSingleWord() {
        Message message = new Message(1, "+27838968976", "Hello");
        String expected = message.getMessageID().substring(0, 2) + ":1:HELLO";
        assertEquals(expected, message.getMessageHash());
    }

    @Test
    public void testCreateMessageHashKeepsDigitsInWords() {
        Message message = new Message(1, "+27838968976", "2024 was great");
        String expected = message.getMessageID().substring(0, 2) + ":1:2024GREAT";
        assertEquals(expected, message.getMessageHash());
    }

    @Test
    public void testCreateMessageHashWithSuppliedMessageID() {
        Message message = new Message(7, "+27838968976", "Hello world this is a test", "1234567890");
        assertEquals("1234567890", message.getMessageID());
        assertEquals("12:7:HELLOTEST", message.getMessageHash());
    }

    @Test
    public void testWithMessageIdFactory() {
        Message message = Message.withMessageId(2, "0838968976", "Send later", "9876543210");
        assertEquals("9876543210", message.getMessageID());
        assertEquals("98:2:SENDLATER", message.getMessageHash());
        assertEquals("+27838968976", message.getRecipientCell());
    }

    @Test
    public void testSentMessageStatusSent() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("Message ready to send.", message.sentMessage(1));
        assertEquals("Sent", message.getMessageStatus());
    }

    @Test
    public void testSentMessageStatusDisregarded() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("Press 0 to delete the message.", message.sentMessage(2));
        assertEquals("Disregarded", message.getMessageStatus());
    }

    @Test
    public void testSentMessageStatusStored() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("Message ready to send.", message.sentMessage(3));
        assertEquals("Stored", message.getMessageStatus());
    }

    @Test
    public void testSentMessageInvalidOption() {
        Message message = new Message(1, "+27838968976", "Hello there");
        assertEquals("Invalid option selected.", message.sentMessage(9));
        assertEquals("Pending", message.getMessageStatus());
    }

    @Test
    public void testStoreMessageJSONContainsAllFields() {
        Message message = new Message(4, "+27838968976", "Hello there");
        message.sentMessage(1);
        String json = message.storeMessageJSON();
        assertTrue(json.contains("\"messageID\": \"" + message.getMessageID() + "\""));
        assertTrue(json.contains("\"messageNumber\": 4"));
        assertTrue(json.contains("\"recipientCell\": \"+27838968976\""));
        assertTrue(json.contains("\"messageContent\": \"Hello there\""));
        assertTrue(json.contains("\"messageHash\": \"" + message.getMessageHash() + "\""));
        assertTrue(json.contains("\"status\": \"Sent\""));
    }
}
