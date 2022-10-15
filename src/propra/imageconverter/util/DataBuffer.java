package propra.imageconverter.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *  Hilfsklasse zur Datenverwaltung, kapselt intern einen ByteBuffer und
 *  erweitert diesen mit zusätzlichen Methoden.
 * 
 * @author pg
 */
public class DataBuffer {

    protected ByteBuffer buffer;

    /**
     *
     */
    public DataBuffer() {
    }
    
    /**
     *
     * @param len
     */
    public DataBuffer(int len) {
        super();
        create(len);
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
     * @return
     */
    public byte[] getBytes() {
        if (buffer == null) {
            throw new IllegalArgumentException(); 
        }
        return buffer.array();
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
     */
    public void put(String string, int offset) {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        
        try {
            buffer.put(string.getBytes("UTF-8")
                    , offset
                    ,string.length());
        } catch (UnsupportedEncodingException ex) {
            
        }
    }
    
    /**
     *
     * @param len
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getString(int len) throws UnsupportedEncodingException {
        if (len <= 0) {
            throw new IllegalArgumentException();
        }
        byte[] str = new byte[len]; 
        buffer.get(str);
        return new String(str,"utf-8");
    }
}
