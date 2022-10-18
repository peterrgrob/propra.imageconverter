package propra.imageconverter.image;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.Utility;

/**
 * TGA spezifische Implementierung
 * 
 * @author pg
 */
public class ImageModuleTGA extends ImageModule {

    // Datei-Offsets der einzelnen Header-Felder
    static final int TGA_HEADER_SIZE = 18;
    static final int TGA_HEADER_OFFSET_ENCODING = 2;
    static final int TGA_HEADER_OFFSET_X0 = 7;
    static final int TGA_HEADER_OFFSET_Y0 = 9;
    static final int TGA_HEADER_OFFSET_WIDTH = 12;
    static final int TGA_HEADER_OFFSET_HEIGHT = 14;
    static final int TGA_HEADER_OFFSET_BPP = 16;
    static final int TGA_HEADER_OFFSET_ORIGIN = 17; 

    /**
     *
     */
    public ImageModuleTGA(long streamLen) {
        super(streamLen);
        headerSize = TGA_HEADER_SIZE;
        byteOrder = ByteOrder.LITTLE_ENDIAN;    
    }

    
    /**
     * Wandelt einen allgemeinen Header in einen TGA Header um
     * 
     * @param info
     * @return
     */
    @Override
    public DataBuffer headerOut(ImageHeader info) {
        if(info.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen
        DataBuffer dataBuffer = new DataBuffer(ImageModuleTGA.TGA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        header = new ImageHeader(info);
        header.getColorType().setMapping(ColorFormat.RED,2);
        header.getColorType().setMapping(ColorFormat.GREEN,1);
        header.getColorType().setMapping(ColorFormat.BLUE,0);
        
        // Headerfelder in Buffer schreiben
        byteBuffer.put(TGA_HEADER_OFFSET_ENCODING, (byte)2);
        byteBuffer.putShort(TGA_HEADER_OFFSET_WIDTH, (short)info.getWidth());
        byteBuffer.putShort(TGA_HEADER_OFFSET_HEIGHT, (short)info.getHeight());
        byteBuffer.put(TGA_HEADER_OFFSET_BPP, (byte)(info.getElementSize() << 3));        
        byteBuffer.put(TGA_HEADER_OFFSET_ORIGIN, (byte)(1 << 5));    
        
        return dataBuffer;
    }

    /**
     * Wandelt einen TGA Header in einen allgemeinen Header um 
     * 
     * @param data
     * @return
     */
    @Override
    public ImageHeader headerIn(DataBuffer data) {
        ByteBuffer byteBuffer = data.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Headerfelder einlesen
        ImageHeader tInfo = new ImageHeader();
        tInfo.setWidth(byteBuffer.getShort(TGA_HEADER_OFFSET_WIDTH));
        tInfo.setHeight(byteBuffer.getShort(TGA_HEADER_OFFSET_HEIGHT));
        tInfo.setElementSize(byteBuffer.get(TGA_HEADER_OFFSET_BPP) >> 3); 
        tInfo.setEncoding(ImageHeader.Encoding.UNCOMPRESSED);
        
        // Prüfe tga Spezifikationen
        if(tInfo.isValid() == false
        || !Utility.checkBit(byteBuffer.get(TGA_HEADER_OFFSET_ORIGIN), (byte)6)
        || Utility.checkBit(byteBuffer.get(TGA_HEADER_OFFSET_ORIGIN), (byte)5)
        || byteBuffer.get(0) != 0
        || byteBuffer.get(TGA_HEADER_OFFSET_ENCODING) != 2) {
            throw new UnsupportedOperationException("Ungültiges TGA Dateiformat!");
        }
        
        return (header = tInfo);
    }
}
