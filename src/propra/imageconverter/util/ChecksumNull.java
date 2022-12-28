package propra.imageconverter.util;

import java.nio.ByteBuffer;

/**
 *  Dummy Prüfsumme die nichts tut
 */
public class ChecksumNull implements IChecksum {
    
    // Aktuelle Prüfsumme
    protected long value;  

    @Override
    public long getValue() {
        return value;
    }
    
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
