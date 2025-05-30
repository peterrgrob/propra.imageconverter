package propra.imageconverter.image;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.checksum.ChecksumPropra;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.data.IDataTranscoder.Compression;
import propra.imageconverter.checksum.IChecksum;

/**
 * Propra spezifische Implementierung einer ImageRessource
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
    
    // Prüfsumme 
    protected IChecksum checksum;
    
    /**
     * Konstruktor
     */
    public ImageResourceProPra(String file, boolean write) throws IOException {
        super(file, write);
        
        fileHeaderSize = PROPRA_HEADER_SIZE;
        header.setFormat(Color.Format.COLOR_RBG);
        checksum = new ChecksumPropra();
        
        inStream.setChecksum(checksum);
        outStream.setChecksum(checksum);
    }
    
    /**
     * Propra unterstützt Prüfsumme
     */
    @Override
    public boolean isChecked() {
        return true;
    }
    
    /**
     * Gibt aktuell laufende Prüfsumme zurück
     */
    @Override
    public long getCurrentChecksum() {
        return checksum.getValue();
    }
    
    /**
     * Liest Propra Header aus Stream ein, wandelt in allgemeine Bildattribute um
     * und prüft auf korrekte Werte
     */
    @Override
    public ImageAttributes readHeader() throws IOException {
        
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
        
        // Header einlesen
        ImageAttributes attributes = new ImageAttributes();
        attributes.setWidth(bytes.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        attributes.setHeight(bytes.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        attributes.setChecksum(bytes.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        attributes.setDataLength(bytes.getLong(PROPRA_HEADER_OFFSET_DATALEN));
        attributes.setFormat(Color.Format.COLOR_RBG);
        int bpp = (int)bytes.get(PROPRA_HEADER_OFFSET_BPP); 
        
        // Kompression initialisieren
        switch (bytes.get(PROPRA_HEADER_OFFSET_ENCODING)) {
            case PROPRA_HEADER_ENCODING_HUFFMAN -> {     
                attributes.setCompression(Compression.HUFFMAN);
            }
            case PROPRA_HEADER_ENCODING_RLE -> {
                attributes.setCompression(Compression.RLE);
            }
            case PROPRA_HEADER_ENCODING_NONE -> {
                attributes.setCompression(Compression.UNCOMPRESSED);
            }
            default -> throw new UnsupportedOperationException("Nicht unterstützte Kompression!");
        }
        
        // Prüfe ProPra Spezifikationen
        if( attributes.getHeight() <= 0 || attributes.getWidth() <= 0 || bpp != 24
        ||  (attributes.getDataLength() != (binaryFile.length() - PROPRA_HEADER_SIZE))) {
            throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
        } else if(attributes.getCompression() == Compression.UNCOMPRESSED) {
            // Prüfungen für unkomprimierte Dateien 
            if(attributes.getPixelCount() * Color.PIXEL_SIZE != attributes.getDataLength()
            || attributes.getPixelCount() * Color.PIXEL_SIZE != (binaryFile.length() - PROPRA_HEADER_SIZE)) {
                throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
            }
        }
        
        // Header setzen
        setHeader(attributes);
        
        return header;
    }  
    
    /**
     * Schreibt Bildattribute im Propra-Format in den Stream
     */
    @Override
    public void writeHeader() throws IOException {
        
        // DataBuffer für Header erstellen
        ByteBuffer buff = ByteBuffer.allocate(fileHeaderSize);
        buff.order(ByteOrder.LITTLE_ENDIAN);
         
        header.setChecksum(checksum.getValue());
        
        // Attribute in ByteBuffer schreiben
        DataUtil.putStringToByteBuffer(buff, 0, PROPRA_VERSION);
        buff.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        buff.putShort(PROPRA_HEADER_OFFSET_WIDTH, (short)header.getWidth());
        buff.putShort(PROPRA_HEADER_OFFSET_HEIGHT, (short)header.getHeight());
        buff.put(PROPRA_HEADER_OFFSET_BPP, (byte)(24));
        buff.putInt(PROPRA_HEADER_OFFSET_CHECKSUM, (int)getCurrentChecksum());
        
        // Kompression 
        switch(header.getCompression()) {
            case HUFFMAN -> {
                buff.put(PROPRA_HEADER_OFFSET_ENCODING, 
                        (byte)PROPRA_HEADER_ENCODING_HUFFMAN);
                buff.putLong(PROPRA_HEADER_OFFSET_DATALEN,
                            header.getDataLength());
            }
            case RLE -> {
                buff.put(PROPRA_HEADER_OFFSET_ENCODING, 
                        (byte)PROPRA_HEADER_ENCODING_RLE);
                buff.putLong(PROPRA_HEADER_OFFSET_DATALEN,
                            header.getDataLength());
            }
            case UNCOMPRESSED -> {
                buff.put(PROPRA_HEADER_OFFSET_ENCODING, 
                            (byte)PROPRA_HEADER_ENCODING_NONE);
                buff.putLong(PROPRA_HEADER_OFFSET_DATALEN,
                            (long)header.getPixelCount() * Color.PIXEL_SIZE);
            }
            default -> {
                throw new UnsupportedOperationException("Ungültige Kompression.");
            }                   
        }
        
        // In Stream schreiben
        binaryFile.seek(0);
        binaryFile.write(buff.array());
    }
}
