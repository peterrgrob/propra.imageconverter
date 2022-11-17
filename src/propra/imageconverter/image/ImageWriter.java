package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataTranscoder;
import propra.imageconverter.data.DataWriter;

/**
 *
 * @author pg
 */
public class ImageWriter extends DataWriter {
    
    protected final int BLOCK_SIZE;
    protected int formatHeaderSize;
    protected DataBuffer writeBuffer;
    protected long contentTransfered;
    protected ImageHeader header;

    // Farbverarbeitung
    protected ImageFilterColor colorConverter; 
    protected ImageTranscoder encoder;    


    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public ImageWriter(String file, DataFormat.Mode mode) throws IOException {
        super(file, mode);
        BLOCK_SIZE = 1024 * 4096 * 3;
        this.writeBuffer = new DataBuffer(BLOCK_SIZE);
        colorConverter = new ImageFilterColor();
    }
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        srcHeader = new ImageHeader(header);
    }
    
    /**
     *
     * @throws java.io.IOException
     */
    @Override
    public void begin() throws IOException {     
        super.begin();
        
        // Format für die Konvertierung setzen
        colorConverter.outFormat(header.colorFormat());
        colorConverter.begin();
        
        // Enkoder erstellen
        encoder = header.colorFormat().createTranscoder();
        if(encoder != null) {
            encoder.begin(header.colorFormat());
        }
        
        contentTransfered = 0;
    }
    
    /**
     * Wandelt Bilddaten in bytes um. 
     * 
     * @return
     * @throws java.io.IOException
     */
    @Override
    public int write(DataBuffer buffer) throws IOException {
        if(!isValid() 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Ausgabeformat für Konvertierung setzen und Block konvertieren
        colorConverter.inFormat(header.colorFormat());
        colorConverter.apply(buffer);   
        
        DataBuffer tmpBuffer = buffer;
        
        // Kompression erforderlich?
        if(encoder != null) {
            // Block komprimieren
            encoder.apply( DataTranscoder.Operation.ENCODE, 
                                buffer, 
                                writeBuffer);
            tmpBuffer = writeBuffer;
        } 
        
        // Prüfsumme mit aktuellem Block aktualisieren
        updateChecksum(tmpBuffer); 
        
        // Block in Datei schreiben
        write(tmpBuffer, 0, tmpBuffer.getDataLength());
        
        // Anzahl der kodierten Bytes merken
        contentTransfered += tmpBuffer.getDataLength();
        return tmpBuffer.getDataLength();
    }
    
    /**
     *
     */
    @Override
    public void end() {
        
        super.end();
        
        // Checksumme im Header vermerken
        if(checksumObj != null) {
            checksumObj.end();
            header.checksum(getChecksum());
        }
        
        // Farbkonvertierung abschließen
        colorConverter.end(); 
        
        // Kompression abschließen
        if(encoder != null) {
            encoder.end();
            header.encodedSize(contentTransfered);
        }
    }
    
    /**
     *
     * @return
     */
    public int getHeaderSize() {
        return formatHeaderSize;
    }

    /**
     *
     * @return
     */
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    /**
     *
     * @return
     */
    public ImageHeader getHeader() {
        return header;
    }
}
