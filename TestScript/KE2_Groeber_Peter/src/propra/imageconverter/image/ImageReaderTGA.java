package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.data.DataFormat;


/**
 *
 * @author pg
 */
public class ImageReaderTGA extends ImageReader {

    // Datei-Offsets der einzelnen Header-Felder
    static final int TGA_HEADER_SIZE = 18;
    static final int TGA_HEADER_OFFSET_ENCODING = 2;
    static final int TGA_HEADER_OFFSET_X0 = 8;
    static final int TGA_HEADER_OFFSET_Y0 = 10;
    static final int TGA_HEADER_OFFSET_WIDTH = 12;
    static final int TGA_HEADER_OFFSET_HEIGHT = 14;
    static final int TGA_HEADER_OFFSET_BPP = 16;
    static final int TGA_HEADER_OFFSET_ORIGIN = 17; 
    
    static final int TGA_HEADER_ENCODING_NONE = 2;     
    static final int TGA_HEADER_ENCODING_RLE = 10;     

    /**
     *
     * @param file
     * @param mode
     * @throws java.io.IOException
     */
    public ImageReaderTGA(String file, DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        formatHeaderSize = TGA_HEADER_SIZE;   
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
        ByteBuffer bytes = ByteBuffer.allocate(formatHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
       
        // Headerbytes von Stream einlesen
        read(bytes, 0, formatHeaderSize);
        
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
}
