package propra.imageconverter.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 */
public class DataFormat {
    
    // Gesetzte Kodierung
    protected Encoding encoding = Encoding.NONE;
    
    // Kodierungstypen der Daten
    public enum Encoding {
        NONE,
        RLE,
        BASEN,
        HUFFMAN;
    }

    
    // Datenmodus für Ein- und Ausgabe
    public enum IOMode {
        BINARY,
        TEXT;
    }
    
    // Datenoperation
    public enum Operation {
        READ,
        WRITE,
        ENCODE,
        DECODE,
        ANALYZE,
        NONE;
    }

    /**
     *
     */
    public DataFormat() {
    }
    
    /**
     *
     */
    public DataFormat(Encoding encoding) {
        this.encoding(encoding);
    } 
    
    /**
     * 
     */
    public DataFormat(DataFormat src) {
        this.encoding = src.encoding;
    }
    
    /**
     * 
     */
    public DataFormat encoding(Encoding encoding) {
        this.encoding = encoding;
        return this;
    }
    
    /**
     * 
     */
    public Encoding encoding() {
        return encoding;
    }
    
    /**
     *
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * 
     */
    public boolean isBinary() {
        return (encoding == Encoding.RLE);
    }
    
    /**
     * Prüft ob ein Bit gesetzt ist
     */
    public static boolean checkBit( byte value, 
                                    byte bit) {
        return ((value >> bit) & 1) == 0;
    }
    
    /**
     * Konvertiert Byte Array in einen Long Wert 
     */
    public static long bytesToLong( byte[] bytes, 
                                    int offset, 
                                    int len) {
        long value = 0;
        for(int i=0; i<len; i++) {
            value <<= 8;
            value += (int)(bytes[i + offset] & 0xFF);
        }
        return value;
    }
    
    /**
     * Konvertiert Bytes eines long Wertes zu einem Byte Array
     */
    static public byte[] longToBytes(   long value, 
                                        int byteCount) {
        byte[] nb = new byte[byteCount];
        for(int i=0; i<byteCount;i++) {
            nb[byteCount - i - 1] = (byte)(value & 0xFF);
            value >>= 8;
        }
        return nb;
    }
    
    /**
     *
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
     */
    public static String getStringFromByteBuffer(   ByteBuffer buffer, 
                                                    int len) throws UnsupportedEncodingException {
        if (len <= 0 
        ||  buffer == null) {
            throw new IllegalArgumentException();
        }
        byte[] str = new byte[len]; 
        buffer.get(str);
        return new String(str,"utf-8");
    }
}
