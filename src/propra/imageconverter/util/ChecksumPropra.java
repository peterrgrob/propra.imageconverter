package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class ChecksumPropra extends Checksum {
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
 
    public void test() {
        byte[] b = new byte[1];
        b[0] = 0;
        if(update(b) != 65538) {
            throw new IllegalArgumentException("Checksum test failed!");
        }
        reset();
        b = new byte[1];
        b[0] = 1;
        if(update(b) != 0x00020003) {
            throw new IllegalArgumentException("Checksum test failed!");
        } 
        reset();
        b = new byte[2];
        b[0] = (byte)255;
        b[1] = (byte)128;
        if(update(b) != 0x01820283) {
            throw new IllegalArgumentException("Checksum test failed!");
        } 
        reset();
        if(update((new String("Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")).getBytes()) != 0x079ED65E) {
            throw new IllegalArgumentException("Checksum test failed!");
        } 
        reset();
    }
}
