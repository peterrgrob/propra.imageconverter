package propra.imageconverter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.data.DataFormat.Encoding;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.image.*;

/**
 * Klasse implementiert Bildkonvertierung
 * 
 * @author pg
 */
public class TaskImage {
    
    // IO Objekte
    private ImageReader inReader;
    private ImageWriter outWriter;
    
    // Kodierung des Ausgbaebildes
    private ColorFormat.Encoding outEncoding = ColorFormat.Encoding.NONE;
    
    /**
     * 
     * @param cmd
     * @throws IOException 
     */
    public TaskImage(CmdLine cmd) throws IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        // Readerobjekt erstellen
        inReader = createImageReader(   cmd.getOption(CmdLine.Options.INPUT_FILE), 
                                        cmd.getExtension(CmdLine.Options.INPUT_FILE));
        if(inReader == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
       
        // Verzeichnisse und Datei für die Ausgabe erstellen, falls nötig
        String outPath = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        if(outPath == null) {
            throw new IOException("Kein Ausgabepfad gegeben!");            
        }
        
        Path outDirs = Paths.get(outPath);
        Files.createDirectories(outDirs.getParent());
        File file = new File(outPath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        // Ausgabeobjekt erstellen
        outWriter = createImageWriter(  cmd.getOption(CmdLine.Options.OUTPUT_FILE),
                                        cmd.getExtension(CmdLine.Options.OUTPUT_FILE));
        if(outWriter == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
        
        // Ausgabekompression setzen
        outEncoding = cmd.getColorEncoding();
    }
    
    
    /**
     * Konvertierung ausführen
     * 
     * @throws IOException
     */
    public void convert() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        begin();
            process();
        end(); 
    }
    
    /**
     * Bereitet blockweise Konvertierung der Bilddaten vor
     *
     * @throws IOException
     */
    private void begin() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Bildkopf einlesen
        ImageHeader inHeader = new ImageHeader(inReader.readHeader());
        
        // Bildkompression setzen und Bildkopf in Ausgabedatei schreiben
        inHeader.colorFormat().encoding(outEncoding);
        outWriter.writeHeader(inHeader);
    }
    
    /**
     * Blockweise Konvertierung der Bilddaten
     * 
     * @throws java.io.IOException 
     */
    private void process() throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }

        inReader.readImage(outWriter);
    }    
    
    /**
     * Blockweise Konvertierung der Bilddaten abschließen 
     * 
     * @throws IOException
     */
    private void end() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Prüfsumme prüfen
        isChecksumValid();
        
        // Falls nötig Header aktualisieren
        if( outWriter.getChecksumObj() != null
        ||  outWriter.getHeader().colorFormat().encoding() == Encoding.RLE) {
            outWriter.writeHeader(outWriter.getHeader());
        }
        
        inReader.close();
        outWriter.close();
    }
    
    /**
     * @throws java.io.IOException
     */
    public void isChecksumValid() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        if(!inReader.validChecksum()) {
            throw new IOException("Eingabe Prüfsummenfehler!");
        }
    }
    
    /**
     * @return
     */
    public boolean isValid() {
        return (    inReader    != null
                &&  outWriter   != null);
    }
    
    /**
     * @return
     */
    @Override
    public String toString() {
        String stateString = "";

        if(isValid()) {
            ImageHeader header = inReader.getHeader();
            stateString = "Bildinfo: \n" + header.width();
            stateString = stateString.concat("x" + header.height());
            stateString = stateString.concat("x" + header.pixelSize());
            stateString = stateString.concat("\nEingabe Prüfsumme: "+String.format("0x%08X", (int)inReader.getChecksum()));
            stateString = stateString.concat("\nAusgabe Prüfsumme: "+String.format("0x%08X", (int)outWriter.getChecksum()));
        }
        
        return stateString;
    }
    
    /**
     *  Erstellt Ausgabe Objekt
     * 
     * @param ext
     * @param streamLen
     * @return
     */
    private static ImageWriter createImageWriter(String path, String ext) throws IOException {
        switch(ext) {
            case "tga" -> {
                return new ImageWriterTGA(path, IOMode.BINARY);
            }
            case "propra" -> {
                return new ImageWriterProPra(path, IOMode.BINARY);
            }

        }
        return null;
    }
    
    /**
     * Erstellt Eingabe Objekt
     * 
     * @param ext
     * @param streamLen
     * @return
     */
    private static ImageReader createImageReader(String path, String ext) throws IOException {
        switch(ext) {
            case "tga" -> {
                return new ImageReaderTGA(path, IOMode.BINARY);
            }
            case "propra" -> {
                return new ImageReaderProPra(path, IOMode.BINARY);
            }

        }
        return null;
    }
}
