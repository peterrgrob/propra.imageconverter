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
    public static long extractBits( ByteBuffer data, 
                                    int byteIndex, 
                                    int bitIndex, 
                                    int bitLen) {
        
        // nächstgrößere Blockgröße in Bytes ermitteln
        int blockSize = (bitLen / 8);
        if(bitLen % 8 != 0) {
            blockSize += 1;
        }
                
        // Block auslesen
        byte[] result = new byte[blockSize];
        data.get(byteIndex, result);
        long value = new BigInteger(result).longValue();
        value = value << 64 - bitIndex;
        return value >> (bitIndex - bitLen);
    }
}
