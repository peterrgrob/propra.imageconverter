package propra.imageconverter.data;

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
    public void reset() {
        super.reset();
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
    public DataBuffer filter(DataBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        
        int dindex = 0;
        byte[] data = buffer.getBytes();
        
        for(int i=1; i<=buffer.getCurrDataLength(); i++) {
            currAi = (i + currIndex + currAi + (data[dindex++] & 0xFF)) % X;
            currBi = (currBi + currAi) % X; 
        }  
        
        currIndex += buffer.getCurrDataLength();
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
    @Override
    public boolean isValid() {
        return true;
    }
}
