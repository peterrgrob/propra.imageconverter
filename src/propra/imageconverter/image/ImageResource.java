package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataResource;

/**
 *
 * @author pg
 */
public class ImageResource extends DataResource {
    
    protected int fileHeaderSize;   
    protected ImageMeta header;
    protected ColorFormat colorFormat;

    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public ImageResource(   String file, 
                            DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        
        colorFormat = new ColorFormat();
    }
    

    /**
     * 
     * @return 
     * @throws IOException 
     */
    public ImageMeta readHeader() throws IOException {
        return null;
    }
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageMeta srcHeader) throws IOException {
        this.header = srcHeader;
    }
    
    /**
     *
     * @return
     */
    public int getFileHeaderSize() {
        return fileHeaderSize;
    }

    /**
     *
     * @return
     */
    public ImageMeta getHeader() {
        return header;
    }
}
