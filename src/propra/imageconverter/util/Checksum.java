package propra.imageconverter.util;

/**
 * Basisklasse für Prüfsummen-Algorithmen
 * 
 * @author pg
 */
public abstract class Checksum implements DataFilter {

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
    public long check(DataBuffer buffer) {
        begin();
        filter(buffer);
        end();
        return getValue();
    }
    
    /**
     *
     */
    public void begin() {
        reset();
    }
    
    /**
     *
     * @return
     */
    public abstract void end();
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     * 
     * @param in
     */
    @Override
    public abstract DataBuffer filter(DataBuffer in);
    
    /**
     *
     * @param in
     * @param out
     */
    @Override
    public DataBuffer filter(DataBuffer in, DataBuffer out) {
        return filter(in);
    }
}
