package propra.imageconverter.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.image.ColorType;

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
        wrap(data);
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
     * @param data
     * @return 
     */
    public ByteBuffer wrap(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        buffer = ByteBuffer.wrap(data);
        return buffer;
    }
    
    /**
     *
     * @param data
     * @param byteOrder
     * @return 
     */
    public ByteBuffer wrap(byte[] data, ByteOrder byteOrder) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        buffer = ByteBuffer.wrap(data);
        buffer.order(byteOrder);
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
}
