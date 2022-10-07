package propra.imageconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import propra.imageconverter.image.*;
import propra.imageconverter.util.*;


/**
 *
 * @author pg
 */
public class ImageConverter {

    protected static final int ERROR_EXIT_CODE = 123;
    public static final Messages MSG = new MessagesSimple();
            
    /** 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CmdLine cmdLine = new CmdLine(args);
            System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_FILE));
            System.out.println(cmdLine.getOption(CmdLine.Options.OUTPUT_FILE));
             
            System.out.println(cmdLine.getOption(CmdLine.Options.INPUT_EXT));
            System.out.println(cmdLine.getOption(CmdLine.Options.OUTPUT_EXT));
            
            ImageConverter converter = new ImageConverter(); 
            converter.Convert(cmdLine);
        }
        catch(FileNotFoundException e) {
            System.exit(ERROR_EXIT_CODE);
        }
    }
    
    /**
     * 
     * @param cmdLine 
     * @throws java.io.FileNotFoundException 
     */
    public void Convert(CmdLine cmdLine) throws FileNotFoundException {    
        try {
            long start = System.currentTimeMillis();

            ImageReader reader = createReader(cmdLine);
            ImageWriter writer = createWriter(cmdLine);
            
            ImageHeader inputInfo = reader.readHeader();
            writer.writeHeader(inputInfo);
            
            System.out.print("Eingabe: "+reader.getHeader().getWidth());
            System.out.print("x" + reader.getHeader().getHeight());
            System.out.print("x" + reader.getHeader().getElementSize());
            
            ImageBuffer iBuffer = reader.readContent(reader.getHeader().getTotalSize());
            writer.writeContent(iBuffer);
            
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("\nAbgeschlossen in (ms):" + String.valueOf(timeElapsed));
            
        } catch (IOException ex) {
            Logger.getLogger(ImageConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            case "propra":
                return new ImageReaderPropra(fInput);
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
            case "propra":
                return new ImageWriterProPra(fOutput);
        }
        throw new IOException();
    }
}
