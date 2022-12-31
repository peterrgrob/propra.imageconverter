package propra.imageconverter.checksum;

import java.nio.ByteBuffer;

/**
 * Interface für Prüfsummen
 */
public interface IChecksum {
    /**
     * Prüfsumme
     */
    public long getValue();
    
    /**
     *  Setzt Prüfsumme zurück
     */
    public void reset();
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     */ 
    public ByteBuffer update(ByteBuffer in);
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     */
    public void update(byte[] b, int offset, int len);
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit einem Byte 
     */
    public void update(byte b);
}
