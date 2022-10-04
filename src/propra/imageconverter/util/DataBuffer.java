package propra.imageconverter.util;

import java.io.UnsupportedEncodingException;
import propra.imageconverter.image.Color;

/**
 *
 * @author pg
 */
public class DataBuffer extends Object {
    protected byte[] data;
    int position;

    public DataBuffer() {
    }

    
    public DataBuffer(byte[] data) {
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
     * @param d
     */
    public void put(byte d) {
        data[position] = d;
        movePosition(1);  
    }
    
    public void put(byte d, int offset) {
        data[offset] = d; 
    }
    
    public void put(short d) {
        data[position] = (byte)(d >> 8);
        data[position + 1] = (byte) (d & 0x00FF);
        movePosition(2);  
    }
    
    public void put(short d, int offset) {
        data[offset] = (byte)(d >> 8);
        data[offset + 1] = (byte) (d & 0x00FF); 
    }
    
    public void put(Color c) {
        Color.buildRGB(data, position, c);
        movePosition(3);  
    }
    
    public void putLittle(short d) {
        data[position + 1] = (byte)(d >> 8);
        data[position] = (byte) (d & 0x00FF);
        movePosition(2);  
    }
    
    public void putLittle(short d, int offset) {
        data[offset + 1] = (byte)(d >> 8);
        data[offset] = (byte) (d & 0x00FF); 
    }
    
    public void putLittle(Color c) {
        Color.buildBGR(data, position, c);
        movePosition(3);  
    }
    
    public byte get(int offset) {
        return data[offset];
    }
    
    public String getString(int len) throws UnsupportedEncodingException {
        byte[] str = copy(len);
        return new String(str,"utf-8");
    }
    
    public short getShort(int offset) {
        return 0;/*return bytesToShort(data[offset],
                            data[offset + 1]);*/
    }
    
    public short getShortLittle(int offset) {
        return bytesToShortLittle(data[offset + 1],
                            data[offset]);
    }
    
    /**
     *
     * @param offset
     * @return
     */
    public int getIntLittle(int offset) {
        return bytesToIntLittle(data, offset);
    }
    
    /**
     *
     * @param offset
     * @return
     */
    public long getLongLittle(int offset) {
        return bytesToLongLittle(data, offset);
    }
        
    public Color getColorRGB() {
        return new Color(data[position++], 
                        data[position++], 
                        data[position++]);  
    }
    
    public Color getColorBGR() {
        Color c = new Color(data[position+2], 
                            data[position+1], 
                            data[position]);  
        position += 3;
        return c;
    }
    
    public void movePosition(int offset) {
        position += offset;
    }
    
    public void setPosition(int pos) {
        position = pos;
    }
    
    public int getPosition() {
        return position;
    }
    
    public boolean isValid() {
        return (data != null && 
                position >= 0);
    }
    
    public byte[] getBuffer() {
        return data;
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
    
    public static short bytesToShortLittle(byte b1, byte b2) {
        return (short)( (b1 << 8) | 
                        (b2 & 0x00FF));
    }
    
    public static int bytesToInt(byte[] buffer, int offset) {
        return (((buffer[offset]     & 0xFF) << 24)  | 
                ((buffer[offset + 1] & 0xFF) << 16)  |
                ((buffer[offset + 2] & 0xFF) << 8)   |
                  buffer[offset + 3] & 0xFF);
    }
    
    public static int bytesToIntLittle(byte[] buffer, int offset) {
        return (((buffer[offset + 3] & 0xFF) << 24)  | 
                ((buffer[offset + 2] & 0xFF) << 16)  |
                ((buffer[offset + 1] & 0xFF) << 8)   |
                  buffer[offset]     & 0xFF);
    }
    
    public static long bytesToLong(byte[] buffer, int offset) {
        return  (((long)(bytesToInt(buffer, offset) & 0xFFFFFFFF)) << 4) |
                 (long)bytesToInt(buffer, offset + 4) & 0xFFFFFFFF;
    }
    
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
