package propra.imageconverter.util;

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
     *
     */
    public void reset() {
        value = 0;
    }
    
    /**
     * Berechnet die Prüfsumme über Bytes
     * 
     * @param data
     * @return
     */
    public long check(byte[] data) {
        reset();
        return update(data);
    }
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     * 
     * @param data
     * @return 
     */
    public long update(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        return update(data, 0, data.length);
    }

    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     * 
     * @param data
     * @param offset
     * @param len
     * @return 
     */
    public abstract long update(byte [] data, int offset, int len);
}
