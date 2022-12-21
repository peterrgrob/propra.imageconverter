package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataTarget;

/**
 *
 */
public class BaseNResource  extends DataResource {
    
    // BaseN Kodierungsformat
    private final BaseNFormat format;
    
    /**
     * 
     */
    public BaseNResource(   String file, 
                            BaseNFormat format,
                            boolean write) throws IOException {
        super(file, write);
        
        this.format = format;
        if(format == null) {
            format = new BaseNFormat(); 
        }
    }
    
    /**
     * Alphabet aus Datei einlesen und DatenFormat ableiten
     */
    public String readAlphabet() throws IOException {
        String alphabet = binaryFile.readLine();
        format.setEncoding(alphabet);
        return alphabet;
    }
    
    /**
     * Alphabet in Datei schreiben
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
     * @param res 
     */
    public void decode(IDataTarget target) throws IOException {
        // Alphabet aus Datei laden?
        if(!format.isValidAlphabet()) {
            format.setEncoding(readAlphabet());
        }

        // Decoder erstellen
        BaseNCodec decoder = new BaseNCodec(this,getFormat());

        // Datei in Puffer dekodieren
        decoder.begin(DataFormat.Operation.DECODE);
        decoder.decode(target);
        decoder.end();
    } 
    
    /**
     * 
     * @param res 
     */
    public void encode(IDataTarget target) throws IOException {
        // Encoder erstellen
        BaseNCodec encoder = new BaseNCodec(this,getFormat());

        // Daten von Datei lesen
        ByteBuffer data = ByteBuffer.allocate((int)binaryFile.length());
        binaryFile.read(data.array());

        // Daten in Resource dekodieren
        encoder.begin(DataFormat.Operation.ENCODE);
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
