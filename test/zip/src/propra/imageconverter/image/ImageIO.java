package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import propra.imageconverter.util.CmdLine;

/**
 *
 * @author pg
 */
public class ImageIO {
    protected ImagePlugin inPlugin;
    protected ImagePlugin outPlugin;
    BufferedInputStream inStream;
    BufferedOutputStream outStream;
    
    public ImageIO() {
        
    }
    
    public void create(CmdLine cmd) throws IOException {
        String path = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        
        // Wenn Datei nicht vorhanden, neue Datei erstellen.
        File file = new File(path);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        // FileStream öffnen und ImageWriter Objekt erstellen.
        FileOutputStream fOutput = new FileOutputStream(file);
        outStream = new BufferedOutputStream(fOutput);
        switch(cmd.getOption(CmdLine.Options.OUTPUT_EXT)) {
            case "tga":
                outPlugin = new ImagePluginTGA();
            case "propra":
                outPlugin = new ImagePluginProPra();
        }
        
        FileInputStream fInput = new FileInputStream(cmd.getOption(CmdLine.Options.INPUT_FILE));
        inStream = new BufferedInputStream(fInput);  
        switch(cmd.getOption(CmdLine.Options.INPUT_EXT)) {
            case "tga":
                inPlugin = new ImagePluginTGA();
            case "propra":
                inPlugin = new ImagePluginProPra();
        }
        
        if(isValid()) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
    }
    
    public void Convert(ImageReader reader, ImageWriter writer) throws IOException {
        
        reader.readHeader();
        writer.writeHeader(writer.plugin.headerToBytes(reader.getHeader()));
        
        // Farben in Blöcken einlesen
        int blockSize = reader.available();
        int numBlocks = reader.getPlugin().getHeader().getTotalSize() / blockSize;
        int blockMod = reader.getHeader().getTotalSize() % blockSize;
        ImageBuffer block = null;
        
        for(int i=0;i<numBlocks;i++) {
            block = reader.readContent(blockSize);
            if(block == null) {
                throw new IOException("Lesefehler!");
            }
            
            // Prüfsumme updaten für den aktuellen Block
            if(reader.getPlugin().isCheckable()) {
                reader.getPlugin().check(block.getBuffer().array());
            }
           
            ImageBuffer out = writer.getPlugin().contentToBytes_(block);
            writer.write(out.getBuffer().array());
            
            // Prüfsumme updaten für den aktuellen Block
            if(writer.getPlugin().isCheckable()) {
                writer.getPlugin().check(out.getBuffer().array());
            }
        }
        

    }
    
    public boolean isValid() {
        return (inStream != null 
            &&  outStream != null
            &&  inPlugin != null
            &&  outPlugin != null);
    }
}
