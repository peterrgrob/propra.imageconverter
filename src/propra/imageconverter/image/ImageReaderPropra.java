package propra.imageconverter.image;

import java.io.IOException;
import java.io.InputStream;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageReaderPropra extends ImageReader {
    static final String PROPRA_VERSION = "ProPraWiSe22";
    
    static final int PROPRA_HEADER_SIZE = 30;
    static final byte PROPRA_HEADER_OFFSET_ENCODING = 12;
    static final short PROPRA_HEADER_OFFSET_WIDTH = 13;
    static final short PROPRA_HEADER_OFFSET_HEIGHT = 15;
    static final byte PROPRA_HEADER_OFFSET_BPP = 17;
    static final int PROPRA_HEADER_OFFSET_DATALEN = 18;
    static final int PROPRA_HEADER_OFFSET_CHECKSUM = 26;
        
    public ImageReaderPropra(InputStream in) throws IOException {
        super(in);
    }

    /**
     *
     * @param len
     * @param buff
     * @return
     * @throws IOException
     */
    @Override
    protected ImageBuffer readBlock(int len, ImageBuffer buff) throws IOException {
        return super.readBlock(len, buff); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    protected ImageInfo readInfo() throws IOException {
        ImageInfo tInfo = new ImageInfo();
        DataBuffer data = new DataBuffer(readBytes(PROPRA_HEADER_SIZE));
        if(!data.isValid()){
            return null;
        }
        
        String version = data.getString(PROPRA_VERSION.length());
        if(version.compareTo(PROPRA_VERSION) != 0) {
           throw new java.io.IOException("Ungültige ProPra Formatkennung.");
        }
        
        tInfo.setWidth(data.getShortLittle(PROPRA_HEADER_OFFSET_WIDTH));
        tInfo.setHeight(data.getShortLittle(PROPRA_HEADER_OFFSET_HEIGHT));
        tInfo.setElementSize(data.get(PROPRA_HEADER_OFFSET_BPP) >> 3); 
        tInfo.setChecksum(data.getIntLittle(PROPRA_HEADER_OFFSET_CHECKSUM)); 
        long dataLen = data.getLongLittle(PROPRA_HEADER_OFFSET_DATALEN);    
        
        if(tInfo.isValid() == false) {
            throw new java.io.IOException("Ungültiges Bildformat.");
        }
        
        return (info = tInfo);
    }
    
}
