package propra.imageconverter.util;

/**
 *  ProPra Implementierung einer Prüfsumme
 * 
 * @author pg
 */
public class ChecksumPropra extends Checksum {

    private static final int X = 65521;
    int a_i;
    int b_i = 1;
    int index;
    
        
    /**
     *
     */
    @Override
    public void reset() {
        super.reset();
        a_i = 0;
        b_i = 1;
        index = 0;
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
            a_i = (i + a_i + Byte.toUnsignedInt(data[dindex++])) % X;
            b_i = (b_i + a_i) % X; 
        }   
        return (value = (a_i << 16) + b_i);
    }

    /**
     *
     * @return
     */
    @Override
    public long finish() {
        return (value = (a_i << 16) + b_i);
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
            a_i = (i + index + a_i + Byte.toUnsignedInt(data[dindex++])) % X;
            b_i = (b_i + a_i) % X; 
        }  
        
        index += len;
    }
}
