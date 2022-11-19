package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataWriter;
import propra.imageconverter.data.IDataTranscoder;

/**
 *
 * @author pg
 */
public class BaseNWriter extends DataWriter {
    
    private final BaseN encoder;
    private final BaseNFormat format;
    
    /**
     * 
     * @param file
     * @param format
     * @throws IOException 
     */
    public BaseNWriter(String file, BaseNFormat format) throws IOException {
        super(file, IOMode.TEXT);
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
    public int write(ByteBuffer buffer) throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Puffer erstellen
        int len = encoder.transcodedBufferLength(IDataTranscoder.Operation.ENCODE, buffer);
        ByteBuffer encodeBuffer = ByteBuffer.allocate(len);
        
        // Daten kodieren
        encoder.apply(IDataTranscoder.Operation.ENCODE, 
                            buffer, 
                            encodeBuffer);

        // Alphabet in Datei schreiben 
        if(format.getBaseEncoding() != BaseNFormat.BaseNEncoding.BASE_32) {
            txtWriter.write(encoder.dataFormat().getAlphabet() + "\n");
        }

        // Zeichen in Datei schreiben
        writeBinaryToTextFile(encodeBuffer);
        
        return encodeBuffer.limit();
    }
}
