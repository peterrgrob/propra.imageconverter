package propra.imageconverter.basen;

import java.io.IOException;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataFormat.Mode;
import propra.imageconverter.data.DataReader;
import propra.imageconverter.data.DataTranscoder;

/**
 *
 * @author pg
 */
public class BaseNReader extends DataReader {
    
    private final BaseN decoder;
    private final DataFormat format;
    
    /**
     * 
     * @param file
     * @param format
     * @throws IOException 
     */
    public BaseNReader(String file, DataFormat format) throws IOException {
        super(file, Mode.BINARY);
        this.format = format;
        decoder = new BaseN(this.format);
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    @Override
    public int read(DataBuffer buffer) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        buffer.setDataLength(buffer.getSize());
            
        // Alphabet vorhanden?
        if(decoder.dataFormat().getAlphabet().length() == 0) {
            // Alphabet aus Datei einlesen und DatenFormat ableiten
            String alphabet = binaryReader.readLine();
            decoder.dataFormat().setEncoding(alphabet);
            buffer.setDataLength(buffer.getSize() - alphabet.length() - 1);
        }
            
        // Daten einlesen
        binaryReader.read(buffer.getBytes(), 0, buffer.getDataLength());

        // In temporären Puffer dekodieren
        DataBuffer decodeBuffer = new DataBuffer(buffer.getSize());
        decoder.apply(  DataTranscoder.Operation.DECODE, 
                            buffer, 
                            decodeBuffer);

        // In Ausgabepuffer kopieren
        buffer.getBuffer().put( 0, 
                                decodeBuffer.getBytes(), 
                                0, 
                                decodeBuffer.getDataLength());

        buffer.setDataLength(decodeBuffer.getDataLength());
        
        return buffer.getDataLength();
    }
}
