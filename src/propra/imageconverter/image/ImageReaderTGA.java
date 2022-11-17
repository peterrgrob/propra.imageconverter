package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.Utility;

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
     * @param stream
     */
    public ImageReaderTGA(String file, DataFormat.Mode mode) throws IOException {
        super(file, mode);
        formatHeaderSize = TGA_HEADER_SIZE;   
    }

    /**
     * 
     * 
     * @throws java.io.IOException
     */
    public ImageHeader readHeader() throws IOException {
        
        // Buffer für Header erstellen
        DataBuffer data = new DataBuffer(formatHeaderSize);
        ByteBuffer byteBuffer = data.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
       
        // Headerbytes von Stream einlesen
        read(data, 0, formatHeaderSize);
        
        // Headerfelder konvertieren
        ImageHeader newHeader = new ImageHeader();
        newHeader.width(byteBuffer.getShort(TGA_HEADER_OFFSET_WIDTH));
        newHeader.height(byteBuffer.getShort(TGA_HEADER_OFFSET_HEIGHT));
        newHeader.pixelSize(byteBuffer.get(TGA_HEADER_OFFSET_BPP) >> 3); 
        
        // Kompression prüfen
        byte compression = byteBuffer.get(TGA_HEADER_OFFSET_ENCODING);
        switch (compression) {
            case TGA_HEADER_ENCODING_RLE:
                newHeader.colorFormat().setEncoding(DataFormat.Encoding.RLE);
                break;
            case TGA_HEADER_ENCODING_NONE:
                newHeader.colorFormat().setEncoding(DataFormat.Encoding.NONE);            
                break;
            default:
                throw new UnsupportedOperationException("Nicht unterstützte TGA Kompression!");
        }
        
        // Prüfe tga Spezifikationen
        if(newHeader.isValid() == false
        || !Utility.checkBit(byteBuffer.get(TGA_HEADER_OFFSET_ORIGIN), (byte)6)
        || Utility.checkBit(byteBuffer.get(TGA_HEADER_OFFSET_ORIGIN), (byte)5)
        || byteBuffer.get(0) != 0) {
            throw new UnsupportedOperationException("Ungültiges TGA Dateiformat!");
        }
        
        header = newHeader;
        return header;
    }  
}
