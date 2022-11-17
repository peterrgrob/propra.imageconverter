package propra.imageconverter.data;


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
    public static long bytesToLong(byte[] bytes, int offset, int len) {
        long value = 0;
        for(int i=0; i<len; i++) {
            value <<= 8;
            value += (int)(bytes[i + offset] & 0xFF);
        }
        return value;
    }
    
    
    static public byte[] longToBytes(long value, int len) {
        byte[] nb = new byte[len];
        for(int i=0; i<len;i++) {
            nb[len - i - 1] = (byte)(value & 0xFF);
            value >>= 8;
        }
        return nb;
    }
}
