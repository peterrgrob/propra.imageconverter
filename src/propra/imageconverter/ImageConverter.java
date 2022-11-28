package propra.imageconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;
import java.util.logging.Level;
import propra.imageconverter.CmdLine.Options;

/*
 *  Einstiegsklasse für ImageConverter 
 * 
 *  Änderungen zu KE2
 *  - Änderung der Klassenstruktur im Hinblick auf Verarbeitung großer Dateien.
 *  - JavaDoc entfernt, da ich nicht genug Zeit habe es konsequent zu nutzen.
 * 
 */
public class ImageConverter {
    
    // Kommandozeilenoptionen
    private final CmdLine cmdLine;
    
    // Fehlercode
    private static final int ERROR_EXIT_CODE = 123;
    
    /**
     * 
     */
    ImageConverter(CmdLine cmdLine) {
        this.cmdLine = cmdLine;
    }
    
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
                printErrorAndQuit("Keine Parameter übergeben!", null);
            }
            
            // Klasseninstanz erstellen und gewünschten Task starten
            ImageConverter converter = new ImageConverter(new CmdLine(args)); 
            converter.run();
        
            // Zeitmessung beenden
            long timeElapsed = System.currentTimeMillis() - start;

            // Infos Programmablauf ausgeben
            printMessage("Operation abgeschlossen in (ms): " + String.valueOf(timeElapsed));
            
        } catch(FileNotFoundException e) {
            printErrorAndQuit("Datei nicht gefunden!", e);
        }
        catch(IOException e) {
            printErrorAndQuit("I/O Fehler:\n", e);
        } 
        catch(Exception e) {
            Logger.getLogger(ImageConverter.class.getName()).log(Level.SEVERE, null, e);
            printErrorAndQuit("Unbehandelter Fehler:", e);
        }
    }
    
    /**
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void run() throws    FileNotFoundException,
                                IOException{
        if(cmdLine.isBaseTask()) {
            doBaseNTask();
        } else {
            doImageTask();
        }
    }
    
    /**
     * 
     * Konvertiert Ein- in Ausgabebild entsprechend der Kommandozeilenparameter
     * @throws java.io.FileNotFoundException 
     */
    public void doImageTask() throws    FileNotFoundException, 
                                        IOException {      
        // Ein- und Ausgabedateipfad auf der Konsole ausgeben
        printMessage("Dateien:");
        printMessage(cmdLine.getOption(Options.INPUT_FILE));
        printMessage(cmdLine.getOption(Options.OUTPUT_FILE));
        
        ImageOperation op = new ImageOperation(cmdLine);
        op.run();
        
        printMessage(op.toString());
    }   
    
    /**
     * BaseN Kodierung entsprechend der Kommandozeilenparameter
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void doBaseNTask() throws    FileNotFoundException, 
                                        IOException {    
        // Eingabedateipfad auf der Konsole ausgeben
        printMessage("Eingabedatei:");
        printMessage(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
        
        // BaseN Kodierung ausführen
        BaseNOperation op = new BaseNOperation(cmdLine);
        op.run();   
        
        // Info ausgeben
        printMessage(op.toString());
    } 
    
    /**
     * 
     * @param msg 
     * @param e 
     */
    public static void printMessage(String msg) {
        System.out.println(msg); 
    }
    
    /**
     * 
     * @param msg 
     */
    public static void printErrorAndQuit(String msg, Exception e) {
        String s = msg;
        if(e != null) {
            if(e.getMessage() != null) {
                s = s.concat(e.getMessage());
            }
        }
        System.err.println(s);
        System.exit(ERROR_EXIT_CODE);
    }
}
