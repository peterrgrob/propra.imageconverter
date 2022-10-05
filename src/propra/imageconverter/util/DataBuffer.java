package propra.imageconverter.util;

import java.io.UnsupportedEncodingException;
import propra.imageconverter.image.Color;

/**
 *
 * @author pg
 */
public class DataBuffer extends Object {
    protected byte[] data;
    protected int position;
    protected Order byteOrder = Order.BIG_ENDIAN;
    
    /**
     *
     */
    public enum Order {
        BIG_ENDIAN,
        LITTLE_ENDIAN;
    }

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
        this.data = data;
    }
    
    /**
     *
     * @param size
     */
    public void create(int size) {
        if(size <= 0) {
            throw new IllegalArgumentException();
        }
        data = new byte[size];
    }
    
    /**
     *
     * @param byteOrder
     */
    public void setOrder(Order byteOrder) {
        this.byteOrder = byteOrder;
    }
    
    /**
     *
     * @return 
     */
    public Order getOrder() {
        return byteOrder;
    }
    
    /**
     *
     * @param data
     */
    public void wrap(byte[] data) {
        this.data = data;
    }
    
    /**
     *
     * @param offset
     */
    public void movePosition(int offset) {
        position += offset;
    }
    
    /**
     *
     * @param pos
     */
    public void setPosition(int pos) {
        position = pos;
    }
    
    /**
     *
     * @return
     */
    public int getPosition() {
        return position;
    }
    
    /**
     *
     * @return
     */
    public boolean isValid() {
        return (data != null && 
                position >= 0);
    }
    
    /**
     *
     * @return
     */
    public byte[] getBuffer() {
        return data;
    }
    
    /**
     *
     * @param d
     */
    public void put(byte d) {
        data[position] = d;
        movePosition(1);  
    }
    
    /**
     *
     * @param d
     * @param offset
     */
    public void put(byte d, int offset) {
        data[offset] = d; 
    }
    
    /**
     *
     * @param d
     */
    public void put(short d) {
        data[position] = (byte)(d >> 8);
        data[position + 1] = (byte) (d & 0x00FF);
        movePosition(2);  
    }
    
    /**
     *
     * @param d
     * @param offset
     */
    public void put(short d, int offset) {
        data[offset] = (byte)(d >> 8);
        data[offset + 1] = (byte) (d & 0x00FF); 
    }
    
    /**
     *
     * @param d
     * @param offset
     */
    public void put(long d, int offset) {
        data[offset + 7]      = (byte) (d >> 56);
        data[offset + 6]  = (byte) (d >> 48);
        data[offset + 5]  = (byte) (d >> 40);
        data[offset + 4]  = (byte) (d >> 32);
        data[offset + 3]  = (byte) (d >> 24);
        data[offset + 2]  = (byte) (d >> 16);
        data[offset + 1]  = (byte) (d >> 8);
        data[offset    ]  = (byte) (d);
    }
    
    /**
     *
     * @param src
     * @param len
     * @param offset
     */
    public void put(byte[] src, int len, int offset) {
        System.arraycopy(src,
        offset,
        data,
        offset,
        len);
    }
    
    /**
     *
     * @param c
     */
    public void put(Color c) {
        Color.buildRGB(data, position, c);
        movePosition(3);  
    }
    
    /**
     *
     * @param string
     * @param offset
     * @throws UnsupportedEncodingException
     */
    public void put(String string, int offset) throws UnsupportedEncodingException {
        put(string.getBytes("UTF-8"),
            string.length(), 
            offset);
    }
    
    /**
     *
     * @param offset
     * @return
     */
    public byte get(int offset) {
        return data[offset];
    }
    
    /**
     *
     * @param len
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getString(int len) throws UnsupportedEncodingException {
        byte[] str = copy(len);
        return new String(str,"utf-8");
    }
    
    /**
     *
     * @param offset
     * @return
     */
    public short getShort(int offset) {
        return 0;/*return bytesToShort(data[offset],
                            data[offset + 1]);*/
    }
        
    /**
     *
     * @return
     */
    public Color getColor(Color.ColorOrder colorOrder) {
        Color c = new Color(data[position + colorOrder.redShift], 
                            data[position + colorOrder.blueShift], 
                            data[position + colorOrder.greenShift]); 
        position += 3;
        return c;
    }
 
    /**
     *
     * @param len
     * @return
     */
    public byte[] copy(int len) {
        byte[] cp = new byte[len];
        System.arraycopy(data,
                        position,
                        cp,
                        0,
                        len);
        return cp;
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
