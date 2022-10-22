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
    
    private ImageModule inPlugin;
    private ImageModule outPlugin;
    
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
    public ImageIO( ImageModule inPlugin,
                    ImageModule outPlugin) {
        wrapPlugins(inPlugin,outPlugin);
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return (    inPlugin    != null
                &&  outPlugin   != null);
    }
    
    /**
     *
     * @param inPlugin
     * @param outPlugin
     */
    public void wrapPlugins(ImageModule inPlugin,
                            ImageModule outPlugin) {
        if( inPlugin == null
        ||  outPlugin == null) {
            throw new IllegalArgumentException();
        }
        
        this.inPlugin = inPlugin;
        this.outPlugin = outPlugin;
    }
    
    /**
     *
     * @param cmd
     * @throws java.io.FileNotFoundException
     */
    public void setupPlugins(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        RandomAccessFile inStream = new RandomAccessFile(cmd.getOption(CmdLine.Options.INPUT_FILE),"r");
        inPlugin = createModule(cmd.getOption(CmdLine.Options.INPUT_EXT), 
                            inStream);
        if(inPlugin == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }

        String outPath = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        File file = new File(outPath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        RandomAccessFile outStream = new RandomAccessFile(file,"rw");
        outPlugin = createModule(cmd.getOption(CmdLine.Options.OUTPUT_EXT), 
                            outStream);
        if(outPlugin == null) {
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
        
        ImageHeader inHeader = inPlugin.readHeader();
        outPlugin.writeHeader(inHeader);
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

        outPlugin.writeHeader(outPlugin.getHeader());
    }
    
    /**
     * 
     * @throws java.io.IOException 
     */
    public void transfer() throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }

        DataBuffer block = new DataBuffer(inPlugin.getBlockSize());
        ColorFormat cFormat = inPlugin.getHeader().getColorType();
        
        inPlugin.beginImageData();
        outPlugin.beginImageData();
        
        while(inPlugin.hasMoreImageData()) {
            inPlugin.readImageData(block);
            outPlugin.writeImageData(block,cFormat);
        }
        
        inPlugin.endImageData();
        outPlugin.endImageData();
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
        
        if(inPlugin.isCheckable()) {
            if(inPlugin.getChecksumObj().getValue() 
            != inPlugin.getHeader().getChecksum()) {
                throw new IOException("Eingabe Prüfsummenfehler!");
            }
        }
        if(outPlugin.isCheckable()
        && inPlugin.isCheckable()) {
            if(inPlugin.getChecksumObj().getValue() 
            != inPlugin.getHeader().getChecksum()) {
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
        
        if(inPlugin.isCheckable()) {
            return inPlugin.getChecksumObj().getValue();
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
        
        if(outPlugin.isCheckable()) {
            return outPlugin.getChecksumObj().getValue();
        }
        return 0;
    }

    /**
     *
     * @return
     */
    public ImageModule getInputPlugin() {
        return inPlugin;
    }

    /**
     *
     * @return
     */
    public ImageModule getOutputPlugin() {
        return outPlugin;
    }
    
    /**
     *
     * @param ext
     * @param streamLen
     * @return
     */
    private static ImageModule createModule(String ext, RandomAccessFile stream) {
        switch(ext) {
            case "tga" -> {
                return new ImageModuleTGA(stream);
            }
            case "propra" -> {
                return new ImageModuleProPra(stream);
            }
        }
        return null;
    }
}
