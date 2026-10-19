/**
 * Single source of truth for South African international cell number validation.
 *
 * Registration (Login) and message recipients (Message) both delegate here so
 * the two can never disagree on the accepted format.
 */
public final class CellNumber {

    /**
     * International format: the country code +27 followed by 9 digits.
     * Change this one constant to change the accepted format everywhere.
     */
    public static final String INTERNATIONAL_REGEX = "^\\+27[0-9]{9}$";

    /** Total length of a valid number: "+27" plus 9 digits. */
    public static final int EXPECTED_LENGTH = 12;

    private CellNumber() {
    }

    /**
     * Checks that the given cell number uses the international format.
     *
     * @param cellNumber the number to validate, may be null.
     * @return true when the number is a valid international cell number.
     */
    public static boolean isValid(String cellNumber) {
        if (cellNumber == null) {
            return false;
        }
        return cellNumber.matches(INTERNATIONAL_REGEX);
    }
}
