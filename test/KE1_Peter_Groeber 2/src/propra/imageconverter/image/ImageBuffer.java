package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author pg
 */
public class ImageBuffer extends DataBuffer {
    protected ImageHeader header;  
  
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
        this.header = new ImageHeader(info);
        create(info.getTotalSize());
    }
    
    /**
     * 
     * @param data
     * @param info 
     * @param byteOrder 
     */
    public void wrap(byte[] data, ImageHeader header, ByteOrder byteOrder) {
        buffer = ByteBuffer.wrap(data);
        buffer.order(byteOrder);
        this.header = new ImageHeader(header);
    }
    
    /**
     *
     * @param format
     * @return
     */
    public ImageBuffer convertTo(ImageHeader format) {
        if(!format.isValid()
        || !isValid()) {
            throw new IllegalArgumentException();
        }
        
        ImageBuffer image = new ImageBuffer(format);
        ColorType newColorType = format.getColorType();
        ColorType oldColorType = header.getColorType();
        
        // Konvertierung der Farben nötig?
        if(oldColorType.compareTo(newColorType) != 0) {
            byte[] color = new byte[3];
            
            for(int i=0;i<header.getElementCount();i++) {
                // Farbtripel von Quell- zu Zielformat konvertieren
                getColor(color);
                newColorType.convertColor(color, oldColorType);
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
        return header;
    }
    
    /**
     * Gibt 3 Byte Farbwert an aktueller Position zurück
     * @param color
     * @return
     */
    public byte[] getColor(byte[] color) {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        buffer.get(color);
        return color;
    }
    
    /**
     * Schreibt 3 Byte Farbwert an aktuelle Position
     * @param color
     * @return
     */
    public byte[] putColor(byte[] color) {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        buffer.put(color);
        return color;
    }
    
    /**
     * Schreibt 3 Byte Farbwert an aktuelle Position und konvertiert zum Zielformat
     * @param color
     * @param type
     * @return
     */
    public byte[] putColor(byte[] color, ColorType type) {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        header.getColorType().convertColor(color, type);
        buffer.put(color);
        return color;
    }
}
