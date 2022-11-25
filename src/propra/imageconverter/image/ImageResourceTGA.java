package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.data.DataFormat;

/**
 *  Schreibt TGA Header
 * 
 * @author pg
 */
public class ImageResourceTGA extends ImageResource {  

    // Datei-Offsets der einzelnen Header-Felder
    static final int TGA_HEADER_SIZE = 18;
    static final int TGA_HEADER_OFFSET_ENCODING = 2;
    static final int TGA_HEADER_OFFSET_X0 = 8;
    static final int TGA_HEADER_OFFSET_Y0 = 10;
    static final int TGA_HEADER_OFFSET_WIDTH = 12;
    static final int TGA_HEADER_OFFSET_HEIGHT = 14;
    static final int TGA_HEADER_OFFSET_BPP = 16;
    static final int TGA_HEADER_OFFSET_ORIGIN = 17; 
    
    // Kodierungungen
    static final int TGA_HEADER_ENCODING_NONE = 2;     
    static final int TGA_HEADER_ENCODING_RLE = 10;    
    
    /**
     *
     * @param file
     * @param mode
     * @throws java.io.IOException
     */
    public ImageResourceTGA(String file, DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        fileHeaderSize = TGA_HEADER_SIZE;
    }
    
    /**
     * 
     * 
     * @return 
     * @throws java.io.IOException
     */
    @Override
    public ImageHeader readHeader() throws IOException {
        
        // DataBuffer für Header erstellen       
        ByteBuffer bytes = ByteBuffer.allocate(fileHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
       
        // Headerbytes von Stream einlesen
        read(bytes);
        
        // Headerfelder konvertieren
        ImageHeader newHeader = new ImageHeader();
        newHeader.width(bytes.getShort(TGA_HEADER_OFFSET_WIDTH));
        newHeader.height(bytes.getShort(TGA_HEADER_OFFSET_HEIGHT));
        newHeader.pixelSize(bytes.get(TGA_HEADER_OFFSET_BPP) >> 3); 
        
        // Kompression prüfen
        byte compression = bytes.get(TGA_HEADER_OFFSET_ENCODING);
        switch (compression) {
            case TGA_HEADER_ENCODING_RLE -> newHeader.colorFormat().encoding(DataFormat.Encoding.RLE);
            case TGA_HEADER_ENCODING_NONE -> newHeader.colorFormat().encoding(DataFormat.Encoding.NONE);
            default -> throw new UnsupportedOperationException("Nicht unterstützte TGA Kompression!");
        }
        
        // Prüfe tga Spezifikationen
        if(newHeader.isValid() == false
        || !DataFormat.checkBit(bytes.get(TGA_HEADER_OFFSET_ORIGIN), (byte)6)
        || DataFormat.checkBit(bytes.get(TGA_HEADER_OFFSET_ORIGIN), (byte)5)
        || bytes.get(0) != 0) {
            throw new UnsupportedOperationException("Ungültiges TGA Dateiformat!");
        }
        
        header = newHeader;
        return header;
    }  
    
    /**
     * Schreibt allgemeinen Header als TGA Header
     * 
     * @param srcHeader
     * @throws java.io.IOException
     */
    @Override
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        super.writeHeader(srcHeader);
        ColorFormat writeColorFormat = new ColorFormat(2, 1, 0);
        header.colorFormat().setMapping(writeColorFormat.getMapping());
                
        // DataBuffer für Header erstellen
        ByteBuffer byteBuffer = ByteBuffer.allocate(fileHeaderSize);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                
        // Headerfelder in Buffer schreiben
        byteBuffer.put(TGA_HEADER_OFFSET_ENCODING, 
                        (byte)2);
        byteBuffer.putShort(TGA_HEADER_OFFSET_WIDTH, 
                            (short)srcHeader.width());
        byteBuffer.putShort(TGA_HEADER_OFFSET_HEIGHT, 
                            (short)srcHeader.height());
        byteBuffer.putShort(TGA_HEADER_OFFSET_Y0, 
                            (short)srcHeader.height());
        byteBuffer.put(TGA_HEADER_OFFSET_BPP, 
                            (byte)(srcHeader.pixelSize() << 3));        
        byteBuffer.put(TGA_HEADER_OFFSET_ORIGIN, 
                            (byte)(1 << 5)); 
        
        // Kompression
        switch(header.colorFormat().encoding()) {
            case RLE -> {
                byteBuffer.put(TGA_HEADER_OFFSET_ENCODING, 
                                    (byte)TGA_HEADER_ENCODING_RLE);
            }
            case NONE -> {
                byteBuffer.put(TGA_HEADER_OFFSET_ENCODING, 
                                    (byte)TGA_HEADER_ENCODING_NONE);
            }
            default -> {
                throw new IllegalArgumentException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        binaryFile.seek(0);
        write(byteBuffer);
    }
    
}
