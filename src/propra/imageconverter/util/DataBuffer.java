package propra.imageconverter.util;

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
    
    public short getShort(int offset) {
        return bytesToShort(data[offset],data[offset + 1]);
    }
    
    public short getShortLittle(int offset) {
        return bytesToShort(data[offset + 1],data[offset]);
    }
    
    public Color getColorRGB() {
        return new Color(data[position++], data[position++], data[position++]);  
    }
    
    public Color getColorBGR() {
        Color c = new Color(data[position+2], data[position+1], data[position]);  
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
        return (data != null && position >= 0);
    }
    
    public byte[] getBuffer() {
        return data;
    }
    
    public static short bytesToShort(byte b1, byte b2) {
        return (short)((b1 << 8) | (b2 & 0x00FF));
    }
}
