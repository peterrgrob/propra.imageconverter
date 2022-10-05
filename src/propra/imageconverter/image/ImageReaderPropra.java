package propra.imageconverter.image;

import java.io.IOException;
import java.io.InputStream;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataBufferLittle;

/**
 *
 * @author pg
 */
public class ImageReaderPropra extends ImageReader {
    static final String PROPRA_VERSION = "ProPraWiSe22";
    static final int PROPRA_HEADER_SIZE = 30;
    static final int PROPRA_HEADER_OFFSET_ENCODING = 12;
    static final int PROPRA_HEADER_OFFSET_WIDTH = 13;
    static final int PROPRA_HEADER_OFFSET_HEIGHT = 15;
    static final int PROPRA_HEADER_OFFSET_BPP = 17;
    static final int PROPRA_HEADER_OFFSET_DATALEN = 18;
    static final int PROPRA_HEADER_OFFSET_CHECKSUM = 26;
    
    
    
    public ImageReaderPropra(InputStream in) throws IOException {
        super(in);
        this.byteOrder = DataBuffer.Order.LITTLE_ENDIAN;
        this.colorOrder.blueShift = 1;
        this.colorOrder.greenShift = 0;
        this.colorOrder.redShift = 2;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    public ImageInfo readInfo() throws IOException {
        ImageInfo tInfo = new ImageInfo();
        
        byte[] buffer = new byte[PROPRA_HEADER_SIZE];
        if(readBytes(PROPRA_HEADER_SIZE, buffer) != PROPRA_HEADER_SIZE) {
            throw new java.io.IOException("Ungültiger ProPra Header.");
        }
                
        DataBufferLittle data = new DataBufferLittle(buffer);
        if(!data.isValid()){
            return null;
        }
        
        String version = data.getString(PROPRA_VERSION.length());
        if(version.compareTo(PROPRA_VERSION) != 0) {
           throw new java.io.IOException("Ungültige ProPra Formatkennung.");
        }
        
        tInfo.setWidth(data.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        tInfo.setHeight(data.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        tInfo.setElementSize(data.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        tInfo.setChecksum(data.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        long dataLen = data.getLong(PROPRA_HEADER_OFFSET_DATALEN);    
        
        if( tInfo.isValid() == false || 
            tInfo.getTotalSize() != dataLen) {
            throw new java.io.IOException("Ungültiges Bildformat.");
        }
        
        return (info = tInfo);
    }
    
}
