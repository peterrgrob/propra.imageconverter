package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataCodec.Operation;
import propra.imageconverter.data.IDataResource;
import propra.imageconverter.data.IDataTarget;

/**
 *
 * @author pg
 */
public class BaseNResource extends DataResource {
    
    // BaseN Kodierungsformat
    private final BaseNFormat format;
    
    /**
     * 
     * @param file
     * @param format
     * @param write
     * @throws IOException
     */
    public BaseNResource(String file, BaseNFormat format, boolean write) throws IOException {
        super(file, write);
        
        this.format = format;
        if(format == null) {
            format = new BaseNFormat(); 
        }
    }
    
    /**
     * Alphabet aus Datei einlesen und DatenFormat ableiten
     * @return 
     * @throws java.io.IOException
     */
    public String readAlphabet() throws IOException {
        String alphabet = binaryFile.readLine();
        format.setEncoding(alphabet);
        return alphabet;
    }
    
    /**
     * Alphabet in Datei schreiben
     * @param alphabet
     * @throws java.io.IOException
     */
    public void writeAlphabet(String alphabet) throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }
        
        // Alphabet in Datei schreiben 
        if(format.getBaseEncoding() != BaseNFormat.BaseNEncoding.BASE_32) {
            binaryFile.writeChars(alphabet + "\n");
        }
    }
    
    
    
    /**
     * 
     * @param output 
     * @throws java.io.IOException 
     */
    public void decode(IDataTarget output) throws IOException {
        // Alphabet aus Datei laden?
        if(!format.isValidAlphabet()) {
            format.setEncoding(readAlphabet());
        }

        // Decoder erstellen
        BaseN decoder = new BaseN(this,getFormat());

        // Datei in Puffer dekodieren
        decoder.begin(Operation.DECODE);
        decoder.decode(output);
        decoder.end();
    } 
    
    /**
     * 
     * @param input 
     * @throws java.io.IOException 
     */
    public void encode(IDataResource input) throws IOException {
        // Encoder erstellen
        BaseN encoder = new BaseN(this,getFormat());

        // Daten von Datei lesen
        ByteBuffer data = ByteBuffer.wrap(input.getInputStream().readAllBytes());
 
        // Daten in Resource kodieren
        encoder.begin(Operation.ENCODE);
        encoder.encode(data, true);
        encoder.end();
    }
    
    /**
     * 
     * @return BaseNFormat
     */
    public BaseNFormat getFormat() {
        return format;
    }
}
