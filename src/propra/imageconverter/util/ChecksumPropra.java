package propra.imageconverter.util;

import java.nio.ByteBuffer;

/**
 * ProPra Implementierung einer Prüfsumme
 * 
 * @author pg
 */
public class ChecksumPropra extends Checksum {

    // Modulo
    private static final int X = 65521;
    
    // Aktuelle Prüfsummenvariablen
    private int currAi;
    private int currBi = 1;
    private int currIndex;

    /**
     * 
     */
    @Override
    public void reset() {
        currAi = 0;
        currBi = 1;
        currIndex = 0;
    }
    
    /**
     *
     */
    @Override
    public long getValue() {
        value = (currAi << 16) + currBi;
        return value;
    }
    
    /**
     * Aktualisiert Prüfsumme mit der ProPra-Prüfsummenvorschrift
     * 
     * @param buffer
     * @return 
     */
    @Override
    public ByteBuffer update(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        update(buffer.array(), buffer.position(), buffer.limit());
        return buffer;
    }

    /**
     * Aktualisiert Prüfsumme mit der ProPra-Prüfsummenvorschrift
     * 
     * @param b
     * @param offset
     * @param len 
     */
    @Override
    public void update(byte[] b, int offset, int len) {
        if (b == null) {
            throw new IllegalArgumentException();
        }
        
        int dindex = offset;
        for(int i=1; i<=len; i++) {
            currAi = (i + currIndex + currAi + (b[dindex++] & 0xFF)) % X;
            currBi = (currBi + currAi) % X; 
        }  
        
        currIndex += len;
    }

    /**
     * Aktualisiert Prüfsumme mit der ProPra-Prüfsummenvorschrift
     * 
     * @param b 
     */
    @Override
    public void update(byte b) {
        currAi = (++currIndex + currAi + (b & 0xFF)) % X;
        currBi = (currBi + currAi) % X; 
    }
}
