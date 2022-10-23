package propra.imageconverter.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import propra.imageconverter.util.CmdLine;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class ImageIO implements Validatable {
    
    private ImageModel inModel;
    private ImageModel outModel;
    
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
    public void setupModel(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        RandomAccessFile inStream = new RandomAccessFile(cmd.getOption(CmdLine.Options.INPUT_FILE),"r");
        inModel = createModel(cmd.getOption(CmdLine.Options.INPUT_EXT), 
                            inStream);
        if(inModel == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }

        String outPath = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        File file = new File(outPath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        RandomAccessFile outStream = new RandomAccessFile(file,"rw");
        outModel = createModel(cmd.getOption(CmdLine.Options.OUTPUT_EXT), 
                            outStream);
        if(outModel == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
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
        
        ImageHeader inHeader = inModel.readHeader();
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

        outModel.writeHeader(outModel.getHeader());
    }
    
    /**
     * 
     * @throws java.io.IOException 
     */
    public void transfer() throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }

        DataBuffer block = new DataBuffer(inModel.getBlockSize());
        ColorFormat cFormat = inModel.getHeader().getColorFormat();
        
        inModel.beginImageData();
        outModel.beginImageData();
        
        while(inModel.hasMoreImageData()) {
            inModel.readImageData(block);
            outModel.writeImageData(block,cFormat);
        }
        
        inModel.endImageData();
        outModel.endImageData();
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
        
        if(inModel.isCheckable()) {
            return inModel.getChecksumObj().getValue();
        }
        return 0;
    }
    
    /**
     *
     * @return
     */
    public long getOutputChecksum() {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        if(outModel.isCheckable()) {
            return outModel.getChecksumObj().getValue();
        }
        return 0;
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
