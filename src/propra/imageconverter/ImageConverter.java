package propra.imageconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.*;
import java.util.logging.Level;
import propra.imageconverter.image.*;
import propra.imageconverter.util.*;

/**
 * Einstiegsklasse für ImageConverter 
 * 
 * @author pg
 */
public class ImageConverter implements Validatable {
    
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
        
        initializeOperation(cmdLine);
        
        ImageHeader inHeader = beginOperation();
        doOperation();
        endOperation();       
        
        // Infos auf der Konsole ausgeben
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        
        // Infos zum Eingabebild ausgeben
        System.out.print("Bildinfo: " + inHeader.getWidth());
        System.out.print("x" + inHeader.getHeight());
        System.out.print("x" + inHeader.getPixelSize());
        System.out.print("\nEingabe Prüfsumme: "+String.format("0x%08X", (int)inModel.getChecksum()));
        System.out.print("\nAusgabe Prüfsumme: "+String.format("0x%08X", (int)outModel.getChecksum()));
        System.out.println("\nKonvertierung abgeschlossen in (ms): " + String.valueOf(timeElapsed));
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return (    inModel    != null
                &&  outModel   != null);
    }
    
        /**
     *
     * @param cmd
     * @throws java.io.FileNotFoundException
     */
    public void initializeOperation(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        // Eingabeobjekt erstellen
        RandomAccessFile inStream = new RandomAccessFile(cmd.getOption(CmdLine.Options.INPUT_FILE),"r");
        inModel = createImageModel(cmd.getOptionExtension(CmdLine.Options.INPUT_FILE), 
                            inStream);
        if(inModel == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }

        String outPath = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        File file = new File(outPath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        // Ausgabeobjekt erstellen
        RandomAccessFile outStream = new RandomAccessFile(file,"rw");
        outModel = createImageModel(cmd.getOptionExtension(CmdLine.Options.OUTPUT_FILE), 
                            outStream);
        if(outModel == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
        
        // Ausgabekompression setzen
        outEncoding = cmd.getColorEncoding();
    }
    
    /**
     *
     * @return 
     * @throws IOException
     */
    public ImageHeader beginOperation() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Bildkopf einlesen
        ImageHeader inHeader = inModel.readHeader();
        
        // Bildkompression setzen und Bildkopf in Ausgabedatei schreiben
        inHeader.getColorFormat().setEncoding(outEncoding);
        outModel.writeHeader(inHeader);
        
        return inHeader;
    }
    
    /**
     *
     * @throws IOException
     */
    public void endOperation() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Falls nötig Prüfsumme in den Bildkopf schreiben
        if(outModel.isCheckable()) {
            outModel.writeHeader(outModel.getHeader());
        }
        
        inModel.close();
        outModel.close();
    }
    
    /**
     * 
     * @throws java.io.IOException 
     */
    public void doOperation() throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }

        // Datenblock für blockweise Übertragung erstellen
        DataBuffer block = new DataBuffer(inModel.getBlockSize());
        ColorFormat inFormat = inModel.getColorFormat();
        
        // Blockweise Übertragung starten
        inModel.beginImageBlocks();
        outModel.beginImageBlocks();
        
        // Blöcke übertragen 
        while(inModel.hasMoreImageData()) {
            inModel.readImageBlock(block);
            outModel.writeImageBlock(block,inFormat);
        }
        
        // blockweise Übertragung beenden     
        inModel.endImageBlocks();
        outModel.endImageBlocks();
        
        // Prüfsumme prüfen
        isChecksumValid();
    }
    
    /**
     *
     * @throws java.io.IOException
     */
    public void isChecksumValid() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        if(inModel.isCheckable()) {
            if(inModel.getChecksumObj().getValue() 
            != inModel.getHeader().getChecksum()) {
                throw new IOException("Eingabe Prüfsummenfehler!");
            }
        }
        if(outModel.isCheckable()
        && inModel.isCheckable()) {
            if(inModel.getChecksumObj().getValue() 
            != inModel.getHeader().getChecksum()) {
                throw new IOException("Ausgabe Prüfsummenfehler!");
            }
        }
    }
    
    /**
     *
     * @param ext
     * @param streamLen
     * @return
     */
    private ImageModel createImageModel(String ext, RandomAccessFile stream) {
        switch(ext) {
            case "tga" -> {
                return new ImageModelTGA(stream);
            }
            case "propra" -> {
                return new ImageModelProPra(stream);
            }
        }
        return null;
    }
}
