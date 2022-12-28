package propra.imageconverter;

import propra.imageconverter.util.CmdLine.Options;
import propra.imageconverter.util.CmdLine;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;
import java.util.logging.Level;
import propra.imageconverter.util.PropraException;


/*
 *  Einstiegsklasse für ImageConverter 
 * 
 *  Änderungen zu KE2
 *  -   Meine Abgabe für KE2 hat die Verarbeitung großer Dateien in Bezug auf RLE nicht 
 *      unterstützt und war teilweise aus Zeitgründen nicht ganz optimal umgesetzt. 
 *      Deshalb habe ich jetzt nochmal grundlegende Änderungen bei der Klassenstruktur 
 *      vorgenommen und das ganze etwas streamorientierter implementiert. 
 *
 *  -   Farbkonvertierung ist aus Performancegründen jetzt weniger allgemein 
 *      implementiert mit Methoden-Referenzen je nach Kombination.
 *
 *  -   Mit Hilfe der BitStreams der Huffman Kompression konnte ich die BaseN 
 *      Kodierung deutlich vereinfachen.
 *      
 *  -   Ich habe versucht, wie einige Reviewer angemerkt haben, die Komplexität und 
 *      nicht genutzte Teile der Klassenstruktur, die im Laufe des Projekts entstanden 
 *      sind, zu reduzieren. 
 *      
 *      Damit es besser verständlich ist hier eine kleine Übersicht:
 * 
 *      IDataResource <-> IDataCodec(Decoder) -> Filter -> IDataCodec(Encoder) <-> IDataResourec 
 */
public class ImageConverter {
    
    // Kommandozeilenoptionen
    private final CmdLine cmdLine;
    
    /**
     * 
     */
    ImageConverter(CmdLine cmdLine) {
        this.cmdLine = cmdLine;
    }
    
    /** 
     * Programmeinstieg
     */
    public static void main(String[] args) {
        try {
            // Zeitmessung starten
            long start = System.currentTimeMillis();   
        
            if(args.length == 0) {
                PropraException.printErrorAndQuit("Keine Parameter übergeben!", null);
            }
            
            // Klasseninstanz erstellen und gewünschten Task starten
            ImageConverter converter = new ImageConverter(new CmdLine(args)); 
            converter.run();
        
            // Zeitmessung beenden
            long timeElapsed = System.currentTimeMillis() - start;

            // Infos Programmablauf ausgeben
            PropraException.printMessage("Operation abgeschlossen in (ms): " + String.valueOf(timeElapsed));
            
        } catch(FileNotFoundException e) {
            PropraException.printErrorAndQuit("Datei nicht gefunden!", e);
        }
        catch(IOException e) {
            PropraException.printErrorAndQuit("I/O Fehler:\n", e);
        } 
        catch(Exception e) {
            Logger.getLogger(ImageConverter.class.getName()).log(Level.SEVERE, null, e);
            PropraException.printErrorAndQuit("Unbehandelter Fehler:", e);
        }
    }
    
    /**
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void run() throws    FileNotFoundException,
                                IOException,
                                Exception{
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
                                        IOException,      
                                        Exception {      
        // Ein- und Ausgabedateipfad auf der Konsole ausgeben
        PropraException.printMessage("Dateien:");
        PropraException.printMessage(cmdLine.getOption(Options.INPUT_FILE));
        PropraException.printMessage(cmdLine.getOption(Options.OUTPUT_FILE));
        
        try(ImageTask op = new ImageTask(cmdLine)) {
            op.run();
            PropraException.printMessage(op.toString());
        }
    }   
    
    /**
     * BaseN Kodierung entsprechend der Kommandozeilenparameter
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void doBaseNTask() throws    FileNotFoundException, 
                                        IOException,    
                                        Exception {    
        // Eingabedateipfad auf der Konsole ausgeben
        PropraException.printMessage("Eingabedatei:");
        PropraException.printMessage(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
        
        // BaseN Kodierung ausführen
        try(BaseNTask op = new BaseNTask(cmdLine)) {   
            op.run();  
            PropraException.printMessage(op.toString());
        }
    } 
}
