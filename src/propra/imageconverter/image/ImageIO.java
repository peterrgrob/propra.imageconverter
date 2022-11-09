package propra.imageconverter.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import propra.imageconverter.util.CmdLine;
import propra.imageconverter.util.CmdLine.Options;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class ImageIO implements Validatable {
   
    private ImageModel inModel;
    private ImageModel outModel;
    private ColorFormat.Encoding outEncoding = ColorFormat.Encoding.NONE;
    
    /**
     *
     */
    public ImageIO() {
    }
    
    /**
     *
     * @param inPlugin
     * @param outPlugin
     */
    public ImageIO( ImageModel inPlugin,
                    ImageModel outPlugin) {
        wrapModel(inPlugin,outPlugin);
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
     * @param inPlugin
     * @param outPlugin
     */
    public void wrapModel( ImageModel inPlugin,
                           ImageModel outPlugin) {
        if( inPlugin == null
        ||  outPlugin == null) {
            throw new IllegalArgumentException();
        }
        
        this.inModel = inPlugin;
        this.outModel = outPlugin;
    }
    
    /**
     *
     * @param cmd
     * @throws java.io.FileNotFoundException
     */
    public void setupFromCmdline(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        // Eingabeobjekt erstellen
        RandomAccessFile inStream = new RandomAccessFile(cmd.getOption(Options.INPUT_FILE),"r");
        inModel = createModel(cmd.getOptionExtension(Options.INPUT_FILE), 
                            inStream);
        if(inModel == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }

        String outPath = cmd.getOption(Options.OUTPUT_FILE);  
        File file = new File(outPath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        // Ausgabeobjekt erstellen
        RandomAccessFile outStream = new RandomAccessFile(file,"rw");
        outModel = createModel(cmd.getOptionExtension(Options.OUTPUT_FILE), 
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
    public ImageHeader beginTransfer() throws IOException {
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
    public void endTransfer() throws IOException {
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
    public void transfer() throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }

        // Datenblock für blockweise Übertragung erstellen
        DataBuffer block = new DataBuffer(inModel.getBlockSize());
        ColorFormat inFormat = inModel.getColorFormat();
        
        // blockweise Übertragung starten
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
     * @return
     */
    public long getInputChecksum() {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        return inModel.getChecksum();
    }
    
    /**
     *
     * @return
     */
    public long getOutputChecksum() {
        if(!isValid()) {
            throw new IllegalStateException();
        }
      return outModel.getChecksum();
    }

    /**
     *
     * @return
     */
    public ImageModel getInputModel() {
        return inModel;
    }

    /**
     *
     * @return
     */
    public ImageModel getOutputModel() {
        return outModel;
    }
    
    /**
     *
     * @param ext
     * @param streamLen
     * @return
     */
    private static ImageModel createModel(String ext, RandomAccessFile stream) {
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
