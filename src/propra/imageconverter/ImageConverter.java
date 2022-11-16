package propra.imageconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;
import java.util.logging.Level;

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
            // Zeitmessung starten
            long start = System.currentTimeMillis();   
        
            // Komandozeilenparameter parsen.
            CmdLine cmdLine = new CmdLine(args);
            
            // Ein- und Ausgabedateipfad auf der Konsole ausgeben
            System.out.println("Dateien:");
            System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
            System.out.println(cmdLine.getOption(CmdLine.Options.OUTPUT_FILE));
            
            // Klasseninstanz erstellen und task starten
            ImageConverter converter = new ImageConverter(); 
            if(cmdLine.isBaseTask()) {
                converter.doDataTask(cmdLine);
            } else {
                converter.doImageTask(cmdLine); 
            }
            
            // Zeitmessung beenden
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;

            // Infos Programmablauf ausgeben
            System.out.println("\nKonvertierung abgeschlossen in (ms): " + String.valueOf(timeElapsed));
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
    public void doImageTask(CmdLine cmdLine) throws FileNotFoundException, 
                                                    IOException {      
        // Konvertierung ausführen
        ImageTask op = new ImageTask();
        op.initialize(cmdLine);
        op.convert(); 
        System.out.println(op.toString());
    }   
    
    /**
     * 
     * @param cmdLine
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void doDataTask(CmdLine cmdLine) throws  FileNotFoundException, 
                                                    IOException {    
        // BaseN Kodierung ausführen
        DataTask op = new DataTask();
        op.initialize(cmdLine);
        op.doTask();   
        System.out.println(op.toString());
    }  
}
