/**
 * Single source of truth for South African cell number validation.
 *
 * Registration (Login) and message recipients (Message) both delegate here so
 * the two can never disagree on the accepted format.
 */
public final class CellNumber {

    /**
     * International format: the country code +27 followed by 9 digits.
     */
    public static final String INTERNATIONAL_REGEX = "^\\+27[0-9]{9}$";

    /**
     * Accepted format: either the international format (+27 followed by 9
     * digits) or the local format (a 0 followed by 9 digits).
     */
    public static final String ACCEPTED_REGEX = "^(?:\\+27|0)[0-9]{9}$";

    /**
     * Local format: a 0 followed by 9 digits.
     */
    public static final String LOCAL_REGEX = "^0[0-9]{9}$";

    /** Total length of a valid international number: "+27" plus 9 digits. */
    public static final int EXPECTED_LENGTH = 12;

    private CellNumber() {
    }

    /**
     * Checks that the given cell number uses an accepted format.
     *
     * @param cellNumber the number to validate, may be null.
     * @return true when the number is a valid cell number.
     */
    public static boolean isValid(String cellNumber) {
        if (cellNumber == null) {
            return false;
        }
        return cellNumber.trim().matches(ACCEPTED_REGEX);
    }

    /**
     * Checks that the given cell number uses the international format only.
     *
     * @param cellNumber the number to validate, may be null.
     * @return true when the number starts with +27 and has 9 digits.
     */
    public static boolean isInternational(String cellNumber) {
        if (cellNumber == null) {
            return false;
        }
        return cellNumber.trim().matches(INTERNATIONAL_REGEX);
    }

    /**
     * Normalises a cell number to the international format.
     *
     * A local number that starts with 0 becomes +27 followed by the remaining
     * digits, every other number is returned unchanged.
     *
     * @param cellNumber the number to normalise, may be null.
     * @return the number in the international format.
     */
    public static String normalize(String cellNumber) {
        if (cellNumber == null) {
            return null;
        }

        String trimmed = cellNumber.trim();
        if (trimmed.matches(LOCAL_REGEX)) {
            return "+27" + trimmed.substring(1);
        }

        return trimmed;
    }
}
