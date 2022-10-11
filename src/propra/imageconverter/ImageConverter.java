package propra.imageconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import propra.imageconverter.image.*;
import propra.imageconverter.util.*;

/**
 *
 * @author pg
 */
public class ImageConverter {
    /**
     * 
     */
    protected static final int ERROR_EXIT_CODE = 123;
    public static final Messages MSG = new MessagesSimple();
            
    /** 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Komandozeilenparamter parsen.
            CmdLine cmdLine = new CmdLine(args);
            
            // Ein- und Ausgabedateipfad ausgeben
            System.out.println("Dateien:");
            System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
            System.out.println(cmdLine.getOption(CmdLine.Options.OUTPUT_FILE));
            
            // Klasseninstanz erstellen und Dateien konvertieren
            ImageConverter converter = new ImageConverter(); 
            converter.Convert(cmdLine);
        }
        catch(FileNotFoundException e) {
            System.exit(ERROR_EXIT_CODE);
        }
    }
    
    /**
     * 
     * @param cmdLine 
     * @throws java.io.FileNotFoundException 
     */
    public void Convert(CmdLine cmdLine) throws FileNotFoundException {    
        try {
            long start = System.currentTimeMillis();
            
            // Reader und Writer erstellen basierend auf Dateierweiterungen.
            ImageReader reader = createPReader(cmdLine);
            ImageWriter writer = createPWriter(cmdLine);
            
            // Eingabebild laden.
            ImageBuffer src = reader.readImage();
            
            // Infos zum Eingabebild ausgeben.
            System.out.print("Bildinfo: "+src.getHeader().getWidth());
            System.out.print("x" + src.getHeader().getHeight());
            System.out.print("x" + src.getHeader().getElementSize());
           
            // Bild konvertieren und speichern.
            ImageBuffer dst = writer.writeImage(src);
            
            System.out.print("\nPrüfsumme: "+String.format("0x%08X", (int)dst.getHeader().getChecksum()));
            
            // Infos ausgeben.
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("\nKonvertierung abgeschlossen in (ms): " + String.valueOf(timeElapsed));
            
        } catch (IOException ex) {
            Logger.getLogger(ImageConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @param cmd
     * @return 
     * @throws java.io.FileNotFoundException 
     */
    public ImageReader createPReader(CmdLine cmd) throws FileNotFoundException, IOException {
        // FileStream öffnen und ImageReader Objekt erstellen.
        FileInputStream fInput = new FileInputStream(cmd.getOption(CmdLine.Options.INPUT_FILE));
        switch(cmd.getOption(CmdLine.Options.INPUT_EXT)) {
            case "tga":
                return new ImageReader(fInput, new ImagePluginTGA());
            case "propra":
                return new ImageReader(fInput, new ImagePluginProPra());
        }
        throw new IOException("Nicht unterstütztes Bildformat.");
    }
    
            /**
     * 
     * @param cmd
     * @return 
     * @throws java.io.FileNotFoundException 
     */
    public ImageWriter createPWriter(CmdLine cmd) throws FileNotFoundException, IOException {
        String path = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        
        // Wenn Datei nicht vorhanden, neue Datei erstellen.
        File file = new File(path);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        // FileStream öffnen und ImageWriter Objekt erstellen.
        FileOutputStream fOutput = new FileOutputStream(file);
        switch(cmd.getOption(CmdLine.Options.OUTPUT_EXT)) {
            case "tga":
                return new ImageWriter(fOutput, new ImagePluginTGA());
            case "propra":
                return new ImageWriter(fOutput, new ImagePluginProPra());
        }
        throw new IOException("Nicht unterstütztes Bildformat.");
    }
}
