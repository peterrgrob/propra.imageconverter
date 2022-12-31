package propra.imageconverter.checksum;

import java.nio.ByteBuffer;

/**
 *  Basis Prüfsumme die nichts tut
 */
public class ChecksumNull implements IChecksum {
    
    // Aktuelle Prüfsumme
    protected long value;  

    /**
     * Gibt Prüfsumme zurück
     */
    @Override
    public long getValue() {
        return value;
    }
    
    /**
     * Setzt Prüfsumme zurück
     */
    @Override
    public void reset() {
        value = 0;
    }
    
    @Override
    public ByteBuffer update(ByteBuffer in) {return in;}
    @Override
    public void update(byte[] b, int offset, int len) {}
    @Override
    public void update(byte b) {}
}
