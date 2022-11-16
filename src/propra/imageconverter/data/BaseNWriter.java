package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public class BaseNWriter extends DataWriter {
    
    private final BaseN encoder;
    private final DataFormat format;
    
    public BaseNWriter(String file, DataFormat format) throws IOException {
        super(file, Mode.TEXT);
        this.format = format;
        encoder = new BaseN(format);
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    @Override
    public int write(DataBuffer buffer) throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        DataBuffer encodeBuffer = new DataBuffer();
        encoder.transcode(  DataTranscoder.Operation.ENCODE, 
                            buffer, 
                            encodeBuffer);

        // Alphabet in Datei schreiben
        if(format.isBaseN()) {
            txtWriter.write(encoder.getDataFormat().getAlphabet() + "\n");
        }

        // Zeichen in Datei schreiben
        writeBinaryToTextFile(encodeBuffer);
        
        return encodeBuffer.getCurrDataLength();
    }
}
