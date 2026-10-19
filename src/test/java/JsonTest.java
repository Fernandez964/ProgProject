import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonTest {

    @Test
    public void testEscapeKeepsPlainTextUnchanged() {
        assertEquals("Hello there", Json.escape("Hello there"));
        assertEquals("", Json.escape(null));
    }

    @Test
    public void testEscapeHandlesQuotesAndLineBreaks() {
        assertEquals("He said \\\"hi\\\"", Json.escape("He said \"hi\""));
        assertEquals("line one\\nline two", Json.escape("line one\nline two"));
        assertEquals("back\\\\slash", Json.escape("back\\slash"));
        assertEquals("tab\\there", Json.escape("tab\there"));
    }

    @Test
    public void testQuoteWrapsEscapedText() {
        assertEquals("\"Hello there\"", Json.quote("Hello there"));
        assertEquals("\"He said \\\"hi\\\"\"", Json.quote("He said \"hi\""));
    }

    @Test
    public void testParseObjectReadsValues() {
        Map<String, String> values = Json.parseObject(
                "{\n  \"messageID\": \"1234567890\",\n  \"messageNumber\": 4,\n  \"status\": \"Sent\"\n}");

        assertEquals("1234567890", values.get("messageID"));
        assertEquals("4", values.get("messageNumber"));
        assertEquals("Sent", values.get("status"));
    }

    @Test
    public void testParseObjectUnescapesText() {
        Map<String, String> values = Json.parseObject("{\"messageContent\": \"He said \\\"hi\\\"\\nagain\"}");

        assertEquals("He said \"hi\"\nagain", values.get("messageContent"));
    }

    @Test
    public void testParseArrayReturnsEachObject() {
        List<String> elements = Json.parseArray("[\n  {\"messageID\": \"1\"},\n  {\"messageID\": \"2\"}\n]");

        assertEquals(2, elements.size(), "Both objects should be found.");
        assertEquals("{\"messageID\": \"1\"}", elements.get(0));
        assertEquals("{\"messageID\": \"2\"}", elements.get(1));
    }

    @Test
    public void testParseArrayKeepsBracesInsideStrings() {
        List<String> elements = Json.parseArray("[{\"messageContent\": \"a } brace\"}, {\"messageID\": \"2\"}]");

        assertEquals(2, elements.size(), "A brace inside a string should not split the object.");
        assertEquals("{\"messageContent\": \"a } brace\"}", elements.get(0));
    }

    @Test
    public void testParseObjectRejectsBrokenText() {
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject("not an object"));
        assertThrows(IllegalArgumentException.class, () -> Json.parseObject("{\"key\": }"));
    }
}
