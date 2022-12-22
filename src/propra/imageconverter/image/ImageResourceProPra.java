package propra.imageconverter.image;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.ChecksumPropra;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataUtil;

/**
 * Schreibt ProPra Header
 * @author pg
 */
public class ImageResourceProPra extends ImageResource {
    
    // Magic String
    static final String PROPRA_VERSION = "ProPraWiSe22";
    
    // Offsets der Headerdaten in der Datei */
    static final int PROPRA_HEADER_SIZE = 30;
    static final int PROPRA_HEADER_OFFSET_ENCODING = 12;
    static final int PROPRA_HEADER_OFFSET_WIDTH = 13;
    static final int PROPRA_HEADER_OFFSET_HEIGHT = 15;
    static final int PROPRA_HEADER_OFFSET_BPP = 17;
    static final int PROPRA_HEADER_OFFSET_DATALEN = 18;
    static final int PROPRA_HEADER_OFFSET_CHECKSUM = 26;   
    
    // Kodierungen
    static final int PROPRA_HEADER_ENCODING_NONE = 0;     
    static final int PROPRA_HEADER_ENCODING_RLE = 1;   
    static final int PROPRA_HEADER_ENCODING_HUFFMAN = 2;  
    
    /**
     * 
     * @param file
     * @param write
     * @throws IOException 
     */
    public ImageResourceProPra(String file, boolean write) throws IOException {
        super(file, write);
        
        fileHeaderSize = PROPRA_HEADER_SIZE;
        colorFormat = new ColorFormat(0, 2, 1);
        
        checksum = new ChecksumPropra();
        inStream.setChecksum(checksum);
        outStream.setChecksum(checksum);
    }
    
    /**
     * 
     * 
     * @return
     * @throws java.io.IOException
     */
    @Override
    public ImageHeader readHeader() throws IOException {
        if(binaryFile == null) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen       
        ByteBuffer bytes = ByteBuffer.allocate(fileHeaderSize);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
        // Headerbytes von Stream lesen
        binaryFile.read(bytes.array());
        
        // Prüfe Formatkennung
        String version;
        try {
            version = DataUtil.getStringFromByteBuffer(bytes, PROPRA_VERSION.length());
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
        newHeader.colorFormat(colorFormat);
        
        // Kompression initialisieren
        switch (bytes.get(PROPRA_HEADER_OFFSET_ENCODING)) {
            case PROPRA_HEADER_ENCODING_HUFFMAN -> {     
                newHeader.colorFormat().encoding(DataFormat.Encoding.HUFFMAN);
            }
            case PROPRA_HEADER_ENCODING_RLE -> {
                newHeader.colorFormat().encoding(DataFormat.Encoding.RLE);
            }
            case PROPRA_HEADER_ENCODING_NONE -> {
                newHeader.colorFormat().encoding(DataFormat.Encoding.NONE);
            }
            default -> throw new UnsupportedOperationException("Nicht unterstützte Kompression!");
        }
        inCodec = createCodec(newHeader);
        
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
     * @param srcHeader
     * @throws IOException 
     */
    @Override
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        if(srcHeader.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        super.writeHeader(srcHeader);
        
        // DataBuffer für Header erstellen
        ByteBuffer buff = ByteBuffer.allocate(fileHeaderSize);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        
        // ProPra spezifisches RBG Farbmapping setzen
        header.colorFormat().setMapping(colorFormat.getMapping());
        
        // Headerfelder in ByteBuffer schreiben
        DataUtil.putStringToByteBuffer(buff, 0, PROPRA_VERSION);
        buff.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        buff.putShort(PROPRA_HEADER_OFFSET_WIDTH,(short)header.width());
        buff.putShort(PROPRA_HEADER_OFFSET_HEIGHT,(short)header.height());
        buff.put(PROPRA_HEADER_OFFSET_BPP,(byte)(header.pixelSize() << 3));
        buff.putInt(PROPRA_HEADER_OFFSET_CHECKSUM, (int)header.checksum());
        
        // Kompression 
        switch(header.colorFormat().encoding()) {
            case HUFFMAN -> {
                buff.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)PROPRA_HEADER_ENCODING_HUFFMAN);
                buff.putLong(PROPRA_HEADER_OFFSET_DATALEN,header.encodedSize());
            }
            case RLE -> {
                buff.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)PROPRA_HEADER_ENCODING_RLE);
                buff.putLong(PROPRA_HEADER_OFFSET_DATALEN,header.encodedSize());
            }
            case NONE -> {
                buff.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)PROPRA_HEADER_ENCODING_NONE);
                buff.putLong(PROPRA_HEADER_OFFSET_DATALEN,(long)header.imageSize());
            }
            default -> {
                throw new IllegalArgumentException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        binaryFile.seek(0);
        binaryFile.write(buff.array());
    }
}
