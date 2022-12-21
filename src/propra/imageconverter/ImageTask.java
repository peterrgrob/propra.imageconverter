package propra.imageconverter;

import propra.imageconverter.util.CmdLine;
import java.io.IOException;
import propra.imageconverter.util.CmdLine.Options;
import propra.imageconverter.data.DataResource;
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
    private ColorFormat.Encoding outEncoding = ColorFormat.Encoding.NONE;
    
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
        DataResource.createFileAndDirectory(outPath);
        
        // Ausgabekompression setzen und Konvertierung starten
        outEncoding = cmd.getColorEncoding();
        outImage = inImage.transcode(outPath, outExt, outEncoding);
        
        // Prüfsumme prüfen
        if(inImage.getChecksum() != null) {
            if(inImage.getChecksum().getValue() != inImage.getHeader().checksum()) {
                throw new IOException(  "Prüfsumme " 
                                        + String.format("0x%08X", (int)inImage.getChecksum().getValue()) 
                                        + " ungleich " 
                                        + String.format("0x%08X", (int)inImage.getHeader().checksum()));
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
            ImageHeader header = inImage.getHeader();
            stateString = "\nBildinfo: " + header.width();
            stateString = stateString.concat("x" + header.height());
            stateString = stateString.concat("x" + header.pixelSize());
            stateString = stateString.concat("\nKompression: " + header.colorFormat().encoding().toString());
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
