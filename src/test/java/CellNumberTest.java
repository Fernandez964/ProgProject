import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CellNumberTest {

    @Test
    public void testIsValidAcceptsInternationalFormat() {
        assertTrue(CellNumber.isValid("+27838968976"), "An international number should pass.");
    }

    @Test
    public void testIsValidAcceptsLocalFormat() {
        assertTrue(CellNumber.isValid("0838968976"), "A local number should pass.");
    }

    @Test
    public void testIsValidRejectsMalformedNumbers() {
        assertFalse(CellNumber.isValid(null), "A missing number should fail.");
        assertFalse(CellNumber.isValid(""), "An empty number should fail.");
        assertFalse(CellNumber.isValid("08966553"), "A number that is too short should fail.");
        assertFalse(CellNumber.isValid("+2783896897"), "An international number that is too short should fail.");
        assertFalse(CellNumber.isValid("+278389689760"), "An international number that is too long should fail.");
        assertFalse(CellNumber.isValid("08389689760"), "A local number that is too long should fail.");
        assertFalse(CellNumber.isValid("27838968976"), "A number without a country code should fail.");
    }

    @Test
    public void testNormalizeConvertsLocalNumberToInternational() {
        assertEquals("+27838968976", CellNumber.normalize("0838968976"),
                "A local number should be normalised to the international format.");
    }

    @Test
    public void testNormalizeKeepsInternationalNumber() {
        assertEquals("+27838968976", CellNumber.normalize("+27838968976"),
                "An international number should be kept as it is.");
    }

    @Test
    public void testNormalizeHandlesMissingNumber() {
        assertNull(CellNumber.normalize(null), "A missing number stays missing.");
    }

    @Test
    public void testIsInternationalOnlyAcceptsCountryCode() {
        assertTrue(CellNumber.isInternational("+27838968976"), "An international number should pass.");
        assertFalse(CellNumber.isInternational("0838968976"), "A local number should not pass.");
    }
}
