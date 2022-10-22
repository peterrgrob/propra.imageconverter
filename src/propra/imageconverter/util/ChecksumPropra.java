package propra.imageconverter.util;

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
     *
     * @param data
     * @return
     */
    @Override
    public long check(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        
        int dindex = 0;
        
        for(int i=1; i<=data.length; i++) {
            currAi = (i + currAi + Byte.toUnsignedInt(data[dindex++])) % X;
            currBi = (currBi + currAi) % X; 
        }   
        return (value = (currAi << 16) + currBi);
    }
    
    /**
     *  Aktualisiert Prüfsumme mit ProPra-Prüfsummen Verfahren, wie vorgegeben
     * 
     * @param data
     * @param offset
     * @param len
     */
    @Override
    public void update(byte[] data, int offset, int len) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        
        int dindex = offset;
        
        for(int i=1; i<=len; i++) {
            currAi = (i + currIndex + currAi + Byte.toUnsignedInt(data[dindex++])) % X;
            currBi = (currBi + currAi) % X; 
        }  
        
        currIndex += len;
    }
    
    /**
     *
     * @return
     */
    @Override
    public long end() {
        return (value = (currAi << 16) + currBi);
    }
}
