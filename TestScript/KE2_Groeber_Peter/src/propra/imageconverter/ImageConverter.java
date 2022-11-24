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
        
            if(args.length == 0) {
                System.out.println("Keine Parameter übergeben!");
                System.exit(ERROR_EXIT_CODE);
            }
            
            // Komandozeilenparameter parsen.
            CmdLine cmdLine = new CmdLine(args);
            
            // Klasseninstanz erstellen und gewünschten Task starten
            ImageConverter converter = new ImageConverter(); 
            if(cmdLine.isBaseTask()) {
                converter.doBaseNTask(cmdLine);
            } else {
                converter.doImageTask(cmdLine); 
            }
        
            // Zeitmessung beenden
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;

            // Infos Programmablauf ausgeben
            System.out.println("Task abgeschlossen in (ms): " + String.valueOf(timeElapsed));
        } catch(FileNotFoundException e) {
            System.out.println("Datei nicht gefunden!");
            System.exit(ERROR_EXIT_CODE);
        }
        catch(IOException e) {
            System.out.println("Ungültige Parameter!");
            System.exit(ERROR_EXIT_CODE);
        } 
        catch(Exception e) {
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
        // Ein- und Ausgabedateipfad auf der Konsole ausgeben
        System.out.println("Dateien:");
        System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
        System.out.println(cmdLine.getOption(CmdLine.Options.OUTPUT_FILE));
        
        // Task ausführen
        TaskImage op = new TaskImage(cmdLine);
        op.convert();
        
        System.out.println(op.toString());
    }   
    
    /**
     * BaseN Kodierung entsprechend der Kommandozeilenparameter
     * 
     * @param cmdLine
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void doBaseNTask(CmdLine cmdLine) throws FileNotFoundException, 
                                                    IOException {    
        // Eingabedateipfad auf der Konsole ausgeben
        System.out.println("Dateien:");
        System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
        
        // BaseN Kodierung ausführen
        TaskBaseN op = new TaskBaseN(cmdLine);
        op.doTask();   
        
        // Info ausgeben
        System.out.println(op.toString());
    } 
}
