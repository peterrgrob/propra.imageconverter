package propra.imageconverter.data;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Diverse Utility Methoden zur Datenverarbeitung
 * @author pg
 */
public class DataUtil {
    
    /**
     * Prüft ob ein Bit gesetzt ist
     * @param value
     * @param bit
     * @return 
     */
    public static boolean checkBit(byte value, byte bit) {
        return ((value >> bit) & 1) == 0;
    }
    
    /**
     * Konvertiert Byte Array in einen Long Wert 
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
     * Konvertiert Bytes eines long Wertes zu einem Byte Array
     * @param value
     * @param byteCount
     * @return 
     */
    static public byte[] longToBytes(long value, int byteCount) {
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
    public static void putStringToByteBuffer(ByteBuffer buffer, 
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
    public static String getStringFromByteBuffer(ByteBuffer buffer, int len) throws UnsupportedEncodingException {
        if (len <= 0 
        ||  buffer == null) {
            throw new IllegalArgumentException();
        }
        byte[] str = new byte[len]; 
        buffer.get(str);
        return new String(str,"utf-8");
    }
    
    /**
     * Verzeichnisse und Datei erstellen, falls nötig
     * @param filePath
     * @return 
     * @throws java.io.IOException
     */
    public static File createFileAndDirectory(String filePath) throws IOException {
     
        Path outDirs = Paths.get(filePath);
        Files.createDirectories(outDirs.getParent());
        
        File file = new File(filePath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        return file;
    }
}
