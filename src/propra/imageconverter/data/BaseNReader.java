package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public class BaseNReader extends DataReader {
    
    private final BaseN decoder;
    private final DataFormat format;
    
    public BaseNReader(String file, DataFormat format) throws IOException {
        super(file, Mode.BINARY);
        this.format = format;
        decoder = new BaseN(format);
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
        
        buffer.setCurrDataLength(buffer.getSize());
            
        // Alphabet vorhanden?
        if(decoder.getDataFormat().getAlphabet().length() == 0) {
            // Alphabet aus Datei einlesen und DatenFormat ableiten
            String alphabet = binaryReader.readLine();
            decoder.getDataFormat().setEncoding(alphabet);
            buffer.setCurrDataLength(buffer.getSize() - alphabet.length() - 1);
        }
            
        // Daten einlesen
        binaryReader.read(buffer.getBytes(), 0, buffer.getCurrDataLength());

        // In temporären Puffer dekodieren
        DataBuffer decodeBuffer = new DataBuffer(buffer.getSize());
        decoder.transcode(  DataTranscoder.Operation.DECODE, 
                            buffer, 
                            decodeBuffer);

        // In Ausgabepuffer kopieren
        buffer.getBuffer().put( 0, 
                                decodeBuffer.getBytes(), 
                                0, 
                                decodeBuffer.getCurrDataLength());

        buffer.setCurrDataLength(decodeBuffer.getCurrDataLength());
        
        return buffer.getCurrDataLength();
    }
}
