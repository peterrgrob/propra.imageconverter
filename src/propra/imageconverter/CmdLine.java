package propra.imageconverter;

import java.util.HashMap;
import propra.imageconverter.data.IDataTranscoder.Compression;

/**
 * Hilfsklasse die Kommandozeilenparameter parsed und speichert, diese
 * können über Options abgefragt werden.
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
        
        // Schlüssel
        private final String key;
        
        private Options(String key) {
            this.key = key;
        }
        
        public String getKey() {
            return key;
        }
    }
   
    /**
     * Parsed alle Optionen der Kommandozeile in Hashtable
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
     * Gibt den Wert der Option zurück
     */
    public String getOption(Options opt) {
        return options.get(opt);
    }
    
    /**
     * Gibt gewünschte Kompression zurück
     */
    public Compression getCompression() {
        String enc = getOption(Options.COMPRESSION);
        if(enc != null) {
            switch(enc) {
                case "rle" -> {
                    return Compression.RLE;
                }
                case "huffman" -> {
                    return Compression.HUFFMAN;
                }
                case "auto" -> {
                    return Compression.AUTO;
                }
            }
        }
        return Compression.UNCOMPRESSED;
    }
    
    /**
     * Gibt Alphabet von der Konsole zurück, oder null
     */
    public String getAlphabet() {
        // Alphabet wählen
        if( options.containsKey(Options.ENCODE_BASE_32)
        ||  options.containsKey(Options.DECODE_BASE_32)
        ||  options.containsKey(Options.DECODE_BASE_N)) {
            return null;
        }else if(options.containsKey(Options.ENCODE_BASE_N)) { 
            // Von der Kommandozeile übernehmen
            String a = options.get(Options.ENCODE_BASE_N);
            if(a == null) {
                throw new IllegalArgumentException("Kein Alphabet übergeben!");
            } else if(a.length() == 0) {
                throw new IllegalArgumentException("Kein Alphabet übergeben!");
            }  
            return a;
        } 
        throw new UnsupportedOperationException("Nicht unterstützte BaseN Kodierung.");
    }
    
    /**
     * true wenn allgemeiner BaseN-Modus 
     */
    public boolean isBaseTask() {
        return (options.containsKey(Options.ENCODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_32)
            ||  options.containsKey(Options.ENCODE_BASE_N)
            ||  options.containsKey(Options.DECODE_BASE_N));
    }
    
    /**
     * true, wenn allgemeine BaseN Dekodierung
     */
    public boolean isBaseNDecode() {
        return (options.containsKey(Options.DECODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_N)); 
    }
    
    /**
     * true, wenn BaseN Modus
     */
    public boolean isBaseN() {
        return (options.containsKey(Options.ENCODE_BASE_N)
            ||  options.containsKey(Options.DECODE_BASE_N)); 
    }
    
    /**
     * true, wenn Base32 Modus
     */
    public boolean isBase32() {
        return (options.containsKey(Options.ENCODE_BASE_32)
            ||  options.containsKey(Options.DECODE_BASE_32)); 
    }
}
