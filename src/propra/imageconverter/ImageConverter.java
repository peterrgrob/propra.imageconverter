package propra.imageconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;
import java.util.logging.Level;
import propra.imageconverter.image.*;
import propra.imageconverter.util.*;

/**
 * Einstiegsklasse für ImageConverter 
 * 
 * @author pg
 */
public class ImageConverter {
    
    // Fehlercode
    protected static final int ERROR_EXIT_CODE = 123;
            
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
            converter.Convert(cmdLine);
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
    public void Convert(CmdLine cmdLine) throws FileNotFoundException, IOException {    
        long start = System.currentTimeMillis();

        ImageIO ioController = new ImageIO();
        ioController.setup(cmdLine);
        ioController.beginTransfer();
        ImageHeader inHeader = ioController.getInPlugin().getHeader();
                
        // Infos zum Eingabebild ausgeben
        System.out.print("Bildinfo: " + inHeader.getWidth());
        System.out.print("x" + inHeader.getHeight());
        System.out.print("x" + inHeader.getElementSize());

        ioController.doTransfer();
        ioController.finishTransfer();
        
        // Infos auf der Konsole ausgeben
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.print("\nEingabe Prüfsumme: "+String.format("0x%08X", (int)ioController.getInChecksum()));
        System.out.print("\nAusgabe Prüfsumme: "+String.format("0x%08X", (int)ioController.getOutChecksum()));
        System.out.println("\nKonvertierung abgeschlossen in (ms): " + String.valueOf(timeElapsed));
    }
}
