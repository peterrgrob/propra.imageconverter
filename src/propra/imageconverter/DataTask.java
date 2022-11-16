package propra.imageconverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.data.BaseN;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataModel;
import propra.imageconverter.data.DataTranscoder;

/**
 *
 * @author pg
 */
public class DataTask {
    
    private DataModel inModel;
    private DataModel outModel;

    /**
     *
     */
    public DataTask() {
        
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
        DataFormat dataFormat = null;
        
        // Ausgabedatei Pfad ableiten
        String outPath = cmd.getOption(CmdLine.Options.INPUT_FILE);      
        
        // Datenformat ableiten
        dataFormat = cmd.getBaseNDataFormat();            
            
        // Ausgabeendung ableiten
        String outExt;
        if(cmd.isBase32()) {
            outExt = ".base-32";
        } else {
            outExt = ".base-n";
        }
        
        // Pfad und Transcoder wählen, je nach Operation
        if(cmd.isBaseNDecode()) {
            readCoding = new BaseN(dataFormat);
            outPath = outPath.replaceAll(outExt, "");  
        } else {    
            // Prüfen ob ein gültiges Alphabet übergeben wurde
            if(!dataFormat.isValid()) {
                throw new IllegalArgumentException("Ungültiges Base-N Alpahabet.");
            }
        
            outPath = outPath.concat(outExt);
            writeCoding = new BaseN(dataFormat); 
        }                    
        
        // Verzeichnisse erstellen, falls nötig
        Path outDirs = Paths.get(outPath);
        Files.createDirectories(outDirs.getParent());
        
        // Ausgabedatei erstellen
        File file = new File(outPath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        // Passende Ein- und Ausgabe Objekte erstellen
        inModel = new DataModel(DataModel.IOMode.READ,  
                                readCoding);
        outModel = new DataModel(DataModel.IOMode.WRITE,  
                                writeCoding);
            
        // Dateistreams erstellen
        if(cmd.isBaseNDecode()) {
            inModel.SetInputOutput(new RandomAccessFile(cmd.getOption(CmdLine.Options.INPUT_FILE),"r"), null);
            outModel.SetInputOutput(null, new RandomAccessFile(file,"rw"));
        } else {
            outModel.SetInputOutput(null, new BufferedWriter(new FileWriter(file)));
            inModel.SetInputOutput(new RandomAccessFile(   cmd.getOption(CmdLine.Options.INPUT_FILE),"r"), null);
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
        String stateString = "";

        if(isValid()) {
  
        }
        
        return stateString;
    }
    
    /**
     * 
     * @throws java.io.IOException 
     */
    public void doTask() throws IOException {
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
