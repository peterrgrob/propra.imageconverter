package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class ChecksumPropra extends Checksum {
    /**
     * 
     */
    private static final int X = 65521;
    
    /**
     *
     * @param data
     * @param offset
     * @param len
     * @return
     */
    @Override
    public long update(byte[] data, int offset, int len) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        
        int a_i = 0;
        int b_i = 1;
        int index = offset;
        
        for(int i=1;i<=len;i++) {
            a_i = (i + a_i + Byte.toUnsignedInt(data[index++])) % X;
            b_i = (b_i + a_i) % X; 
        }   
        return (value = (a_i << 16) + b_i);
    }
}
