package propra.imageconverter.basen;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataResource;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTranscoder.EncodeMode;
import propra.imageconverter.basen.BaseN.BaseNEncoding;
import propra.imageconverter.PropraException;


/**
 *  BaseN Implementierung einer Ressource
 */
public class BaseNResource extends DataResource {
    
    // Alphabettabellen, aus Performancegründen keine Hashmap
    private String alphabet = new String();
    private final byte[] alphabetMap = new byte[256];
    
    // Verwendete BaseN Kodierung
    private BaseNEncoding baseEncoding;
    
    // Konvertierungsresource
    private IDataResource currentResource; 
    
    public BaseNResource(String file, String alphabet, boolean write) throws IOException {
        super(file, write);
        setAlphabet(alphabet);
    }
    
    /**
     * Dekodiert BaseN Ressource und speichert in output
     */
    public void decode(IDataResource output) throws IOException {
        currentResource = output;
                
        // Alphabet aus Datei laden?
        if(!isValid()) {
            setAlphabet(binaryFile.readLine());
        }
        
        PropraException.printMessage("Format: " + baseEncoding.toString() 
                                    + "\nAlphabet: " + alphabet + "\nDekodierung starten...");
        
        // Daten dekodieren, Target als Lambda-Ausdruck
        BaseN decoder = new BaseN(this);
        decoder.decode(getInputStream(), (ByteBuffer data, boolean lastBlock, IDataTranscoder caller) -> {
                        currentResource.getOutputStream().write(data);
        });
        
        PropraException.printMessage("Abgeschlossen");
    } 
    
    /**
     * Kodiert Binärdaten zu BaseN
     */
    public void encode(IDataResource input) throws IOException {
        
        // Bei BaseN Alphabet in Datei schreiben
        if(baseEncoding != BaseNEncoding.BASE_DEFAULT_32) {
            binaryFile.writeChars(alphabet + "\n");
        }
            
        PropraException.printMessage("Format: " + baseEncoding.toString() 
                                    + "\nAlphabet: " + alphabet + "\nKodierung starten...");
        
        // Daten von Datei lesen
        ByteBuffer data = ByteBuffer.wrap(input.getInputStream().readAllBytes());

        // Daten in Resource kodieren
        BaseN encoder = new BaseN(this);
        encoder.beginEncoding(EncodeMode.ENCODE, getOutputStream());
        encoder.encode(data, true);
        encoder.endEncoding();
        
        PropraException.printMessage("Abgeschlossen");
    }
    
    /**
     * Alphabet setzen und prüfen
     */
    public void setAlphabet(String alphabet) {
        if(alphabet == null) {
            this.alphabet = BaseNEncoding.BASE_32_ALPHABET;
            baseEncoding = BaseNEncoding.BASE_DEFAULT_32;
        } else {
            if(alphabet.length() > 0) {
                // Alphabet setzen und Mappingarray erstellen
                this.alphabet = alphabet;

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
                // Mapping aus dem Alphabet vorberechnen für die Dekodierung
                for(int i=0; i<alphabet.length(); i++) {
                    alphabetMap[alphabet.getBytes()[i]] = (byte)i;
                }
            } else {
                throw new IllegalArgumentException("Leeres BaseN Alphabet!");
            }
        }
        
        // Mapping aus dem Alphabet vorberechnen für die Dekodierung
        for(int i=0; i<this.alphabet.length(); i++) {
            alphabetMap[this.alphabet.getBytes()[i]] = (byte)i;
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
