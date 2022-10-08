package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author pg
 */
public class ImageBuffer extends DataBuffer {
    /**
     * 
     */
    protected ImageHeader info;  
  
    /**
     * 
     */
    ImageBuffer() {
        
    }
    
    /**
     * 
     * @param info 
     */
    ImageBuffer(ImageHeader info) {
        create(info);
    }
    
    /**
     * 
     * @param info 
     */
    ImageBuffer(byte[] data, ImageHeader info) {
        wrap(data, info, ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * 
     * @param info
     */
    public void create(ImageHeader info) {
        if (!info.isValid()) {
            throw new IllegalArgumentException();
        }        
        this.info = new ImageHeader(info);
        create(info.getTotalSize());
    }
    
    /**
     * 
     * @param data
     * @param info 
     */
    public void wrap(byte[] data, ImageHeader info, ByteOrder byteOrder) {
        buffer = ByteBuffer.wrap(data);
        buffer.order(byteOrder);
        this.info = new ImageHeader(info);
    }
    
    /**
     *
     * @param header
     * @param byteOrder
     * @return
     */
    public ImageBuffer convertTo(ImageHeader header, ByteOrder byteOrder) {
        ImageBuffer image = new ImageBuffer(header);
        ColorType colorType = header.getColorType();
        
        if(info.getColorType().compareTo(colorType) != 0 
                        || byteOrder != buffer.order()) {
            byte[] color = new byte[3];
            
            for(int i=0;i<info.getElementCount();i++) {
                getColor(color);
                colorType.convertColor(color, info.getColorType());
                image.putColor(color);
            }
            
            image.getBuffer().rewind();
        }
        else {
            image.wrap(buffer.array());
        }
        return image;
    }
    
    /**
     * 
     * @return 
     */
    public ImageHeader getHeader() {
        return info;
    }
    
    /**
     * Schreibt 3 Byte Farbwert an aktuelle Position
     * @param color
     * @param type
     * @return
     */
    public byte[] putColor(byte[] color, ColorType type) {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        if(buffer.order() == ByteOrder.LITTLE_ENDIAN) {
            ColorType.switchEndian(color);
        }
        info.getColorType().convertColor(color, type);
        buffer.put(color);
        return color;
    }
}
