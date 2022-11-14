package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.ChecksumPropra;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataFormat.Encoding;

/**
 *  ProPra spezifische Implementierung
 * 
 * @author pg
 */
public class ImageModelProPra extends ImageModel {
    
    static private final String PROPRA_VERSION = "ProPraWiSe22";
    static private final int PROPRA_HEADER_SIZE = 30;
    static private final int PROPRA_HEADER_OFFSET_ENCODING = 12;
    static private final int PROPRA_HEADER_OFFSET_WIDTH = 13;
    static private final int PROPRA_HEADER_OFFSET_HEIGHT = 15;
    static private final int PROPRA_HEADER_OFFSET_BPP = 17;
    static private final int PROPRA_HEADER_OFFSET_DATALEN = 18;
    static private final int PROPRA_HEADER_OFFSET_CHECKSUM = 26;   
    
    static private final int PROPRA_HEADER_ENCODING_NONE = 0;     
    static private final int PROPRA_HEADER_ENCODING_RLE = 1;   
    
    /**
     *
     * @param stream
     */
    public ImageModelProPra(RandomAccessFile stream) {
        super(stream);
        headerSize = PROPRA_HEADER_SIZE;  
        checksumObj = new ChecksumPropra();
    }

    /**
     *  Wandelt einen allgemeinen Header in einen ProPra Header um
     * 
     * @param srcHeader
     */
    @Override
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen
        DataBuffer dataBuffer = new DataBuffer(PROPRA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // ProPra spezifisches RBG Farbmapping setzen
        header = new ImageHeader(srcHeader);
        header.getColorFormat().setMapping(ColorFormat.RED,0);
        header.getColorFormat().setMapping(ColorFormat.GREEN,2);
        header.getColorFormat().setMapping(ColorFormat.BLUE,1);
        
        // Headerfelder in ByteBuffer schreiben
        dataBuffer.put(PROPRA_VERSION,0);
        byteBuffer.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        byteBuffer.putShort(PROPRA_HEADER_OFFSET_WIDTH,(short)srcHeader.getWidth());
        byteBuffer.putShort(PROPRA_HEADER_OFFSET_HEIGHT,(short)srcHeader.getHeight());
        byteBuffer.put(PROPRA_HEADER_OFFSET_BPP,(byte)(srcHeader.getPixelSize() << 3));
        byteBuffer.putLong(PROPRA_HEADER_OFFSET_DATALEN,(long)srcHeader.getImageSize());
        
        // Kompression 
        switch(header.getColorFormat().getEncoding()) {
            case RLE -> {
                byteBuffer.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)PROPRA_HEADER_ENCODING_RLE);
            }
            case NONE -> {
                byteBuffer.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)PROPRA_HEADER_ENCODING_NONE);
            }
            default -> {
                throw new IllegalArgumentException("Ungültige Kompression.");
            }                   
        }
        
        // Headerbytes in Stream schreiben
        stream.seek(0);
        stream.write(byteBuffer.array());
    }

    /**
     * Wandelt einen ProPra Header in allgemeinen Header um 
     * 
     * @return Allgemeiner Header
     * @throws java.io.IOException
     */
    @Override
    public ImageHeader readHeader() throws IOException {
        if(stream == null) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen       
        DataBuffer data = new DataBuffer(headerSize);
        ByteBuffer bytes = data.getBuffer();
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
        // Headerbytes von Stream lesen
        readDataFromStream(data, 0, headerSize);
        
        // Prüfe Formatkennung
        String version;
        try {
            version = data.getString(PROPRA_VERSION.length());
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
        if(version.compareTo(PROPRA_VERSION) != 0) {
           throw new UnsupportedOperationException("Ungültige ProPra-Kennung!");
        }
        
        // Headerfelder in allgemeinen Header konvertieren
        ImageHeader newHeader = new ImageHeader();
        newHeader.setWidth(bytes.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        newHeader.setHeight(bytes.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        newHeader.setPixelSize((int)bytes.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        newHeader.setChecksum(bytes.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        long dataLen = bytes.getLong(PROPRA_HEADER_OFFSET_DATALEN);   
        
        // RBG Farbmapping setzen
        newHeader.getColorFormat().setMapping(ColorFormat.RED,0);
        newHeader.getColorFormat().setMapping(ColorFormat.GREEN,2);
        newHeader.getColorFormat().setMapping(ColorFormat.BLUE,1);
        
        // Kompression prüfen
        switch (bytes.get(PROPRA_HEADER_OFFSET_ENCODING)) {
            case PROPRA_HEADER_ENCODING_RLE:
                newHeader.getColorFormat().setEncoding(Encoding.RLE);
                break;
            case PROPRA_HEADER_ENCODING_NONE:
                newHeader.getColorFormat().setEncoding(Encoding.NONE);  
                break;
            default:
                throw new UnsupportedOperationException("Nicht unterstützte Kompression!");
        }
        
        // Prüfe ProPra Spezifikationen
        if( newHeader.isValid() == false 
        ||  (dataLen != (stream.length() - PROPRA_HEADER_SIZE))) {
            throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
        } else if(newHeader.getColorFormat().getEncoding() == Encoding.NONE) {
            
            // Prüfungen für unkomprimierte Dateien 
            if(newHeader.getImageSize() != dataLen
            || newHeader.getImageSize() != (stream.length() - PROPRA_HEADER_SIZE)) {
                throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
            }
        }
        
        // Neuen Header zurückgeben
        return new ImageHeader(header = newHeader);
    }  
}
