package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataResource;

/**
 *
 * @author pg
 */
public class BaseNResource extends DataResource {
    
    private final BaseN decoder;
    private final BaseNFormat format;
    
    /**
     * 
     * @param file
     * @param format
     * @throws IOException 
     */
    public BaseNResource(String file, BaseNFormat format) throws IOException {
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
    public void read(ByteBuffer buffer) throws IOException {
        if( !isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
            
        // Alphabet vorhanden?
        if(decoder.dataFormat().getAlphabet().length() == 0) {
            
            // Alphabet aus Datei einlesen und DatenFormat ableiten
            String alphabet = binaryFile.readLine();
            decoder.dataFormat().setEncoding(alphabet);
        }

        // In Ausgabe dekodieren
        //decoder.decode( binaryFile, 
        //                buffer);

    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    @Override
    public void write(ByteBuffer buffer) throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Puffer erstellen
        /*int len = encoder.transcodedBufferLength(IDataTranscoder.Operation.ENCODE, buffer);
        ByteBuffer encodeBuffer = ByteBuffer.allocate(len);*/
        
        // Daten kodieren
        decoder.encode( binaryFile, 
                        buffer,
                        true);

        // Alphabet in Datei schreiben 
        if(format.getBaseEncoding() != BaseNFormat.BaseNEncoding.BASE_32) {
            txtWriter.write(decoder.dataFormat().getAlphabet() + "\n");
        }

        // Zeichen in Datei schreiben
        //writeBinaryToTextFile(encodeBuffer);
    }
}
