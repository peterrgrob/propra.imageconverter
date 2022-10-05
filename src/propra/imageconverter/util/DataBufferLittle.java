package propra.imageconverter.util;

import propra.imageconverter.image.Color;

/**
 *
 * @author pg
 */
public class DataBufferLittle extends DataBuffer {

    /**
     *
     */
    public DataBufferLittle() {
        byteOrder = Order.LITTLE_ENDIAN;
    }
    
    /**
     *
     * @param data
     */
    public DataBufferLittle(byte[] data) {
        super();
        this.data = data;
    }
    
    /**
     *
     * @param d
     */
    @Override
    public void put(short d) {
        data[position + 1] = (byte)(d >> 8);
        data[position] = (byte) (d & 0x00FF);
        movePosition(2);  
    }
    
    /**
     *
     * @param d
     * @param offset
     */
    @Override
    public void put(short d, int offset) {
        data[offset + 1] = (byte)(d >> 8);
        data[offset] = (byte) (d & 0x00FF); 
    }
    
    /**
     *
     * @param d
     * @param offset
     */
    public void put(int d, int offset) {
        data[offset]      = (byte) (d >> 24);
        data[offset + 1]  = (byte) (d >> 16);
        data[offset + 2]  = (byte) (d >> 8);
        data[offset + 3]  = (byte) (d);
    }
    
    /**
     *
     * @param d
     * @param offset
     */
    public void put(long d, int offset) {
        data[offset]      = (byte) (d >> 56);
        data[offset + 1]  = (byte) (d >> 48);
        data[offset + 2]  = (byte) (d >> 40);
        data[offset + 3]  = (byte) (d >> 32);
        data[offset + 4]  = (byte) (d >> 24);
        data[offset + 5]  = (byte) (d >> 16);
        data[offset + 6]  = (byte) (d >> 8);
        data[offset + 7]  = (byte) (d);
    }
    
    /**
     *
     * @param c
     */
    @Override
    public void put(Color c) {
        Color.buildBGR(data, position, c);
        movePosition(3);  
    }
    
    /**
     *
     * @param offset
     * @return
     */
    @Override
    public short getShort(int offset) {
        return bytesToShortLittle(data[offset + 1],
                                    data[offset]);
    }
    
    /**
     *
     * @param offset
     * @return
     */
    public int getInt(int offset) {
        return bytesToIntLittle(data, offset);
    }
    
    /**
     *
     * @param offset
     * @return
     */
    public long getLong(int offset) {
        return bytesToLongLittle(data, offset);
    }
    
    /**
     *
     * @return
     */
    public Color getColor() {
        Color c = new Color(data[position+2], 
                            data[position+1], 
                            data[position]);  
        position += 3;
        return c;
    }
    
    /**
     *
     * @param colorOrder
     * @return
     */
    @Override
    public Color getColor(Color.ColorOrder colorOrder) {
        byte[] tmp = new byte[3];
        tmp[0] = data[position + 2];
        tmp[1] = data[position + 1];        
        tmp[2] = data[position + 0];
   
        Color c = new Color(tmp[colorOrder.redShift], 
                            tmp[colorOrder.greenShift], 
                            tmp[colorOrder.blueShift]); 
        position += 3;
        return c;
    }
}
