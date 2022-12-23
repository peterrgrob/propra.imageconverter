package propra.imageconverter;

import propra.imageconverter.util.CmdLine;
import java.io.IOException;
import propra.imageconverter.util.CmdLine.Options;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.DataResource.Compression;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.image.*;

/**
 * Klasse implementiert Bildkonvertierung
 * 
 * @author pg
 */
public class ImageTask implements AutoCloseable {
    
    private CmdLine cmd;
    private ImageResource inImage;
    private ImageResource outImage;
    
    // Kodierung des Ausgabebildes
    private Compression outEncoding = Compression.NONE;
    
    /**
     * 
     * @param cmd
     * @throws IOException 
     */
    public ImageTask(CmdLine cmd) throws IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        this.cmd = cmd;
    }
    
    
    /**
     * Konvertierung ausführen
     * 
     * @throws IOException
     */
    public void run() throws IOException {
        
        String inExt = cmd.getExtension(Options.INPUT_FILE);
        String outExt = cmd.getExtension(Options.OUTPUT_FILE);
        
        // Readerobjekt erstellen
        inImage = ImageResource.createResource( cmd.getOption(Options.INPUT_FILE), 
                                                    inExt,
                                                    false);
        if(inImage == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
       
        // Verzeichnisse und Datei für die Ausgabe erstellen, falls nötig
        String outPath = cmd.getOption(Options.OUTPUT_FILE);  
        if(outPath == null) {
            throw new IOException("Kein Ausgabepfad gegeben!");            
        }  
        DataUtil.createFileAndDirectory(outPath);
        
        // Ausgabekompression setzen und Konvertierung starten
        outEncoding = cmd.getCompression();
        outImage = inImage.convertTo(outPath, outExt, outEncoding);
        
        // Prüfsumme prüfen
        if(inImage.getChecksum() != null) {
            if(inImage.getChecksum().getValue() != inImage.getAttributes().getChecksum()) {
                throw new IOException(  "Prüfsumme " 
                                        + String.format("0x%08X", (int)inImage.getChecksum().getValue()) 
                                        + " ungleich " 
                                        + String.format("0x%08X", (int)inImage.getAttributes().getChecksum()));
            }
        }
    }
    
    /**
     * @return
     */
    @Override
    public String toString() {
        String stateString = "";

        if(inImage != null) {
            ImageAttributes header = inImage.getAttributes();
            stateString = "\nBildinfo: " + header.getWidth();
            stateString = stateString.concat("x" + header.getHeight());
            stateString = stateString.concat("x" + Color.PIXEL_SIZE);
            stateString = stateString.concat("\nKompression: " + header.getCompression().toString());
            stateString = stateString.concat(" --> " + outEncoding.toString());            
            
            if(inImage.getChecksum() != null) {
                stateString = stateString.concat(   "\nEingabe Prüfsumme (Ok): "+String.format("0x%08X", 
                                                    (int)inImage.getChecksum().getValue()));
            }
            
            if(outImage.getChecksum() != null) {
                stateString = stateString.concat(   "\nAusgabe Prüfsumme: "+String.format("0x%08X", 
                                                    (int)outImage.getChecksum().getValue()));
            }
        }
        
        return stateString;
    }

    /**
     * Schließt geöffnete Resourcen, wird automatisch bei Verwendung mit 
     * try-with-resources aufgerufen
     */
    @Override
    public void close() throws Exception {
         if(inImage != null) {
            inImage.close();
        }
        if(outImage != null) {
            outImage.close();            
        }
    }
}
