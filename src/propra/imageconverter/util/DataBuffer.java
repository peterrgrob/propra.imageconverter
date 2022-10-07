package propra.imageconverter.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public class DataBuffer {
    /**
     * 
     */
    protected ByteBuffer buffer;

    /**
     *
     */
    public DataBuffer() {
    }

    /**
     *
     * @param data
     */
    public DataBuffer(byte[] data) {
        super();
        buffer = ByteBuffer.wrap(data);
    }
    
    /**
     *
     * @param size
     */
    public void create(int size) {
        if(size <= 0) {
            throw new IllegalArgumentException();
        }
        buffer = ByteBuffer.allocate(size);
    }
    
    /**
     *
     * @return
     */
    public boolean isValid() {
        return (buffer != null);
    }
    
    /**
     *
     * @return
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    /**
     *
     * @param string
     * @param offset
     * @throws UnsupportedEncodingException
     */
    public void put(String string, int offset) throws UnsupportedEncodingException {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        
        buffer.put(string.getBytes("UTF-8"),
                    offset
                    ,string.length());
    }
   
    
    /**
     *
     * @param len
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getString(int len) throws UnsupportedEncodingException {
        byte[] str = new byte[len]; 
        buffer.get(str);
        return new String(str,"utf-8");
    }
    
    /**
     *
     * @param src
     * @param dst
     * @return
     */
    public static byte[] copyColor(byte[] src, byte[] dst) {
        if (src == null || dst == null) {
            throw new IllegalStateException();
        }
        
        dst[0] = src[0];
        dst[1] = src[1];
        dst[2] = src[2];
        return dst;
    }
    
    /**
     *
     * @param src
     * @param dst
     * @return
     */
    public static byte[] colorToLittleEndian(byte[] src, byte[] dst) {
        if (src == null || dst == null) {
            throw new IllegalStateException();
        }
        
        dst[0] = src[2];
        dst[1] = src[1];
        dst[2] = src[0];
        return dst;
    }
    
    /**
     *
     * @param b1
     * @param b2
     * @return
     */
    public static short bytesToShortLittle(byte b1, byte b2) {
        return (short)( (b1 << 8) | 
                        (b2 & 0x00FF));
    }
    
    /**
     *
     * @param buffer
     * @param offset
     * @return
     */
    public static int bytesToInt(byte[] buffer, int offset) {
        return (((buffer[offset]     & 0xFF) << 24)  | 
                ((buffer[offset + 1] & 0xFF) << 16)  |
                ((buffer[offset + 2] & 0xFF) << 8)   |
                  buffer[offset + 3] & 0xFF);
    }
    
    /**
     *
     * @param buffer
     * @param offset
     * @return
     */
    public static int bytesToIntLittle(byte[] buffer, int offset) {
        return (((buffer[offset + 3] & 0xFF) << 24)  | 
                ((buffer[offset + 2] & 0xFF) << 16)  |
                ((buffer[offset + 1] & 0xFF) << 8)   |
                  buffer[offset]     & 0xFF);
    }
    
    /**
     *
     * @param buffer
     * @param offset
     * @return
     */
    public static long bytesToLong(byte[] buffer, int offset) {
        return  (((long)(bytesToInt(buffer, offset) & 0xFFFFFFFF)) << 4) |
                 (long)bytesToInt(buffer, offset + 4) & 0xFFFFFFFF;
    }
    
    /**
     *
     * @param buffer
     * @param offset
     * @return
     */
    public static long bytesToLongLittle(byte[] buffer, int offset) {
        return (((long)(buffer[offset + 7] & 0xFF) << 56)  | 
                ((long)(buffer[offset + 6] & 0xFF) << 48)  |
                ((long)(buffer[offset + 5] & 0xFF) << 40)  |
                ((long)(buffer[offset + 4] & 0xFF) << 32   |
                ((long)(buffer[offset + 3] & 0xFF) << 24)  | 
                ((long)(buffer[offset + 2] & 0xFF) << 16)  |
                ((long)(buffer[offset + 1] & 0xFF) << 8)   |
                 (long)buffer[offset]      & 0xFF));
    }
}
