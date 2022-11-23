package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataResource;

/**
 * Oberklasse für formatspezifische ImageWriter
 * 
 * @author pg
 */
public class ImageWriter extends DataResource {
    
    // Formatespezifische Variablen
    protected ImageHeader header;
    protected int fileHeaderSize;
   


    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public ImageWriter( String file, 
                        DataFormat.IOMode mode) throws IOException {
        super(file, mode);
    }
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        header = new ImageHeader(srcHeader);
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
