package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.data.IDataTranscoder.Compression;

/**
 * Tga spezifische Implementierung einer ImageRessource
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
     * Konstruktor
     */
    public ImageResourceTGA(String file, boolean write) throws IOException {
        super(file, write);
        fileHeaderSize = TGA_HEADER_SIZE;
        header.setFormat(Color.Format.COLOR_BGR);
    }
    
    /** 
     * Liest Tga Header aus dem Stream ein, wandelt in allgemeine Bildattribute um
     * und prüft auf korrekte Werte
     */
    @Override
    public ImageAttributes readHeader() throws IOException {
        
        // DataBuffer für Header erstellen       
        ByteBuffer bytes = ByteBuffer.allocate(fileHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
       
        // Headerbytes von Stream einlesen
        ImageAttributes attributes = new ImageAttributes();
        binaryFile.read(bytes.array());
        
        attributes.setWidth(bytes.getShort(TGA_HEADER_OFFSET_WIDTH));
        attributes.setHeight(bytes.getShort(TGA_HEADER_OFFSET_HEIGHT));
        attributes.setFormat(Color.Format.COLOR_BGR);
        int bpp = bytes.get(TGA_HEADER_OFFSET_BPP); 
        
        // Kompression
        switch (bytes.get(TGA_HEADER_OFFSET_ENCODING)) {
            case TGA_HEADER_ENCODING_RLE -> {
                attributes.setCompression(Compression.RLE);
            }
            case TGA_HEADER_ENCODING_NONE -> {
                attributes.setCompression(Compression.UNCOMPRESSED);
            }
            default -> throw new UnsupportedOperationException("Nicht unterstützte TGA Kompression!");
        }
        
        // Prüfe tga Spezifikationen
        if(attributes.getWidth() <= 0 || attributes.getHeight() <= 0 || bpp != 24
        || !DataUtil.checkBit(bytes.get(TGA_HEADER_OFFSET_ORIGIN), (byte)6)
        || DataUtil.checkBit(bytes.get(TGA_HEADER_OFFSET_ORIGIN), (byte)5)
        || bytes.get(0) != 0) {
            throw new UnsupportedOperationException("Ungültiges TGA Dateiformat!");
        }
        
        // Header setzen
        setHeader(attributes);
        return header;
    }  
    
    /**
     * Schreibt allgemeine Bildattribute als TGA Header
     */
    @Override
    public void writeHeader() throws IOException {
        
        // DataBuffer für Header erstellen
        ByteBuffer bytes = ByteBuffer.allocate(fileHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
                
        // Headerfelder in Buffer schreiben
        bytes.put(TGA_HEADER_OFFSET_ENCODING, (byte)2);
        bytes.putShort(TGA_HEADER_OFFSET_WIDTH, (short)header.getWidth());
        bytes.putShort(TGA_HEADER_OFFSET_HEIGHT, (short)header.getHeight());
        bytes.putShort(TGA_HEADER_OFFSET_Y0, (short)header.getHeight());
        bytes.put(TGA_HEADER_OFFSET_BPP, (byte)(24));        
        bytes.put(TGA_HEADER_OFFSET_ORIGIN, (byte)(1 << 5)); 
        
        // Kompression
        switch(header.getCompression()) {
            case RLE -> {
                bytes.put(TGA_HEADER_OFFSET_ENCODING, 
                            (byte)TGA_HEADER_ENCODING_RLE);
            }
            case UNCOMPRESSED -> {
                bytes.put(TGA_HEADER_OFFSET_ENCODING, 
                            (byte)TGA_HEADER_ENCODING_NONE);
            }
            default -> {
                throw new UnsupportedOperationException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        binaryFile.seek(0);
        binaryFile.write(bytes.array());
    }
}
