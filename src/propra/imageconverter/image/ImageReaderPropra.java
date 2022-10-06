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
        info = new ImageInfo();
        info.getColorType().setChannel(Color.RED,new Color.ChannelInfo(2));
        info.getColorType().setChannel(Color.GREEN,new Color.ChannelInfo(0));
        info.getColorType().setChannel(Color.BLUE,new Color.ChannelInfo(1));
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
                
        DataBuffer dataBuffer = new DataBuffer(buffer);
        ByteBuffer bytes = dataBuffer.getBuffer();
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        
        String version = dataBuffer.getString(PROPRA_VERSION.length());
        if(version.compareTo(PROPRA_VERSION) != 0) {
           throw new java.io.IOException("Ungültige ProPra Formatkennung.");
        }
        
        tInfo.setWidth(bytes.getShort(PROPRA_HEADER_OFFSET_WIDTH));
        tInfo.setHeight(bytes.getShort(PROPRA_HEADER_OFFSET_HEIGHT));
        tInfo.setElementSize(bytes.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        tInfo.setChecksum(bytes.getInt(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        long dataLen = bytes.getLong(PROPRA_HEADER_OFFSET_DATALEN);    
        
        if( tInfo.isValid() == false || 
            tInfo.getTotalSize() != dataLen) {
            throw new java.io.IOException("Ungültiges Bildformat.");
        }
        
        return (info = tInfo);
    }
    
}
