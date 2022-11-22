package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataReader;
import propra.imageconverter.data.IDataCallback;

/**
 *
 * @author pg
 */
public class ImageReader extends DataReader {

    protected int fileHeaderSize;
    protected ImageTranscoder decoder;    
    protected ImageHeader header;

    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public ImageReader( String file, 
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
     * @param dataTarget
     * @throws java.io.IOException
     */
    public void readImage(IDataCallback dataTarget) throws IOException {
        if(!isValid() 
        || dataTarget == null) {
            throw new IllegalArgumentException();
        }
        
        // Dekoder erstellen
        decoder = header.colorFormat().createTranscoder();
        decoder.begin(header.colorFormat(), 
                      checksumObj);
        
        // Dekomprimieren
        decoder.decode( binaryReader,
                        dataTarget);
            
        
        // Checksumme im Header vermerken
        if(checksumObj != null) {
            header.checksum(checksumObj.getValue());
        }
        
        decoder.end();
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
