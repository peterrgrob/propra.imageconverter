package propra.imageconverter;

import java.io.IOException;
import propra.imageconverter.CmdLine.Options;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.checksum.ChecksumPropra;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataFormat.Encoding;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.image.*;
import propra.imageconverter.data.IDataCodec;
import propra.imageconverter.data.IDataListener;

/**
 * Klasse implementiert Bildkonvertierung
 * 
 * @author pg
 */
public class ImageOperation implements IDataListener {
    
    // IO Objekte
    private ImageResource inReader;
    private ImageResource outWriter;
    IDataCodec inCodec;
    IDataCodec outCodec;
    
    // Prüfsummenobjekte
    private Checksum inChecksum;
    private Checksum outChecksum;  
    
    // Kodierung des Ausgabebildes
    private ColorFormat.Encoding outEncoding = ColorFormat.Encoding.NONE;
    
    /**
     * 
     * @param cmd
     * @throws IOException 
     */
    public ImageOperation(CmdLine cmd) throws IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        String inExt = cmd.getExtension(Options.INPUT_FILE);
        String outExt = cmd.getExtension(Options.OUTPUT_FILE);
        
        // Readerobjekt erstellen
        inReader = createImageResource( cmd.getOption(Options.INPUT_FILE), 
                                        inExt);
        if(inReader == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
       
        // Verzeichnisse und Datei für die Ausgabe erstellen, falls nötig
        String outPath = cmd.getOption(Options.OUTPUT_FILE);  
        if(outPath == null) {
            throw new IOException("Kein Ausgabepfad gegeben!");            
        }  
        DataResource.createFileAndDirectory(outPath);
        
        // Ausgabeobjekt erstellen
        outWriter = createImageResource(  cmd.getOption(Options.OUTPUT_FILE),
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
            outChecksum = new ChecksumPropra();
        }
    }
    
    
    /**
     * Konvertierung ausführen
     * 
     * @throws IOException
     */
    public void run() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Bildkopf einlesen
        ImageMeta inHeader = new ImageMeta(inReader.readHeader());
        
        inCodec = createImageCodec(  inHeader.colorFormat().encoding(), 
                                    inReader, 
                                    inChecksum);
        outCodec = createImageCodec( outEncoding,
                                    outWriter, 
                                    outChecksum);
        
        // Bildkompression setzen und Bildkopf in Ausgabedatei schreiben
        inHeader.colorFormat().encoding(outEncoding);
        outWriter.writeHeader(inHeader);
       
        // Bilddaten verarbeiten
        DataBlock dataBlock = new DataBlock();
        inCodec.begin(DataFormat.Operation.READ);
        outCodec.begin(DataFormat.Operation.WRITE);
        inCodec.decode(dataBlock, outCodec);
        inCodec.end();
        outCodec.end();
        
        // Prüfsumme prüfen
        if(inChecksum != null) {
            if(inChecksum.getValue() != inReader.getHeader().checksum()) {
                throw new IOException(  "Prüfsumme " 
                                        + String.format("0x%08X", (int)inChecksum.getValue()) 
                                        + " ungleich " 
                                        + String.format("0x%08X", (int)inReader.getHeader().checksum()));
            }
        }
        
        // Falls nötig Header aktualisieren
        if( outChecksum != null
        ||  outWriter.getHeader().colorFormat().encoding() == Encoding.RLE) {
            outWriter.writeHeader(outWriter.getHeader());
        }
        
        inReader.close();
        outWriter.close();
    }
    
    /**
     * 
     * @param caller
     * @param block
     * @throws IOException 
     */
    @Override
    public void onData( Event event,
                        IDataCodec caller, 
                        DataBlock block) throws IOException {
        if(caller == inCodec
        && block != null
        && outCodec != null) {
            outCodec.encode(block);
        }
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
            ImageMeta header = inReader.getHeader();
            stateString = "\nBildinfo: " + header.width();
            stateString = stateString.concat("x" + header.height());
            stateString = stateString.concat("x" + header.pixelSize());
            stateString = stateString.concat("\nKompression: " + header.colorFormat().encoding().toString());
            stateString = stateString.concat(" --> " + outEncoding.toString());            
            
            if(inChecksum != null) {
                stateString = stateString.concat("\nEingabe Prüfsumme (Ok): "+String.format("0x%08X", (int)inChecksum.getValue()));
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
    private static ImageResource createImageResource(String path, String ext) throws IOException {
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
    private static ImageCodecRaw createImageCodec(  DataFormat.Encoding format,
                                                    ImageResource resource, 
                                                    Checksum checksum) {
        switch(format) {
            case NONE -> {
                return new ImageCodecRaw(resource, checksum);
            }
            case RLE -> {
                return new ImageCodecRLE(resource, checksum);
            }
        }
        return null;
    }
}
