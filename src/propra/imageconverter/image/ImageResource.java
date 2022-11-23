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
    protected ImageTranscoder decoder;    
    protected ImageHeader header;

    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public ImageResource(   String file, 
                            DataFormat.IOMode mode) throws IOException {
        super(file, mode);
    }
    

    /**
     * 
     * @return 
     * @throws IOException 
     */
    public ImageHeader readHeader() throws IOException {
        return null;
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
    public ImageHeader getHeader() {
        return header;
    }
}
