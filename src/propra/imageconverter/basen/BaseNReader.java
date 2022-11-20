package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataReader;
import propra.imageconverter.data.IDataTranscoder;

/**
 *
 * @author pg
 */
public class BaseNReader extends DataReader {
    
    private final BaseN decoder;
    private final BaseNFormat format;
    
    /**
     * 
     * @param file
     * @param format
     * @throws IOException 
     */
    public BaseNReader(String file, BaseNFormat format) throws IOException {
        super(file, IOMode.BINARY);
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
    public int read(ByteBuffer buffer) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
            
        // Alphabet vorhanden?
        if(decoder.dataFormat().getAlphabet().length() == 0) {
            
            // Alphabet aus Datei einlesen und DatenFormat ableiten
            String alphabet = binaryReader.readLine();
            decoder.dataFormat().setEncoding(alphabet);
            buffer.limit(buffer.capacity() - alphabet.length() - 1);
        }
                    
        // Daten in temoprären Puffer einlesen
        ByteBuffer readBuffer = ByteBuffer.allocate(buffer.limit());
        binaryReader.read(readBuffer.array(), 0, readBuffer.capacity());

        // In Puffer dekodieren
        int decodedLen = (int)decoder.apply( IDataTranscoder.Operation.DECODE, 
                                            readBuffer, 
                                            buffer);

        buffer.limit(decodedLen);
        return decodedLen;
    }
}
