package propra.imageconverter.checksum;

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
        begin();
        update(buffer);
        end();
        return getValue();
    }
    
    /**
     *
     */
    public void begin() {
        value = 0;
    }
    
    /**
     *
     */
    public abstract void end();
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     * 
     * @param in
     * @return 
     */
    public abstract ByteBuffer update(ByteBuffer in);
}
