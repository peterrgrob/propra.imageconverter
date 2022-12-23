package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataCompression.Operation;
import propra.imageconverter.data.IDataResource;
import propra.imageconverter.data.IDataTarget;

/**
 *
 * @author pg
 */
public class BaseNResource extends DataResource {

    // Alphabettabellen, aus Performancegründen keine Hashmap
    private String alphabet = new String();
    private final byte[] alphabetMap = new byte[256];
    
    // Verwendete BaseN Kodierung
    private BaseNEncoding baseEncoding;
    
    // Standard Base32 Alphabet
    public static String BASE_32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
    
    /**
     * 
     * @param file
     * @param format
     * @param write
     * @throws IOException
     */
    public BaseNResource(String file, String alphabet, boolean write) throws IOException {
        super(file, write);
        setAlphabet(alphabet);
    }
    
    /**
     * Alphabet aus Datei einlesen und DatenFormat ableiten
     * @return 
     * @throws java.io.IOException
     */
    public String readAlphabet() throws IOException {
        setAlphabet(binaryFile.readLine());
        return alphabet;
    }
    
    /**
     * Alphabet in Datei schreiben
     * @param alphabet
     * @throws java.io.IOException
     */
    public void writeAlphabet(String alphabet) throws IOException {
        // Alphabet in Datei schreiben 
        if(baseEncoding != BaseNEncoding.BASE_32) {
            binaryFile.writeChars(alphabet + "\n");
        }
    }
    
    /**
     *
     * @param alphabet
     */
    public void setAlphabet(String alphabet) {
        if(alphabet == null) {
            throw new IllegalArgumentException();
        }
        
        if(alphabet.length() > 0) {
            // Alphabet setzen und Mappingarray erstellen
            this.alphabet = alphabet;
            for(int i=0; i<alphabet.length(); i++) {
                alphabetMap[alphabet.getBytes()[i]] = (byte)i;
            }

            // Kodierung ableiten
            switch(alphabet.length()) {
                case 2 -> {this.baseEncoding = BaseNEncoding.BASE_2;}
                case 4 -> {this.baseEncoding = BaseNEncoding.BASE_4;}     
                case 8 -> {this.baseEncoding = BaseNEncoding.BASE_8;}
                case 16 -> {this.baseEncoding = BaseNEncoding.BASE_16;}
                case 32 -> {this.baseEncoding = BaseNEncoding.BASE_32;}
                case 64 -> {this.baseEncoding = BaseNEncoding.BASE_64;}
                default -> {throw new IllegalArgumentException("Ungültige Base-N Alphabetlänge");}
            }
        }
    }
    
    /**
     * Alphabet prüfen
     */
    public boolean isValid() {
        switch(this.baseEncoding) {
            case BASE_2, BASE_4, BASE_8, BASE_16, BASE_32, BASE_64 -> {
                if(alphabet == null) {
                    return false;
                }
                if(!(alphabet.length() == 2
                || alphabet.length() == 4
                || alphabet.length() == 8
                || alphabet.length() == 16
                || alphabet.length() == 32
                || alphabet.length() == 64)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 
     * @param output 
     * @throws java.io.IOException 
     */
    public void decodeTo(IDataTarget output) throws IOException {
        // Alphabet aus Datei laden?
        if(!isValid()) {
            setAlphabet(readAlphabet());
        }

        // Decoder erstellen
        BaseN decoder = new BaseN(this);

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
    public void encodeFrom(IDataResource input) throws IOException {
        // Encoder erstellen
        BaseN encoder = new BaseN(this);

        // Daten von Datei lesen
        ByteBuffer data = ByteBuffer.wrap(input.getInputStream().readAllBytes());
 
        // Daten in Resource kodieren
        encoder.begin(Operation.ENCODE);
        encoder.encode(data, true);
        encoder.end();
    }
    
    /**
     * 
     * @return 
     */
    public BaseNEncoding getBaseNEncoding() {
        return baseEncoding;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public byte[] getAlphabetMap() {
        return alphabetMap;
    }
    
    
}
