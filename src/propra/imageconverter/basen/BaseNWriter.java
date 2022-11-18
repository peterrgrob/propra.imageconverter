package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
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
        
        // Daten kodieren
        ByteBuffer encodeBuffer = ByteBuffer.allocate(0);
        encoder.apply(IDataTranscoder.Operation.ENCODE, 
                            buffer, 
                            encodeBuffer);

        // Alphabet in Datei schreiben
        if(format.encoding() == DataFormat.Encoding.BASEN) {
            txtWriter.write(encoder.dataFormat().getAlphabet() + "\n");
        }

        // Zeichen in Datei schreiben
        writeBinaryToTextFile(encodeBuffer);
        
        return encodeBuffer.limit();
    }
}
