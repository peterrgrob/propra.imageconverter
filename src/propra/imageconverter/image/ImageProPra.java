package propra.imageconverter.image;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.checksum.ChecksumPropra;
import propra.imageconverter.data.DataFormat;

/**
 * Schreibt ProPra Header
 * @author pg
 */
public class ImageProPra extends Image {
       
    // Offsets der Headerdaten in der Datei */
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
     * 
     */
    public ImageProPra(String file, DataFormat.IOMode mode) throws IOException {
        
        super(file, mode);
        fileHeaderSize = PROPRA_HEADER_SIZE;
        colorFormat = new ColorFormat(0, 2, 1);
        checksum = new ChecksumPropra();
    }
    
    /**
     * 
     * 
     */
    @Override
    public ImageMeta readHeader() throws IOException {
        if(binaryFile == null) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen       
        ByteBuffer bytes = ByteBuffer.allocate(fileHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
        // Headerbytes von Stream lesen
        read(bytes);
        
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
        ImageMeta newHeader = new ImageMeta();
        newHeader.width(bytes.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        newHeader.height(bytes.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        newHeader.pixelSize((int)bytes.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        newHeader.checksum(bytes.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        long dataLen = bytes.getLong(PROPRA_HEADER_OFFSET_DATALEN); 
        newHeader.encodedSize(dataLen);
        
        // RBG Farbmapping setzen
        newHeader.colorFormat(colorFormat);
        
        // Kompression initialisieren
        switch (bytes.get(PROPRA_HEADER_OFFSET_ENCODING)) {
            case PROPRA_HEADER_ENCODING_RLE -> {
                inCodec = new ImageCodecRLE(this);
                newHeader.colorFormat().encoding(DataFormat.Encoding.RLE);
            }
            case PROPRA_HEADER_ENCODING_NONE -> {
                inCodec = new ImageCodecRaw(this);
                newHeader.colorFormat().encoding(DataFormat.Encoding.NONE);
            }
            default -> throw new UnsupportedOperationException("Nicht unterstützte Kompression!");
        }
        
        // Prüfe ProPra Spezifikationen
        if( newHeader.isValid() == false 
        ||  (dataLen != (binaryFile.length() - PROPRA_HEADER_SIZE))) {
            throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
        } else if(newHeader.colorFormat().encoding() == DataFormat.Encoding.NONE) {
            
            // Prüfungen für unkomprimierte Dateien 
            if(newHeader.imageSize() != dataLen
            || newHeader.imageSize() != (binaryFile.length() - PROPRA_HEADER_SIZE)) {
                throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
            }
        }
        
        header = newHeader;
        return header;
    }  
    
    /**
     * Schreibt allgemeinen Header als ProPra Header
     * 
     */
    @Override
    public void writeHeader(ImageMeta srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        super.writeHeader(srcHeader);
        
        // DataBuffer für Header erstellen
        ByteBuffer byteBuffer = ByteBuffer.allocate(fileHeaderSize);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // ProPra spezifisches RBG Farbmapping setzen
        header.colorFormat().setMapping(colorFormat.getMapping());
        
        // Headerfelder in ByteBuffer schreiben
        DataFormat.putStringToByteBuffer(byteBuffer, 0, PROPRA_VERSION);
        byteBuffer.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        byteBuffer.putShort(PROPRA_HEADER_OFFSET_WIDTH,(short)srcHeader.width());
        byteBuffer.putShort(PROPRA_HEADER_OFFSET_HEIGHT,(short)srcHeader.height());
        byteBuffer.put(PROPRA_HEADER_OFFSET_BPP,(byte)(srcHeader.pixelSize() << 3));
        byteBuffer.putInt(PROPRA_HEADER_OFFSET_CHECKSUM, (int)srcHeader.checksum());
        
        // Kompression 
        switch(header.colorFormat().encoding()) {
            case RLE -> {
                byteBuffer.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)PROPRA_HEADER_ENCODING_RLE);
                byteBuffer.putLong(PROPRA_HEADER_OFFSET_DATALEN,srcHeader.encodedSize());
            }
            case NONE -> {
                byteBuffer.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)PROPRA_HEADER_ENCODING_NONE);
                byteBuffer.putLong(PROPRA_HEADER_OFFSET_DATALEN,(long)srcHeader.imageSize());
            }
            default -> {
                throw new IllegalArgumentException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        binaryFile.seek(0);
        byteBuffer.clear();
        write(byteBuffer);
    }
}
