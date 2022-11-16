package propra.imageconverter.data;

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
     * @param buffer
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
    @Override
    public void begin() {
        reset();
    }
    
    /**
     *
     */
    @Override
    public abstract void end();
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     * 
     * @param in
     * @return 
     */
    @Override
    public abstract DataBuffer filter(DataBuffer in);
    
    /**
     *
     * @param in
     * @param out
     * @return 
     */
    @Override
    public DataBuffer filter(DataBuffer in, DataBuffer out) {
        return filter(in);
    }
}
