package propra.imageconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;
import java.util.logging.Level;
import propra.imageconverter.util.*;

/**
 * Einstiegsklasse für ImageConverter 
 * 
 * @author pg
 */
public class ImageConverter {
    
    // Fehlercode
    private static final int ERROR_EXIT_CODE = 123;
    

            
    /** 
     * Programmeinstieg
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Komandozeilenparameter parsen.
            CmdLine cmdLine = new CmdLine(args);
            
            // Ein- und Ausgabedateipfad auf der Konsole ausgeben
            System.out.println("Dateien:");
            System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
            System.out.println(cmdLine.getOption(CmdLine.Options.OUTPUT_FILE));
            
            // Klasseninstanz erstellen und konvertierung starten
            ImageConverter converter = new ImageConverter(); 
            converter.convertImage(cmdLine);
        }
        catch(IOException e) {
            Logger.getLogger(ImageConverter.class.getName()).log(Level.SEVERE, null, e);
            System.err.println(e.toString());
            System.exit(ERROR_EXIT_CODE);
        }
    }
    
    /**
     * 
     * Konvertiert Ein- in Ausgabebild entsprechend der Kommandozeilenparameter
     * 
     * @param cmdLine Kommandozeilenparamater 
     * @throws java.io.FileNotFoundException 
     */
    public void convertImage(CmdLine cmdLine) throws FileNotFoundException, IOException {    
        
        // Zeitmessung starten
        long start = System.currentTimeMillis();      
        
        if(!cmdLine.isBaseN()) {
            // Konvertierung ausführen
            ImageOperation op = new ImageOperation();
            op.initialize(cmdLine);
            op.convert(); 
            System.out.println(op.toString());
        } else {
            // BaseN Kodierung ausführen
            DataOperation op = new DataOperation();
            op.initialize(cmdLine);
            op.convert();   
            System.out.println(op.toString());
        }
        
        // Infos auf der Konsole ausgeben
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        
        // Infos Programmablauf ausgeben
        System.out.println("\nKonvertierung abgeschlossen in (ms): " + String.valueOf(timeElapsed));
    }   
}
