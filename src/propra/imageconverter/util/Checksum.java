package propra.imageconverter.util;

import java.nio.ByteBuffer;

/**
 * Basisklasse für Prüfsummen
 *
 * @author pg
 */
public abstract class Checksum {

    /**
     * Aktuelle Prüfsumme
     */
    protected long value;    

    /**
     * 
     */
    public Checksum() {   
    }

    /**
     * 
     */
    public long getValue() {
        return value;
    }
    
    /**
     *  Setzt Prüfsumme zurück
     */
    public void reset() {
        value = 0;
    }
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     * 
     * @param in
     * @return 
     */
    public abstract ByteBuffer update(ByteBuffer in);
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     * 
     * @param b
     * @param offset
     * @param len 
     */
    public abstract void update(byte[] b, int offset, int len);
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit einem Byte 
     * 
     * @param b 
     */
    public abstract void update(byte b);
}
