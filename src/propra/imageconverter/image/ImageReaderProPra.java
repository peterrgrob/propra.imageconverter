package propra.imageconverter.image;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.data.DataFormat;

/**
 *
 * @author pg
 */
public class ImageReaderProPra extends ImageReader {
    
    static final String PROPRA_VERSION = "ProPraWiSe22";
    static final int PROPRA_HEADER_SIZE = 30;
    static final int PROPRA_HEADER_OFFSET_ENCODING = 12;
    static final int PROPRA_HEADER_OFFSET_WIDTH = 13;
    static final int PROPRA_HEADER_OFFSET_HEIGHT = 15;
    static final int PROPRA_HEADER_OFFSET_BPP = 17;
    static final int PROPRA_HEADER_OFFSET_DATALEN = 18;
    static final int PROPRA_HEADER_OFFSET_CHECKSUM = 26;   
    
    static final int PROPRA_HEADER_ENCODING_NONE = 0;     
    static final int PROPRA_HEADER_ENCODING_RLE = 1;   
    
    /**
     *
     * @param stream
     */
    public ImageReaderProPra(String file, DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        formatHeaderSize = PROPRA_HEADER_SIZE;   
    }
    
    /**
     * Wandelt einen ProPra Header in allgemeinen Header um 
     * 
     * @throws java.io.IOException
     */
    @Override
    public ImageHeader readHeader() throws IOException {
        if(binaryReader == null) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen       
        ByteBuffer bytes = ByteBuffer.allocate(formatHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
        // Headerbytes von Stream lesen
        read(bytes, 0, formatHeaderSize);
        
        // Prüfe Formatkennung
        String version;
        try {
            version = DataFormat.getStringFromByteBuffer(bytes, PROPRA_VERSION.length());
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
        if(version.compareTo(PROPRA_VERSION) != 0) {
           throw new UnsupportedOperationException("Ungültige ProPra-Kennung!");
        }
        
        // Headerfelder in allgemeinen Header konvertieren
        ImageHeader newHeader = new ImageHeader();
        newHeader.width(bytes.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        newHeader.height(bytes.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        newHeader.pixelSize((int)bytes.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        newHeader.checksum(bytes.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        long dataLen = bytes.getLong(PROPRA_HEADER_OFFSET_DATALEN); 
        newHeader.encodedSize(dataLen);
        
        // RBG Farbmapping setzen
        newHeader.colorFormat().setMapping(ColorFormat.RED,0);
        newHeader.colorFormat().setMapping(ColorFormat.GREEN,2);
        newHeader.colorFormat().setMapping(ColorFormat.BLUE,1);
        
        // Kompression prüfen
        switch (bytes.get(PROPRA_HEADER_OFFSET_ENCODING)) {
            case PROPRA_HEADER_ENCODING_RLE:
                newHeader.colorFormat().encoding(DataFormat.Encoding.RLE);
                break;
            case PROPRA_HEADER_ENCODING_NONE:
                newHeader.colorFormat().encoding(DataFormat.Encoding.NONE);  
                break;
            default:
                throw new UnsupportedOperationException("Nicht unterstützte Kompression!");
        }
        
        // Prüfe ProPra Spezifikationen
        if( newHeader.isValid() == false 
        ||  (dataLen != (binaryReader.length() - PROPRA_HEADER_SIZE))) {
            throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
        } else if(newHeader.colorFormat().encoding() == DataFormat.Encoding.NONE) {
            
            // Prüfungen für unkomprimierte Dateien 
            if(newHeader.imageSize() != dataLen
            || newHeader.imageSize() != (binaryReader.length() - PROPRA_HEADER_SIZE)) {
                throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
            }
        }
        
        header = newHeader;
        return header;
    }  
}
