package propra.imageconverter.util;

import java.nio.ByteBuffer;

/**
 * Basisklasse für Prüfsummen-Algorithmen
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
     * @param value
     */
    public Checksum(int value) {
        this.value = value;
    }

    /**
     *
     * @return
     */
    public long getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue(long value) {
        this.value = value;
    }
    
    /**
     * Berechnet die Prüfsumme über Bytes
     * 
     * @param buffer
     * @return
     */
    public long check(ByteBuffer buffer) {
        reset();
        update(buffer);
        getValue();
        return getValue();
    }
    
    /**
     *
     */
    public void reset() {
        value = 0;
    }
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     */
    public abstract ByteBuffer update(ByteBuffer in);
    
    public abstract void update(byte[] b, int offset, int len);
    
    /**
     * 
     */
    public abstract void update(byte b);
}
