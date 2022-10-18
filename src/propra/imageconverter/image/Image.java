package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * DataBuffer Implementierung mit bildspezifischen Zusatzmethoden.
 * 
 * @author pg
 */
public class Image extends DataBuffer {
    
    protected ImageHeader header;  
  
    /**
     * 
     */
    Image() {
        
    }
    
    /**
     * 
     * @param info 
     */
    Image(ImageHeader info) {
        create(info);
    }
    
    /**
     * 
     * @param info 
     */
    Image(byte[] data, ImageHeader info) {
        wrap(data, info, ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Initialisiert den Buffer anhand eines Headers. 
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
     * Initialisiert den Buffer anhand eines byte Arrays, Headers und 
     * ByteOrder.
     * 
     * @param data
     * @param header
     * @param info 
     * @param byteOrder 
     */
    public void wrap(byte[] data, ImageHeader header, ByteOrder byteOrder) {
        buffer = ByteBuffer.wrap(data);
        buffer.order(byteOrder);
        this.header = new ImageHeader(header);
    }
    
    /**
     * Konvertiert den Image in einen neuen Image mit gegebenen Format.
     *  
     * @param target
     * @param format
     * @return
     */
    public Image convertTo(Image target, ColorFormat format) {
        if( !target.isValid()
        ||  !isValid()
        ||  format == null) {
            throw new IllegalArgumentException();
        }
        
        ColorFormat srcFormat = getHeader().getColorType();

        // Konvertierung der Farben nötig?
        if(srcFormat.compareTo(format) != 0) {
            byte[] color = new byte[3];
            
            for(int i=0; i<getHeader().getElementCount(); i++) {
                // Farbtripel von Quell- zu Zielformat konvertieren
                getColor(color);
                format.convertColor(color, srcFormat);
                target.putColor(color);
            }
            
            target.getBuffer().rewind();
        }
        else {
            // Keine Konvertierung nötig, daher Buffer direkt übernehmen
            target.wrap(buffer.array());
        }
        return target;
    }
    
    /**
     * 
     * @return 
     */
    public ImageHeader getHeader() {
        return header;
    }
    
    /**
     * Gibt 3 Byte Farbwert an aktueller Position zurück.
     * 
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
     * Schreibt 3 Byte Farbwert an aktuelle Position.
     * 
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
     * Schreibt 3 Byte Farbwert an aktuelle Position und konvertiert zum Zielformat.
     * 
     * @param color
     * @param type
     * @return
     */
    public byte[] putColor(byte[] color, ColorFormat type) {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        header.getColorType().convertColor(color, type);
        buffer.put(color);
        return color;
    }
}
