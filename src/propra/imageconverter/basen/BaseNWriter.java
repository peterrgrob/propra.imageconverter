package propra.imageconverter.basen;

import java.io.IOException;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataFormat.Mode;
import propra.imageconverter.data.DataTranscoder;
import propra.imageconverter.data.DataWriter;

/**
 *
 * @author pg
 */
public class BaseNWriter extends DataWriter {
    
    private final BaseN encoder;
    private final DataFormat format;
    
    /**
     * 
     * @param file
     * @param format
     * @throws IOException 
     */
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
        
        // Daten kodieren
        DataBuffer encodeBuffer = new DataBuffer();
        encoder.apply(  DataTranscoder.Operation.ENCODE, 
                            buffer, 
                            encodeBuffer);

        // Alphabet in Datei schreiben
        if(format.isBaseN()) {
            txtWriter.write(encoder.dataFormat().getAlphabet() + "\n");
        }

        // Zeichen in Datei schreiben
        writeBinaryToTextFile(encodeBuffer);
        
        return encodeBuffer.getDataLength();
    }
}
