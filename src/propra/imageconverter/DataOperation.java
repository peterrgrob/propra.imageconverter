package propra.imageconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import propra.imageconverter.util.CmdLine;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataModel;
import propra.imageconverter.util.DataTranscoder;

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
        
        DataTranscoder readCoding = null;
        DataTranscoder writeCoding = null;  
        
        // Eingabedatei öffnen
        RandomAccessFile inStream = new RandomAccessFile(   cmd.getOption(CmdLine.Options.INPUT_FILE),
                                                            "r");
        
        // Ausgabedatei Pfad ableiten
        String outPath = cmd.getOption(CmdLine.Options.INPUT_FILE);      
        
        // Pfad und Transcoder wählen, je nach Operation
        if(cmd.isBaseNDecode()) {
            
            outPath = outPath.replaceAll(".base-32", "");
            readCoding = cmd.getBaseN();
            writeCoding = null;
            
        } else {
            
            outPath = outPath.concat(".base-32");
            readCoding = null;
            writeCoding = cmd.getBaseN();
            
        }
        
        // Ausgabedatei erstellen
        // TODO: Verzeichnis erstellen
        File file = new File(outPath);
        if(!file.exists()) {
            file.createNewFile();
        }
            
        // Datei öffnen
        RandomAccessFile outStream = new RandomAccessFile(file,"rw");
            
        // Passende Ein- und Ausgabe Objekte erstellen
        inModel = new DataModel(DataModel.IOMode.READ, 
                                inStream, 
                                readCoding);
        outModel = new DataModel(DataModel.IOMode.WRITE, 
                                outStream, 
                                writeCoding);
  
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
        
        // Datenübertragung
        inModel.read(block);
        outModel.write(block);
        
        // Blockweise Übertragung beenden     
        inModel.end(DataModel.IOMode.READ);
        outModel.end(DataModel.IOMode.WRITE);
    }
}
