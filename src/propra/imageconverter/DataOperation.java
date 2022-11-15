package propra.imageconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import propra.imageconverter.util.CmdLine;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataModel;

/**
 *
 * @author pg
 */
public class DataOperation {
    
    private DataModel inModel;
    private DataModel outModel;

    /**
     *
     */
    public DataOperation() {
        
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
        
        // Eingabedatei öffnen
        RandomAccessFile inStream = new RandomAccessFile(cmd.getOption(CmdLine.Options.INPUT_FILE),"r");
        
        // Objekte erstellen
        if(cmd.isBaseNDecode()) {
            
            // Ausgabedatei erstellen, öffnen
            String outPath = cmd.getOption(CmdLine.Options.INPUT_FILE);
            outPath = outPath.replaceAll(".base-32", "");
            File file = new File(outPath);
            if(!file.exists()) {
                file.createNewFile();
            }
            
            RandomAccessFile outStream = new RandomAccessFile(file,"rw");

            inModel = new DataModel(DataModel.IOMode.READ, inStream, cmd.getBaseN());
            outModel = new DataModel(DataModel.IOMode.WRITE, outStream, null);
        } else {
            
            // Ausgabedatei erstellen, öffnen
            String outPath = cmd.getOption(CmdLine.Options.INPUT_FILE);
            outPath = outPath.concat(".base-32");
            File file = new File(outPath);
            if(!file.exists()) {
                file.createNewFile();
            }
            
            RandomAccessFile outStream = new RandomAccessFile(file,"rw");
            
            inModel = new DataModel(DataModel.IOMode.READ, inStream, null);
            outModel = new DataModel(DataModel.IOMode.WRITE, inStream, cmd.getBaseN());
        }
    }
    
    /**
     *
     * @throws IOException
     */
    public void convert() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        begin();
        process();
        end(); 
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
            
        }
        
        return stateString;
    }
    
    /**
     *
     * @return 
     * @throws IOException
     */
    private void begin() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
    }
    
    /**
     *
     * @throws IOException
     */
    private void end() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
       
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
        DataBuffer block = new DataBuffer((int)inModel.getContentSize(DataModel.IOMode.READ));
        
        // Blockweise Übertragung starten
        inModel.begin(DataModel.IOMode.READ);
        outModel.begin(DataModel.IOMode.WRITE);
        
        inModel.read(block);
        outModel.write(block);
        
        // Blockweise Übertragung beenden     
        inModel.end(DataModel.IOMode.READ);
        outModel.end(DataModel.IOMode.WRITE);
    }
}
