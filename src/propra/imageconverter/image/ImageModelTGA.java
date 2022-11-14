package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataFormat.Encoding;
import propra.imageconverter.util.Utility;

/**
 * TGA spezifische Implementierung
 * 
 * @author pg
 */
public class ImageModelTGA extends ImageModel {

    // Datei-Offsets der einzelnen Header-Felder
    static private final int TGA_HEADER_SIZE = 18;
    static private final int TGA_HEADER_OFFSET_ENCODING = 2;
    static private final int TGA_HEADER_OFFSET_X0 = 7;
    static private final int TGA_HEADER_OFFSET_Y0 = 9;
    static private final int TGA_HEADER_OFFSET_WIDTH = 12;
    static private final int TGA_HEADER_OFFSET_HEIGHT = 14;
    static private final int TGA_HEADER_OFFSET_BPP = 16;
    static private final int TGA_HEADER_OFFSET_ORIGIN = 17; 
    
    static private final int TGA_HEADER_ENCODING_NONE = 2;     
    static private final int TGA_HEADER_ENCODING_RLE = 10;     

    /**
     *
     * @param stream
     */
    public ImageModelTGA(RandomAccessFile stream) {
        super(stream);
        headerSize = TGA_HEADER_SIZE;   
    }

    
    /**
     * Wandelt einen allgemeinen Header in einen TGA Header um
     * 
     * @param srcHeader
     */
    @Override
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen
        DataBuffer dataBuffer = new DataBuffer(ImageModelTGA.TGA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Header speichern für blockweise Verarbeitung der Bilddaten
        header = new ImageHeader(srcHeader);
        header.getColorFormat().setMapping(ColorFormat.RED,2);
        header.getColorFormat().setMapping(ColorFormat.GREEN,1);
        header.getColorFormat().setMapping(ColorFormat.BLUE,0);
        
        // Headerfelder in Buffer schreiben
        byteBuffer.put(TGA_HEADER_OFFSET_ENCODING, (byte)2);
        byteBuffer.putShort(TGA_HEADER_OFFSET_WIDTH, (short)srcHeader.getWidth());
        byteBuffer.putShort(TGA_HEADER_OFFSET_HEIGHT, (short)srcHeader.getHeight());
        byteBuffer.put(TGA_HEADER_OFFSET_BPP, (byte)(srcHeader.getPixelSize() << 3));        
        byteBuffer.put(TGA_HEADER_OFFSET_ORIGIN, (byte)(1 << 5)); 
        
        // Kompression
        switch(header.getColorFormat().getEncoding()) {
            case RLE -> {
                byteBuffer.put(TGA_HEADER_OFFSET_ENCODING, (byte)TGA_HEADER_ENCODING_RLE);
            }
            case NONE -> {
                byteBuffer.put(TGA_HEADER_OFFSET_ENCODING, (byte)TGA_HEADER_ENCODING_NONE);
            }
            default -> {
                throw new IllegalArgumentException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        stream.seek(0);
        stream.write(byteBuffer.array());
    }

    /**
     * Wandelt einen TGA Header in einen allgemeinen Header um 
     * 
     * @return
     */
    @Override
    public ImageHeader readHeader() throws IOException{
        
        // Buffer für Header erstellen
        DataBuffer data = new DataBuffer(headerSize);
        ByteBuffer byteBuffer = data.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
       
        // Headerbytes von Stream einlesen
        readDataFromStream(data, 0, headerSize);
        
        // Headerfelder konvertieren
        ImageHeader newHeader = new ImageHeader();
        newHeader.setWidth(byteBuffer.getShort(TGA_HEADER_OFFSET_WIDTH));
        newHeader.setHeight(byteBuffer.getShort(TGA_HEADER_OFFSET_HEIGHT));
        newHeader.setPixelSize(byteBuffer.get(TGA_HEADER_OFFSET_BPP) >> 3); 
        
        // Kompression prüfen
        byte compression = byteBuffer.get(TGA_HEADER_OFFSET_ENCODING);
        switch (compression) {
            case TGA_HEADER_ENCODING_RLE:
                newHeader.getColorFormat().setEncoding(Encoding.RLE);
                break;
            case TGA_HEADER_ENCODING_NONE:
                newHeader.getColorFormat().setEncoding(Encoding.NONE);            
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
        
        return new ImageHeader((header = newHeader));
    }
}
