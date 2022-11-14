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
public class DataBuffer implements Validatable {

    protected ByteBuffer buffer;
    
    // Menge an Daten die im Buffer vorhanden sind und zugehöriger 
    // Offset für die blockweise Verarbeitung von Daten
    protected int currDataLength;
    protected int currDataOffset;


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
    @Override
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
    public int getSize() {
        if (!isValid()) {
            return 0; 
        }
        return buffer.capacity();
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
       
    public int getCurrDataLength() {
        return currDataLength;
    }

    public void setCurrDataLength(int currDataLength) {
        this.currDataLength = currDataLength;
    }
    
    public int getCurrDataOffset() {
        return currDataOffset;
    }

    public void setCurrDataOffset(int currDataOffset) {
        this.currDataOffset = currDataOffset;
    }
    
    public void skipBytes(int len) {
        buffer.position(buffer.position() + len);
    }
}
