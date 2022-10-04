package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class Utility {
    /**
     * 
     * @param b1
     * @param b2
     * @return 
     */
    public static int bytesToInt(byte b1, byte b2) {
        return (b1 << 8) | (b2 & 0x00FF);
    }
}
