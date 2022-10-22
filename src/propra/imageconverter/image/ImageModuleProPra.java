package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.ChecksumPropra;
import propra.imageconverter.util.DataBuffer;

/**
 *  ProPra spezifische Implementierung
 * 
 * @author pg
 */
public class ImageModuleProPra extends ImageModule {
    
    static private final String PROPRA_VERSION = "ProPraWiSe22";
    static private final int PROPRA_HEADER_SIZE = 30;
    static private final int PROPRA_HEADER_OFFSET_ENCODING = 12;
    static private final int PROPRA_HEADER_OFFSET_WIDTH = 13;
    static private final int PROPRA_HEADER_OFFSET_HEIGHT = 15;
    static private final int PROPRA_HEADER_OFFSET_BPP = 17;
    static private final int PROPRA_HEADER_OFFSET_DATALEN = 18;
    static private final int PROPRA_HEADER_OFFSET_CHECKSUM = 26;   
    
    /**
     *
     * @param stream
     */
    public ImageModuleProPra(RandomAccessFile stream) {
        super(stream);
        headerSize = PROPRA_HEADER_SIZE;  
        checksumObj = new ChecksumPropra();
    }

    /**
     *  Wandelt einen allgemeinen Header in einen ProPra Header um
     * 
     * @param info
     */
    @Override
    public void writeHeader(ImageHeader info) throws IOException {
        if(info.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen
        DataBuffer dataBuffer = new DataBuffer(PROPRA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // ProPra spezifisches RBG Farbmapping setzen
        header = new ImageHeader(info);
        header.getColorType().setMapping(ColorFormat.RED,0);
        header.getColorType().setMapping(ColorFormat.GREEN,2);
        header.getColorType().setMapping(ColorFormat.BLUE,1);
        
        // Headerfelder in ByteBuffer schreiben
        dataBuffer.put(PROPRA_VERSION,0);
        byteBuffer.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        byteBuffer.putShort(PROPRA_HEADER_OFFSET_WIDTH,(short)info.getWidth());
        byteBuffer.putShort(PROPRA_HEADER_OFFSET_HEIGHT,(short)info.getHeight());
        byteBuffer.put(PROPRA_HEADER_OFFSET_BPP,(byte)(info.getPixelSize() << 3));
        byteBuffer.putLong(PROPRA_HEADER_OFFSET_DATALEN,(long)info.getBufferSize());
        
        stream.seek(0);
        stream.write(byteBuffer.array());
    }

    /**
     * Wandelt einen ProPra Header in allgemeinen Header um 
     * 
     * @param data
     * @return Allgemeiner Header
     * @throws java.io.IOException
     */
    @Override
    public ImageHeader readHeader() throws IOException {
        if(stream == null) {
            throw new IllegalArgumentException();
        }
        DataBuffer data = new DataBuffer(headerSize);
        ByteBuffer bytes = data.getBuffer();
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
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
        
        // Headerfelder einlesen
        ImageHeader newHeader = new ImageHeader();
        newHeader.setWidth(bytes.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        newHeader.setHeight(bytes.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        newHeader.setPixelSize((int)bytes.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        newHeader.setChecksum(bytes.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        newHeader.setEncoding(ImageHeader.Encoding.UNCOMPRESSED);
        long dataLen = bytes.getLong(PROPRA_HEADER_OFFSET_DATALEN);   
        
        // RBG Farbmapping setzen
        newHeader.getColorType().setMapping(ColorFormat.RED,0);
        newHeader.getColorType().setMapping(ColorFormat.GREEN,2);
        newHeader.getColorType().setMapping(ColorFormat.BLUE,1);
        
        // Prüfe ProPra Spezifikationen
        if( newHeader.isValid() == false 
        ||  (newHeader.getBufferSize() != dataLen)
        ||  (dataLen != (stream.length() - PROPRA_HEADER_SIZE))
        ||  (newHeader.getBufferSize() != (stream.length() - PROPRA_HEADER_SIZE))
        ||  (bytes.get(PROPRA_HEADER_OFFSET_ENCODING) != 0)) {
            throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
        }
        
        return (header = newHeader);
    }  
}
