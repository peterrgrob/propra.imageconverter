package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataWriter;
import propra.imageconverter.data.IDataCallback;

/**
 * Oberklasse für formatspezifische ImageWriter
 * 
 * @author pg
 */
public class ImageWriter extends DataWriter implements IDataCallback {
    
    // IO Variablen
    protected long contentTransfered;
    
    // Formatespezifische Variablen
    protected ImageHeader header;
    protected ColorFormat readColorFormat;
    protected ColorFormat writeColorFormat;
    protected int fileHeaderSize;

    // Farbverarbeitung
    protected ImageTranscoder encoder;    


    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public ImageWriter( String file, 
                        DataFormat.IOMode mode) throws IOException {
        super(file, mode);
        writeColorFormat = new ColorFormat();
        readColorFormat = new ColorFormat();
    }
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        header = new ImageHeader(srcHeader);
        writeColorFormat = srcHeader.colorFormat();
        
        // Dekoder erstellen
        encoder = writeColorFormat.createTranscoder();
    }
    
    /**
     * 
     * @param data 
     * @throws java.io.IOException 
     */
    @Override
    public void dataCallback(ByteBuffer data) throws IOException {
        if(data != null) {
            write(data);
        }
    }
    
    
    /**
     * Wandelt Bilddaten in bytes um. 
     * 
     * @return
     * @throws java.io.IOException
     */
    @Override
    public ByteBuffer write(ByteBuffer buffer) throws IOException {
        if(!isValid() 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Unterschiedliche Farbformate?
        if(!readColorFormat.equals(writeColorFormat)) {

            // Farben konvertieren
            ColorFormat.convertColorBuffer( buffer, readColorFormat, 
                                            buffer, writeColorFormat);
        }
        
        // In Datei kodieren
        encoder.encode( binaryWriter, 
                        buffer);
        
        // Anzahl der kodierten Bytes merken
        contentTransfered += buffer.limit();
        return buffer;
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
