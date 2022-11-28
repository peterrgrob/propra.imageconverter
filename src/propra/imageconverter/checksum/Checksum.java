package propra.imageconverter.checksum;

import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataFilter;

/**
 * Basisklasse für Prüfsummen-Algorithmen
 * 
 * @author pg
 */
public abstract class Checksum implements IDataFilter {

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
        beginFilter();
        apply(buffer);
        endFilter();
        return getValue();
    }
    
    /**
     *
     */
    @Override
    public void beginFilter() {
        value = 0;
    }
    
    /**
     *
     */
    @Override
    public abstract void endFilter();
    
    /**
     * Aktualisiert die aktuelle Prüfsumme mit Bytes 
     * 
     * @param in
     * @return 
     */
    @Override
    public abstract ByteBuffer apply(ByteBuffer in);
}
