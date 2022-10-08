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
        wrap(data, info);
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
    public void wrap(byte[] data, ImageHeader info) {
        buffer = ByteBuffer.wrap(data);
        this.info = new ImageHeader(info);
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
