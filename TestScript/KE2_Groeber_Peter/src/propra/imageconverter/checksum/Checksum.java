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
        begin();
        apply(buffer);
        end();
        return getValue();
    }
    
    /**
     *
     */
    @Override
    public void begin() {
        value = 0;
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
    public abstract ByteBuffer apply(ByteBuffer in);
    
    /**
     *
     * @param in
     * @param out
     * @return 
     */
    @Override
    public ByteBuffer apply(ByteBuffer in, ByteBuffer out) {
        return apply(in);
    }
}
