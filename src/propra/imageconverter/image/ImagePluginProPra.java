package propra.imageconverter.image;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import propra.imageconverter.util.ChecksumPropra;
import propra.imageconverter.util.DataBuffer;

/**
 *  ProPra spezifische Implementierung
 * 
 * @author pg
 */
public class ImagePluginProPra extends ImagePlugin {
    
    static final String PROPRA_VERSION = "ProPraWiSe22";
    static final int PROPRA_HEADER_SIZE = 30;
    static final int PROPRA_HEADER_OFFSET_ENCODING = 12;
    static final int PROPRA_HEADER_OFFSET_WIDTH = 13;
    static final int PROPRA_HEADER_OFFSET_HEIGHT = 15;
    static final int PROPRA_HEADER_OFFSET_BPP = 17;
    static final int PROPRA_HEADER_OFFSET_DATALEN = 18;
    static final int PROPRA_HEADER_OFFSET_CHECKSUM = 26;   
    
    /**
     *
     */
    public ImagePluginProPra() {
        headerSize = PROPRA_HEADER_SIZE;  
        checksumObj = new ChecksumPropra();
    }

    /**
     *  Wandelt einen allgemeinen Header in einen ProPra Header um
     * 
     * @param info
     * @return Header als DataBuffer
     */
    @Override
    public DataBuffer headerToBytes(ImageHeader info) {
        if(info.isValid() == false) {
            throw new IllegalArgumentException();
        }
        
        // DataBuffer für Header erstellen
        DataBuffer dataBuffer = new DataBuffer(PROPRA_HEADER_SIZE);
        ByteBuffer byteBuffer = dataBuffer.getBuffer();
        byteBuffer.order(byteOrder);
        
        // ProPra spezifisches RBG Farbmapping setzen
        header = new ImageHeader(info);
        header.getColorType().setMapping(ColorType.RED,0);
        header.getColorType().setMapping(ColorType.GREEN,2);
        header.getColorType().setMapping(ColorType.BLUE,1);
        
        // Headerfelder in ByteBuffer schreiben
        dataBuffer.put(PROPRA_VERSION,0);
        byteBuffer.put(PROPRA_HEADER_OFFSET_ENCODING, (byte)0);
        byteBuffer.putShort(PROPRA_HEADER_OFFSET_WIDTH,(short)info.getWidth());
        byteBuffer.putShort(PROPRA_HEADER_OFFSET_HEIGHT,(short)info.getHeight());
        byteBuffer.put(PROPRA_HEADER_OFFSET_BPP,(byte)(info.getElementSize() << 3));
        byteBuffer.putLong(PROPRA_HEADER_OFFSET_DATALEN,(long)info.getTotalSize());
       
        return dataBuffer;
    }

    /**
     * Wandelt einen ProPra Header in allgemeinen Header um 
     * 
     * @param data
     * @return Allgemeiner Header
     */
    @Override
    public ImageHeader bytesToHeader(DataBuffer data) {
        ByteBuffer bytes = data.getBuffer();
        bytes.order(this.byteOrder);
        
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
        ImageHeader tInfo = new ImageHeader();
        tInfo.setWidth(bytes.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        tInfo.setHeight(bytes.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        tInfo.setElementSize((int)bytes.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        tInfo.setChecksum(bytes.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        tInfo.setEncoding(ImageHeader.Encoding.UNCOMPRESSED);
        long dataLen = bytes.getLong(PROPRA_HEADER_OFFSET_DATALEN);   
        
        // RBG Farbmapping setzen
        tInfo.getColorType().setMapping(ColorType.RED,0);
        tInfo.getColorType().setMapping(ColorType.GREEN,2);
        tInfo.getColorType().setMapping(ColorType.BLUE,1);
        
        // Prüfe ProPra Spezifikationen
        if( tInfo.isValid() == false 
        ||  (tInfo.getTotalSize() != dataLen)
        ||  (dataLen != (initialAvailableBytes - PROPRA_HEADER_SIZE))
        ||  (tInfo.getTotalSize() != (initialAvailableBytes - PROPRA_HEADER_SIZE))
        ||  (bytes.get(PROPRA_HEADER_OFFSET_ENCODING) != 0)) {
            throw new UnsupportedOperationException("Ungültiges ProPra Dateiformat!");
        }
        
        return (header = tInfo);
    }  

    /**
     * Führt Prüsummenberechnung für einen DataBuffer durch und speichert
     * diese im Header-Block
     * 
     * @param data
     * @param headerData
     * @return Prüfsumme
     */
    @Override
    public long checkContent(DataBuffer data, DataBuffer headerData) {
        if (data == null
        ||  headerData == null) {
            throw new IllegalArgumentException();
        }
        // Prüfsumme berechnen und in den Header schreiben
        if(isCheckable()) {
            long chkSum = check(data.getBuffer().array());
            headerData.getBuffer().putInt(PROPRA_HEADER_OFFSET_CHECKSUM, (int)chkSum);
            header.setChecksum(chkSum);
        }
        
        return header.getChecksum();
    }
}
