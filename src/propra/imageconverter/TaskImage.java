package propra.imageconverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.checksum.ChecksumPropra;
import propra.imageconverter.data.DataController;
import propra.imageconverter.data.DataFormat;
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
    private ImageResource inReader;
    private ImageResource outWriter;
    private Checksum inChecksum;
    private Checksum outChecksum;  
    
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
        
        String inExt = cmd.getExtension(CmdLine.Options.INPUT_FILE);
        String outExt = cmd.getExtension(CmdLine.Options.OUTPUT_FILE);
        
        // Readerobjekt erstellen
        inReader = createImageReader(   cmd.getOption(CmdLine.Options.INPUT_FILE), 
                                        inExt);
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
                                        outExt);
        if(outWriter == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
        
        // Ausgabekompression setzen
        outEncoding = cmd.getColorEncoding();
        
        if(inExt.compareTo("propra") == 0) {
            inChecksum = new ChecksumPropra();
        }
        
        if(outExt.compareTo("propra") == 0) {
            inChecksum = new ChecksumPropra();
        }
    }
    
    
    /**
     * Konvertierung ausführen
     * 
     * @throws IOException
     */
    public void doTask() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Bildkopf einlesen
        ImageHeader inHeader = new ImageHeader(inReader.readHeader());
        
        ImageCodec inCodec = createImageCodec(  inHeader.colorFormat().encoding(), 
                                                inReader, 
                                                inChecksum);
        ImageCodec outCodec = createImageCodec( outEncoding,
                                                outWriter, 
                                                outChecksum);
        DataController controller = new DataController(inCodec, outCodec);
        
        // Bildkompression setzen und Bildkopf in Ausgabedatei schreiben
        inHeader.colorFormat().encoding(outEncoding);
        outWriter.writeHeader(inHeader);
       
        // Konvertieren
        controller.process();
        
        // Prüfsumme prüfen
        isChecksumValid();
        
        // Falls nötig Header aktualisieren
        if( outChecksum != null
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
            
            if(inChecksum != null) {
                stateString = stateString.concat("\nEingabe Prüfsumme OK: "+String.format("0x%08X", (int)inChecksum.getValue()));
            }
            
            if(outChecksum != null) {
                stateString = stateString.concat("\nAusgabe Prüfsumme: "+String.format("0x%08X", (int)outChecksum.getValue()));
            }
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
    private static ImageResource createImageWriter(String path, String ext) throws IOException {
        switch(ext) {
            case "tga" -> {
                return new ImageResourceTGA(path, IOMode.BINARY);
            }
            case "propra" -> {
                return new ImageResourceProPra(path, IOMode.BINARY);
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
    private static ImageResource createImageReader(String path, String ext) throws IOException {
        switch(ext) {
            case "tga" -> {
                return new ImageResourceTGA(path, IOMode.BINARY);
            }
            case "propra" -> {
                return new ImageResourceProPra(path, IOMode.BINARY);
            }

        }
        return null;
    }
    
    /**
     * 
     * @param format 
     */
    private static ImageCodec createImageCodec( DataFormat.Encoding format,
                                                ImageResource resource, 
                                                Checksum checksum) {
        switch(format) {
            case NONE -> {
                return new ImageCodec(resource, checksum);
            }
            case RLE -> {
                return new ImageCodecRLE(resource, checksum);
            }
        }
        return null;
    }
}
