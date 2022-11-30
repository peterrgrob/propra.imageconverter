package propra.imageconverter;
import propra.imageconverter.basen.BaseNCodec;
import java.util.HashMap;
import propra.imageconverter.basen.BaseNFormat;
import propra.imageconverter.image.ColorFormat;

/**
 * Hilfsklasse die Kommandozeilenparameter parsed und speichert, diese
 * können über Option abgefragt werden.
 * 
 * @author pg
 */
 public class CmdLine {
    
    // Enthält geparste Optionen, ggfs. mit Wert
    private final HashMap<Options, String> options;
    
    // Optionen für die Kommandozeile
    public enum Options {
        INPUT_FILE("--input"),
        OUTPUT_FILE("--output"),
        COMPRESSION("--compression"),
        ENCODE_BASE_32("--encode-base-32"),
        DECODE_BASE_32("--decode-base-32"),  
        ENCODE_BASE_N("--encode-base-n"),
        DECODE_BASE_N("--decode-base-n");
        
        private final String key;
        
        private Options(String key) {
            this.key = key;
        }
        
        public String getKey() {
            return key;
        }
    }
   
    /**
     * 
     */
    public CmdLine(String[] args) {
        this.options = new HashMap<>();
        
        // Iteriere und parse alle Argumente
        for(var a: args) {
            
            // Aufteilen in Komponenten
            String[] tupel = a.split("=");
            if(tupel.length > 0) {
                
                // Mit enums vergleichen und bei Bedarf speichern
                for (Options opt : Options.values()) { 
                    
                    if(opt.getKey().equals(tupel[0])) {
                        String param = "";
                        
                        if(tupel.length > 1) {
                            param = tupel[1];
                        }
                        
                        options.put(opt, param);
                    }
                }
            }
        }
    } 
    
    /**
     * @param opt
     * @return Wert der Option oder null
     */
    public String getOption(Options opt) {
        return options.get(opt);
    }
    
    /**
     * @return Encoding, je nach Options
     */
    public ColorFormat.Encoding getColorEncoding() {
        String enc = getOption(Options.COMPRESSION);
        if(enc != null) {
            if(enc.compareTo("rle") == 0) {
                return ColorFormat.Encoding.RLE;
            }
        }
        return ColorFormat.Encoding.NONE;
    }
    
    /**
     *
     * @param opt
     * @return 
     */
    public String getExtension(Options opt) {
        return getExtension(getOption(opt));
    }
    
    /**
     * 
     * @param path
     * @return Extrahiert Dateiendung
     */
    static private String getExtension(String path) {
        String[] components = path.split("\\.");
        if(components.length < 2) {
            return "";
        }
        return components[components.length - 1].toLowerCase();
    } 
    
    /**
     *
     * @return Gibt passendes BaseNFormat Objekt zurück, oder null
     */
    public BaseNFormat getBaseNDataFormat() {
        String alphabet = null;
        
        // Alphabet wählen
        if( options.containsKey(Options.ENCODE_BASE_32)
        ||  options.containsKey(Options.DECODE_BASE_32)) {
            
            alphabet = BaseNFormat.BASE_32_ALPHABET;
            
        } else if(  options.containsKey(Options.DECODE_BASE_N)) {
            
            // Leeres Alphabet, wird später aus Datei geladen
            return new BaseNFormat();
            
        }else if(  options.containsKey(Options.ENCODE_BASE_N)) {
            
            // Von der Kommandozeile übernhemen
            alphabet = options.get(Options.ENCODE_BASE_N);
            if(alphabet == null) {
               return null;
            } else if(alphabet.length() == 0) {
               return null; 
            }           
        } 
        
        // Format Objekt zurückgeben
        return new BaseNFormat(alphabet);
    }
    
    /**
     * 
     * @return true wenn Base en- oder dekodierung 
     */
    public boolean isBaseTask() {
        return (options.containsKey(Options.ENCODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_32)
            ||  options.containsKey(Options.ENCODE_BASE_N)
            ||  options.containsKey(Options.DECODE_BASE_N));
    }
    
    /**
     * @return true, wenn BaseNCodec&Base32 Dekodierung
     */
    public boolean isBaseNDecode() {
        return (options.containsKey(Options.DECODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_N)); 
    }
    
    /**
     * @return true, wenn BaseNCodec Verarbeitung
     */
    public boolean isBaseN() {
        return (options.containsKey(Options.ENCODE_BASE_N)
            ||  options.containsKey(Options.DECODE_BASE_N)); 
    }
    
    /**
     * @return true, wenn Base32 Verarbeitung
     */
    public boolean isBase32() {
        return (options.containsKey(Options.ENCODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_32)); 
    }
}
