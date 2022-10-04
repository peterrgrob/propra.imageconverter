package propra.imageconverter;

import java.io.FileNotFoundException;
import propra.imageconverter.util.*;
import propra.imageconverter.image.Converter;

/**
 *
 * @author pg
 */
public class ImageConverter {

    protected static final int ERROR_EXIT_CODE = 123;
    public static final Messages MSG = new MessagesSimple();
            
    /** 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CmdLine cmdLine = new CmdLine(args);
            System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
            System.out.println(cmdLine.getOption(CmdLine.Options.OUTPUT_FILE));
             
            System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_EXT));
            System.out.println(cmdLine.getOption(CmdLine.Options.OUTPUT_EXT));
            
            Converter c = new Converter();
            c.Convert(cmdLine);
        }
        catch(FileNotFoundException e) {
            System.exit(ERROR_EXIT_CODE);
        }
    }
    
}
