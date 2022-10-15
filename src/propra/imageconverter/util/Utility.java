package propra.imageconverter.util;

/**
 * Diverse Utility Methoden
 * 
 * @author pg
 */
public class Utility {

    /**
     * Prüft ob ein bestimmtes Bit gesetzt ist.
     * 
     * @param value
     * @param bit
     * @return
     */
    public static boolean checkBit(byte value, byte bit) {
        return ((value >> bit) & 1) == 0;
    }
}
