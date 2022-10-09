package propra.imageconverter.image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import propra.imageconverter.util.DataBuffer;


/**
 *
 * @author pg
 */
public class ImageReaderPropra extends ImageReader {
    /**
     * 
     */
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
     * @param in
     * @throws IOException
     */
    public ImageReaderPropra(InputStream in) throws IOException {
        super(in);
        this.byteOrder = ByteOrder.LITTLE_ENDIAN;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    public ImageHeader readHeader() throws IOException {
        /**
         * Header-Bytes von Stream lesen
         */
        byte[] buffer = new byte[PROPRA_HEADER_SIZE];
        if(readBytes(buffer,PROPRA_HEADER_SIZE) != PROPRA_HEADER_SIZE) {
            throw new java.io.IOException("Ungültiger ProPra Header.");
        }   
        DataBuffer dataBuffer = new DataBuffer(buffer);
        ByteBuffer bytes = dataBuffer.getBuffer();
        bytes.order(this.byteOrder);
        
        /**
         * Prüfe Formatkennung
         */
        String version = dataBuffer.getString(PROPRA_VERSION.length());
        if(version.compareTo(PROPRA_VERSION) != 0) {
           throw new java.io.IOException("Ungültige ProPra Formatkennung.");
        }
        
        /**
         * Headerfelder einlesen
         */
        ImageHeader tInfo = new ImageHeader();
        tInfo.setWidth(bytes.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        tInfo.setHeight(bytes.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        tInfo.setElementSize(bytes.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        tInfo.setChecksum(bytes.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        tInfo.setEncoding(ImageHeader.Encoding.UNCOMPRESSED);
        long dataLen = bytes.getLong(PROPRA_HEADER_OFFSET_DATALEN);   
        
        /**
         * RBG Farbmapping setzen
         */
        tInfo.getColorType().setMapping(ColorType.RED,0);
        tInfo.getColorType().setMapping(ColorType.GREEN,2);
        tInfo.getColorType().setMapping(ColorType.BLUE,1);
        
        /**
         * Prüfe ProPra Spezifikationen
         */
        if( tInfo.isValid() == false 
        ||  (tInfo.getTotalSize() != dataLen)
        ||  (dataLen != (intialAvailableBytes - PROPRA_HEADER_SIZE))
        ||  (tInfo.getTotalSize() != (intialAvailableBytes - PROPRA_HEADER_SIZE))
        ||  (bytes.get(PROPRA_HEADER_OFFSET_ENCODING) != 0)) {
            throw new java.io.IOException("Ungültiges ProPra Bildformat.");
        }
        
        return (header = tInfo);
    }
    
}
