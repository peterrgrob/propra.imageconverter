package propra.imageconverter.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public class DataFormat {
    
    protected Encoding encoding = Encoding.NONE;
    
    // Kodierung der Daten
    public enum Encoding {
        NONE,
        RLE,
        BASEN;
    }

    
    // Datenmodus für Ein- und Ausgabe
    public enum IOMode {
        BINARY,
        TEXT;
    }
    
    // Datenoperation
    public enum Operation {
        ENCODE,
        DECODE,
        FILTER,
        NONE;
    }

    /**
     *
     */
    public DataFormat() {
        
    }
    
    /**
     *
     * @param encoding
     */
    public DataFormat(Encoding encoding) {
        encoding(encoding);
    } 
    
    /**
     * 
     * @param src
     */
    public DataFormat(DataFormat src) {
        this.encoding = src.encoding;
    }
    
    /**
     * @param encoding
     */
    public void encoding(Encoding encoding) {
        this.encoding = encoding;
    }
    
    /*
     * @return
     */
    public Encoding encoding() {
        return encoding;
    }
    
    /**
     *
     * @return
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * 
     * @return 
     */
    public boolean isBinary() {
        return (encoding == Encoding.RLE);
    }
    
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
     * @param bytes
     * @param offset
     * @param len
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
    
    /**
     * 
     * @param value
     * @param len
     * @return 
     */
    static public byte[] longToBytes(long value, int len) {
        byte[] nb = new byte[len];
        for(int i=0; i<len;i++) {
            nb[len - i - 1] = (byte)(value & 0xFF);
            value >>= 8;
        }
        return nb;
    }
    
        /**
     *
     * @param buffer
     * @param string
     * @param offset
     */
    public static void putStringToByteBuffer(   ByteBuffer buffer, 
                                                int offset, 
                                                String string) {
        try {
            buffer.put(string.getBytes("UTF-8")
                        , offset
                        ,string.length());
        } catch (UnsupportedEncodingException ex) {
            
        }
    }
    
        /**
     *
     * @param buffer
     * @param len
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getStringFromByteBuffer(ByteBuffer buffer, int len) throws UnsupportedEncodingException {
        if (len <= 0 
        ||  buffer == null) {
            throw new IllegalArgumentException();
        }
        byte[] str = new byte[len]; 
        buffer.get(str);
        return new String(str,"utf-8");
    }
}
