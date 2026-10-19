import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small hand written JSON helper used to write and read the message file.
 *
 * Only the subset of JSON the application needs is supported: an array of flat
 * objects whose values are strings or numbers. Keeping it in the project removes
 * the need for an external JSON library.
 */
public final class Json {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private Json() {
    }

    /**
     * Escapes a value so it can be placed inside a JSON string literal.
     *
     * @param raw the text to escape, may be null.
     * @return the escaped text without the surrounding quotes.
     */
    public static String escape(String raw) {
        if (raw == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append("\\u")
                          .append(HEX[(c >> 12) & 0xF])
                          .append(HEX[(c >> 8) & 0xF])
                          .append(HEX[(c >> 4) & 0xF])
                          .append(HEX[c & 0xF]);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Wraps a value in quotes after escaping it.
     *
     * @param raw the text to quote, may be null.
     * @return a complete JSON string literal.
     */
    public static String quote(String raw) {
        return "\"" + escape(raw) + "\"";
    }

    /**
     * Parses a flat JSON object into a map of raw values.
     *
     * Numbers are returned as their literal text, strings are unescaped.
     *
     * @param json the object text, for example {@code {"messageID": "1234567890"}}.
     * @return the key/value pairs in the order they appear.
     * @throws IllegalArgumentException when the text is not a valid flat object.
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> values = new LinkedHashMap<>();
        if (json == null) {
            throw new IllegalArgumentException("JSON text is missing.");
        }

        int index = skipWhitespace(json, 0);
        if (index >= json.length() || json.charAt(index) != '{') {
            throw new IllegalArgumentException("Expected a JSON object but found: " + json);
        }
        index++;

        while (true) {
            index = skipWhitespace(json, index);
            if (index >= json.length()) {
                throw new IllegalArgumentException("Unterminated JSON object: " + json);
            }
            if (json.charAt(index) == '}') {
                return values;
            }

            Value key = readString(json, index);
            index = skipWhitespace(json, key.second);

            if (index >= json.length() || json.charAt(index) != ':') {
                throw new IllegalArgumentException("Expected ':' after key '" + key.first + "' in: " + json);
            }
            index = skipWhitespace(json, index + 1);

            String value;
            if (index < json.length() && json.charAt(index) == '"') {
                Value parsed = readString(json, index);
                value = parsed.first;
                index = parsed.second;
            } else {
                int end = index;
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                    end++;
                }
                value = json.substring(index, end).trim();
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Missing value for key '" + key.first + "' in: " + json);
                }
                index = end;
            }

            values.put(key.first, value);

            index = skipWhitespace(json, index);
            if (index >= json.length()) {
                throw new IllegalArgumentException("Unterminated JSON object: " + json);
            }
            char next = json.charAt(index);
            if (next == ',') {
                index++;
                continue;
            }
            if (next == '}') {
                return values;
            }
            throw new IllegalArgumentException("Expected ',' or '}' in: " + json);
        }
    }

    /**
     * Extracts the objects of a JSON array.
     *
     * @param json the array text, for example {@code [{...}, {...}]}.
     * @return the raw text of every object in the array, in order.
     * @throws IllegalArgumentException when the text is not a valid array.
     */
    public static List<String> parseArray(String json) {
        List<String> elements = new ArrayList<>();
        if (json == null) {
            throw new IllegalArgumentException("JSON text is missing.");
        }

        int open = json.indexOf('[');
        int close = json.lastIndexOf(']');
        if (open < 0 || close < open) {
            throw new IllegalArgumentException("Expected a JSON array but found: " + json);
        }

        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int i = open + 1; i < close; i++) {
            char c = json.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    elements.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }

        return elements;
    }

    /**
     * Moves the cursor past any whitespace.
     */
    private static int skipWhitespace(String text, int index) {
        int i = index;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * Reads a quoted string.
     *
     * @return the unescaped value and the index just after the closing quote.
     * @throws IllegalArgumentException when the text does not start with a quote.
     */
    private static Value readString(String text, int index) {
        if (index >= text.length() || text.charAt(index) != '"') {
            throw new IllegalArgumentException("Expected a quoted string in: " + text);
        }

        StringBuilder sb = new StringBuilder();
        int i = index + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char escaped = text.charAt(i + 1);
                switch (escaped) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'u':
                        if (i + 5 < text.length()) {
                            sb.append((char) Integer.parseInt(text.substring(i + 2, i + 6), 16));
                            i += 4;
                        }
                        break;
                    default:
                        sb.append(escaped);
                }
                i += 2;
                continue;
            }
            if (c == '"') {
                return new Value(sb.toString(), i + 1);
            }
            sb.append(c);
            i++;
        }

        throw new IllegalArgumentException("Unterminated string in: " + text);
    }

    /**
     * A parsed value together with the position that follows it.
     */
    private static final class Value {
        private final String first;
        private final int second;

        private Value(String first, int second) {
            this.first = first;
            this.second = second;
        }
    }
}
