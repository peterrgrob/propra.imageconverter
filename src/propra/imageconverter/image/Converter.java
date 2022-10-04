package propra.imageconverter.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import propra.imageconverter.util.*;

/**
 *
 * @author pg
 */
public class Converter {
    
    public Converter() {
    }
   
    /**
     * 
     * @param cmd
     * @return 
     * @throws java.io.FileNotFoundException 
     */
    public ImageReader createReader(CmdLine cmd) throws FileNotFoundException, IOException {
        FileInputStream fInput = new FileInputStream(cmd.getOption(CmdLine.Options.INPUT_FILE));
        switch(cmd.getOption(CmdLine.Options.INPUT_EXT)) {
            case "tga":
                return new ImageReaderTGA(fInput);
        }
        throw new IOException();
    }
    
    /**
     * 
     * @param cmd
     * @return 
     * @throws java.io.IOException 
     */
    public ImageWriter createWriter(CmdLine cmd) throws IOException {
        String path = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        
        File file = new File(path);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        FileOutputStream fOutput = new FileOutputStream(file);
        switch(cmd.getOption(CmdLine.Options.OUTPUT_EXT)) {
            case "tga":
                return new ImageWriterTGA(fOutput);
        }
        throw new IOException();
    }
    
    /**
     * 
     * @param cmdLine 
     * @throws java.io.FileNotFoundException 
     */
    public void Convert(CmdLine cmdLine) throws FileNotFoundException {
        try {
            ImageReader reader = createReader(cmdLine);
            ImageWriter writer = createWriter(cmdLine);
            
            ImageInfo inputInfo = reader.readInfo();
            writer.writeInfo(inputInfo);
            
            System.out.print(reader.getInfo().getWidth());
            System.out.print(reader.getInfo().getHeight());
            System.out.print(reader.getInfo().getElementSize());
            
            ImageBuffer iBuffer = reader.readBlock(reader.getInfo().getTotalSize());
            writer.writeBlock(iBuffer);
            
        } catch (IOException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
