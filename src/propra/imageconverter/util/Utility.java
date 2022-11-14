package propra.imageconverter.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;

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
    
    /**
     *
     * @param array
     * @param byteIndex
     * @param bitIndex
     * @param bitLen
     * @return
     */
    public static long bytesToLong(byte[] bytes, int len) {
        long value = 0;
        for(int i=0; i<len; i++) {
            value <<= 8;
            value += bytes[i];
        }
        return value;
    }
}
