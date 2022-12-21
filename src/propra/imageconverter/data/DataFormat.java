package propra.imageconverter.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 */
public class DataFormat {
    
    // Aktuelle Kodierung
    protected Encoding encoding = Encoding.NONE;
    
    // Kodierungstypen der Daten
    public enum Encoding {
        NONE,
        RLE,
        BASEN,
        HUFFMAN;
    }
    
    // Datenoperation
    public enum Operation {
        ENCODE,
        DECODE,
        ENCODER_ANALYZE,
        DECODER_ANALYZE,
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
        this.encoding(encoding);
    } 
    
    /**
     * 
     * @param src
     */
    public DataFormat(DataFormat src) {
        this.encoding = src.encoding;
    }
    
    /**
     * 
     * @param encoding
     * @return  
     */
    public DataFormat encoding(Encoding encoding) {
        this.encoding = encoding;
        return this;
    }
    
    /**
     * 
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
     * Prüft ob ein Bit gesetzt ist
     * @param value
     * @param bit
     * @return 
     */
    public static boolean checkBit( byte value, 
                                    byte bit) {
        return ((value >> bit) & 1) == 0;
    }
    
    /**
     * Konvertiert Byte Array in einen Long Wert 
     * @param bytes
     * @param offset
     * @param len
     * @return 
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
     * @param value
     * @param byteCount
     * @return 
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
     * @param buffer
     * @param offset
     * @param string
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
     * @throws java.io.UnsupportedEncodingException 
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
