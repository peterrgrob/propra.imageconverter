package propra.imageconverter.checksum;

import propra.imageconverter.data.DataBuffer;

/**
 *  ProPra Implementierung einer Prüfsumme
 * 
 * @author pg
 */
public class ChecksumPropra extends Checksum {

    private static final int X = 65521;
    private int currAi;
    private int currBi = 1;
    private int currIndex;

    /**
     * 
     */
    @Override
    public void begin() {
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
    public DataBuffer apply(DataBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        
        int dindex = 0;
        byte[] data = buffer.getBytes();
        
        for(int i=1; i<=buffer.getDataLength(); i++) {
            currAi = (i + currIndex + currAi + (data[dindex++] & 0xFF)) % X;
            currBi = (currBi + currAi) % X; 
        }  
        
        currIndex += buffer.getDataLength();
        return buffer;
    }
    
    /**
     *
     * @return
     */
    @Override
    public void end() {
        value = (currAi << 16) + currBi;
    }

    /**
     *
     * @return
     */
    public boolean isValid() {
        return true;
    }
}
