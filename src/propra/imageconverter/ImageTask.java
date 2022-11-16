package propra.imageconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.image.ColorFormat;
import propra.imageconverter.image.ImageHeader;
import propra.imageconverter.image.ImageModel;
import propra.imageconverter.image.ImageModelProPra;
import propra.imageconverter.image.ImageModelTGA;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat.Encoding;

/**
 *
 * @author pg
 */
public class ImageTask {
    
    private ImageModel inModel;
    private ImageModel outModel;
    private ColorFormat.Encoding outEncoding = ColorFormat.Encoding.NONE;

    /**
     *
     */
    public ImageTask() {
        
    }
    
     /**
     *
     * @param cmd
     * @throws java.io.FileNotFoundException
     */
    public void initialize(CmdLine cmd) throws FileNotFoundException, IOException {
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
        

        
        // Verzeichnisse und Datei erstellen, falls nötig
        String outPath = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        Path outDirs = Paths.get(outPath);
        Files.createDirectories(outDirs.getParent());
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
     * @throws IOException
     */
    public void convert() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        ImageHeader inHeader = begin();
        process();
        end(); 
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
     * @return
     */
    public boolean isValid() {
        return (    inModel    != null
                &&  outModel   != null);
    }
    
    /**
     *
     * @return
     */
    @Override
    public String toString() {
        String stateString = "Uninitialisiert.";

        if(isValid()) {
            ImageHeader header = inModel.getHeader();
            stateString = "Bildinfo: \n" + header.getWidth();
            stateString = stateString.concat("x" + header.getHeight());
            stateString = stateString.concat("x" + header.getPixelSize());
            stateString = stateString.concat("\nEingabe Prüfsumme: "+String.format("0x%08X", (int)inModel.getChecksum()));
            stateString = stateString.concat("\nAusgabe Prüfsumme: "+String.format("0x%08X", (int)outModel.getChecksum()));
        }
        
        return stateString;
    }
    
    /**
     *
     * @return 
     * @throws IOException
     */
    private ImageHeader begin() throws IOException {
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
    private void end() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Falls nötig Header aktualisieren
        if( outModel.isCheckable()
        ||  outModel.getColorFormat().getEncoding() == Encoding.RLE) {
            outModel.writeHeader(outModel.getHeader());
        }
        
        inModel.close();
        outModel.close();
    }
    
    /**
     * 
     * @throws java.io.IOException 
     */
    private void process() throws IOException {
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
