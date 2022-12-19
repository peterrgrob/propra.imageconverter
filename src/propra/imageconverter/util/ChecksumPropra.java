package propra.imageconverter.util;

import java.nio.ByteBuffer;

/**
 *  ProPra Implementierung einer Prüfsumme
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
     *  Aktualisiert Prüfsumme mit ProPra-Prüfsummen Verfahren, wie vorgegeben
     * 
     * @param buffer
     */
    @Override
    public ByteBuffer update(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        
        int dindex = 0;
        byte[] data = buffer.array();
        
        for(int i=1; i<=buffer.limit(); i++) {
            currAi = (i + currIndex + currAi + (data[dindex++] & 0xFF)) % X;
            currBi = (currBi + currAi) % X; 
        }  
        
        currIndex += buffer.limit();
        return buffer;
    }
    
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
     * 
     */
    @Override
    public void update(byte b) {
        currAi = (++currIndex + currAi + (b & 0xFF)) % X;
        currBi = (currBi + currAi) % X; 
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
     *
     * @return
     */
    public boolean isValid() {
        return true;
    }
}
