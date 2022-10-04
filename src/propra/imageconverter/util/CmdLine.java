package propra.imageconverter.util;

import java.util.HashMap;

/**
 *
 * @author pg
 */
 public class CmdLine {
    public static final String INPUT_KEY = "--input";
    public static final String OUTPUT_KEY = "--output";
    
    private HashMap<Options,String> options = new HashMap();
    
    /**
     * 
     */
    public enum Options {
        INPUT_FILE,
        INPUT_EXT,
        OUTPUT_FILE,
        OUTPUT_EXT;
    }
   
    /**
     * 
     * @param args 
     */
    public CmdLine(String[] args) {
        for(var a: args) {
            String[] tupel = a.split("=");
            if(tupel.length == 2) {
                switch (tupel[0]) {
                    case INPUT_KEY:
                        options.put(Options.INPUT_FILE, tupel[1]); 
                        options.put(Options.INPUT_EXT,getExtension(tupel[1]));
                    case OUTPUT_KEY:
                        options.put(Options.OUTPUT_FILE, tupel[1]);
                        options.put(Options.OUTPUT_EXT,getExtension(tupel[1]));
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
}
