package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.image.compression.ImageCompressionRLE;
import propra.imageconverter.image.compression.ImageCompressionRaw;

/**
 *  Schreibt und liest TGA Header
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
    
    // Kodierungen
    static final int TGA_HEADER_ENCODING_NONE = 2;     
    static final int TGA_HEADER_ENCODING_RLE = 10;    
    
    /**
     *
     * 
     * @param file
     * @param file
     * @param write
     * @throws java.io.IOException
     * @throws IOException
     */
    public ImageResourceTGA(String file, boolean write) throws IOException {
        super(file, write);
        fileHeaderSize = TGA_HEADER_SIZE;
    }
    
    /**
     * 
     * 
     * @return
     * @return 
     * @throws java.io.IOException 
     * @throws IOException
     */
    @Override
    public ImageAttributes readHeader() throws IOException {
        
        // DataBuffer für Header erstellen       
        ByteBuffer bytes = ByteBuffer.allocate(fileHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
       
        // Headerbytes von Stream einlesen
        binaryFile.read(bytes.array());
        
        // Headerfelder konvertieren
        header.setWidth(bytes.getShort(TGA_HEADER_OFFSET_WIDTH));
        header.setHeight(bytes.getShort(TGA_HEADER_OFFSET_HEIGHT));
        header.setFormat(Color.Format.COLOR_BGR);
        int bpp = bytes.get(TGA_HEADER_OFFSET_BPP); 
        
        // Kompression
        byte compression = bytes.get(TGA_HEADER_OFFSET_ENCODING);
        switch (compression) {
            case TGA_HEADER_ENCODING_RLE -> {
                inCodec = new ImageCompressionRLE(this);
                header.setCompression(Compression.RLE);
            }
            case TGA_HEADER_ENCODING_NONE -> {
                inCodec = new ImageCompressionRaw(this);
                header.setCompression(Compression.NONE);
            }
            default -> throw new UnsupportedOperationException("Nicht unterstützte TGA Kompression!");
        }
        
        // Prüfe tga Spezifikationen
        if(header.getWidth() <= 0 || header.getHeight() <= 0 || bpp != 24
        || !DataUtil.checkBit(bytes.get(TGA_HEADER_OFFSET_ORIGIN), (byte)6)
        || DataUtil.checkBit(bytes.get(TGA_HEADER_OFFSET_ORIGIN), (byte)5)
        || bytes.get(0) != 0) {
            throw new UnsupportedOperationException("Ungültiges TGA Dateiformat!");
        }
        
        return header;
    }  
    
    /**
     * Schreibt allgemeinen Header als TGA Header
     */
    @Override
    public void writeHeader(ImageAttributes srcHeader) throws IOException {

        super.writeHeader(srcHeader);
                
        // DataBuffer für Header erstellen
        ByteBuffer bytes = ByteBuffer.allocate(fileHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
                
        // Headerfelder in Buffer schreiben
        bytes.put(TGA_HEADER_OFFSET_ENCODING, (byte)2);
        bytes.putShort(TGA_HEADER_OFFSET_WIDTH, (short)srcHeader.getWidth());
        bytes.putShort(TGA_HEADER_OFFSET_HEIGHT, (short)srcHeader.getHeight());
        bytes.putShort(TGA_HEADER_OFFSET_Y0, (short)srcHeader.getHeight());
        bytes.put(TGA_HEADER_OFFSET_BPP, (byte)(24));        
        bytes.put(TGA_HEADER_OFFSET_ORIGIN, (byte)(1 << 5)); 
        
        // Kompression
        switch(header.getCompression()) {
            case RLE -> {
                bytes.put(TGA_HEADER_OFFSET_ENCODING, 
                            (byte)TGA_HEADER_ENCODING_RLE);
            }
            case NONE -> {
                bytes.put(TGA_HEADER_OFFSET_ENCODING, 
                            (byte)TGA_HEADER_ENCODING_NONE);
            }
            default -> {
                throw new IllegalArgumentException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        binaryFile.seek(0);
        binaryFile.write(bytes.array());
    }
}
