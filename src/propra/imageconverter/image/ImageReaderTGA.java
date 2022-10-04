package propra.imageconverter.image;

import java.io.IOException;
import java.io.InputStream;
import propra.imageconverter.util.DataBuffer;

import propra.imageconverter.util.Utility;

/**
 *
 * @author pg
 */
public class ImageReaderTGA extends ImageReader {
    
    static final int TGA_HEADER_SIZE = 18;
    static final int TGA_HEADER_OFFSET_ENCODING = 2;
    static final int TGA_HEADER_OFFSET_WIDTH = 12;
    static final int TGA_HEADER_OFFSET_HEIGHT = 14;
    static final int TGA_HEADER_OFFSET_BPP = 16;
    static final int TGA_HEADER_OFFSET_ORIGIN = 17;
    
    public ImageReaderTGA(InputStream in) throws IOException {
        super(in);
    }

    /**
     * 
     * @return 
     * @throws java.io.IOException 
     */
    @Override
    protected ImageInfo readInfo() throws IOException {
        ImageInfo tInfo = new ImageInfo();
        DataBuffer data = new DataBuffer(readBytes(TGA_HEADER_SIZE));
        if(!data.isValid()){
            return null;
        }
        
        switch(data.get(TGA_HEADER_OFFSET_ENCODING)) {
            case 2:
                tInfo.setEncoding(ImageInfo.Encoding.UNCOMPRESSED);
                break;
            default:
                throw new java.lang.UnsupportedOperationException("Nicht unterstütztes Bildformat.");
        }
        
        tInfo.setWidth(data.getShortLittle(TGA_HEADER_OFFSET_WIDTH));
        tInfo.setHeight(data.getShortLittle(TGA_HEADER_OFFSET_HEIGHT));
        tInfo.setElementSize(data.get(TGA_HEADER_OFFSET_BPP) >> 3); 
        
        if(tInfo.isValid() == false) {
            throw new java.io.IOException("Ungültiges Bildformat.");
        }
        
        return (info = tInfo);
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
        DataBuffer data = new DataBuffer(readBytes(len));

        for(int i=0; i<info.getElementCount(); i++) {
            buff.set(i, data.getColorBGR());
        }
        
        return buff;
    }
}
