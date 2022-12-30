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
 */
public class DataUtil {
    
    /**
     * Prüft ob ein Bit gesetzt ist
     */
    public static boolean checkBit(byte value, byte bit) {
        return ((value >> bit) & 1) == 0;
    }
    
    /**
     * Schreibt String in ByteBuffer
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
     * Liest String von ByteBuffer
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
    
    /**
     * Extrahiert Dateiendung des Dateinamens
     */
    static public String getExtension(String path) {
        String[] components = path.split("\\.");
        if(components.length < 2) {
            return "";
        }
        return components[components.length - 1].toLowerCase();
    } 
}
