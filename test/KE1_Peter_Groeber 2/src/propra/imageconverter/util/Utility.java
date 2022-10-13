package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class Utility {

    /**
     *
     * @param value
     * @param bit
     * @return
     */
    public static boolean checkBit(byte value, byte bit) {
        return ((value >> bit) & 1) == 0;
    }
}
