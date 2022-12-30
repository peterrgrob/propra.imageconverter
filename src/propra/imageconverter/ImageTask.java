package propra.imageconverter;

import propra.imageconverter.util.CmdLine;
import java.io.IOException;
import propra.imageconverter.util.CmdLine.Options;
import propra.imageconverter.data.IDataTranscoder.Compression;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.image.*;
import propra.imageconverter.util.PropraException;

/**
 * Klasse implementiert Bildkonvertierungen
 * 
 * @author pg
 */
public class ImageTask implements AutoCloseable {
    
    private CmdLine cmd;
    private ImageResource inImage;
    private ImageResource outImage;
    
    // Kodierung des Ausgabebildes
    private Compression outEncoding = Compression.UNCOMPRESSED;
    
    /**
     * 
     * @param cmd
     * @throws IOException 
     */
    public ImageTask(CmdLine cmd) throws IOException {
        PropraException.assertArgument(cmd);
        this.cmd = cmd;
    }
    
    
    /**
     * Konvertierung ausführen
     * 
     * @throws IOException
     */
    public void run() throws IOException {
        
        String inPath = cmd.getOption(Options.INPUT_FILE);
        String outPath = cmd.getOption(Options.OUTPUT_FILE);
        
        // Ein- und Ausgabedateipfad auf der Konsole ausgeben
        PropraException.printMessage("\n\nBildkonvertierung");
        PropraException.printMessage("Eingabe: " + inPath);
        PropraException.printMessage("Ausgabe: " + outPath);
        
        // Readerobjekt erstellen
        inImage = ImageResource.createResource(inPath,false);
        
        // Verzeichnisse und Datei für die Ausgabe erstellen, falls nötig
        if(outPath == null) {
            throw new IOException("Kein Ausgabepfad gegeben!");            
        }  
        DataUtil.createFileAndDirectory(outPath);
        
        // Ausgabekompression setzen und Konvertierung starten
        outEncoding = cmd.getCompression();
        outImage = inImage.convertTo(outPath, outEncoding);
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
