package propra.imageconverter;
import propra.imageconverter.basen.BaseN;
import propra.imageconverter.data.DataFormat;
import java.util.HashMap;
import propra.imageconverter.image.ColorFormat;

/**
 * Hilfsklasse die Kommandozeilenparameter parsed und speichert, diese
 können über Option abgefragt werden.
 * 
 * @author pg
 */
 public class CmdLine {
     
    private final HashMap<Options, String> options;
    
    /*
     * 
     */
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
     * @param args 
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
     * 
     * @param opt
     * @return 
     */
    public String getOption(Options opt) {
        return options.get(opt);
    }
    
    /**
     *
     * @return
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
    public String getOptionExtension(Options opt) {
        return getExtension(getOption(opt));
    }
    
    /**
     * 
     * @param path
     * @return 
     */
    private String getExtension(String path) {
        String[] components = path.split("\\.");
        if(components.length < 2) {
            return "";
        }
        return components[components.length-1].toLowerCase();
    } 
    
    /**
     *
     * @return
     */
    public DataFormat getBaseNDataFormat() {
        String alphabet = null;
        
        // Alphabet setzen
        if( options.containsKey(Options.ENCODE_BASE_32)
        ||  options.containsKey(Options.DECODE_BASE_32)) {
            alphabet = BaseN.BASE_32_ALPHABET;
        } else if(  options.containsKey(Options.DECODE_BASE_N)) {
            // Alphabet wird aus Datei geladen
            return new DataFormat();
        }else if(  options.containsKey(Options.ENCODE_BASE_N)) {
            alphabet = options.get(Options.ENCODE_BASE_N);
            if(alphabet == null) {
               throw new IllegalArgumentException("Ungültiges Alphabet für Base-N Kodierung.");
            }
        }  
        
        // DataFormat Objekt zurückgeben
        return new DataFormat(alphabet);
    }
    
    /**
     * 
     * @return 
     */
    public boolean isBaseTask() {
        return (options.containsKey(Options.ENCODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_32)
            ||  options.containsKey(Options.ENCODE_BASE_N)
            ||  options.containsKey(Options.DECODE_BASE_N));
    }
    
    /**
     * 
     * @return 
     */
    public boolean isBaseNDecode() {
        return (options.containsKey(Options.DECODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_N)); 
    }
    
    public boolean isBaseN() {
        return (options.containsKey(Options.ENCODE_BASE_N)
            ||  options.containsKey(Options.DECODE_BASE_N)); 
    }
    
    public boolean isBase32() {
        return (options.containsKey(Options.ENCODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_32)); 
    }
}
