package propra.imageconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.basen.BaseNFormat;
import propra.imageconverter.basen.BaseNReader;
import propra.imageconverter.basen.BaseNWriter;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataReader;
import propra.imageconverter.data.DataWriter;

/**
 *
 * @author pg
 */
public class BaseNTask {
    
    private DataReader reader;
    private DataWriter writer;
    
    /**
     * 
     */
    public BaseNTask() {
        reader = null;
        writer = null;
    }
    
    /**
     *
     * @param cmd
     * @throws java.io.FileNotFoundException
     */
    public BaseNTask(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        // Ausgabedatei Pfad ableiten
        String outPath = cmd.getOption(CmdLine.Options.INPUT_FILE);      
        
        // Datenformat ableiten
        BaseNFormat dataFormat = cmd.getBaseNDataFormat();            
            
        // Ausgabeendung ableiten
        String outExt;
        if(cmd.isBase32()) {
            outExt = ".base-32";
        } else {
            outExt = ".base-n";
        }
        
        // Pfad für die Ausgabedatei anpassen
        if(cmd.isBaseNDecode()) {
            outPath = outPath.replaceAll(outExt, "");  
        } else {    
            // Prüfen ob ein gültiges Alphabet übergeben wurde
            if(!dataFormat.isValid()) {
                throw new IllegalArgumentException("Ungültiges Base-N Alpahabet.");
            }
        
            outPath = outPath.concat(outExt);
        }          

            
        // Reader/Writer erstellen
        if(cmd.isBaseNDecode()) {
            reader = new BaseNReader(cmd.getOption(CmdLine.Options.INPUT_FILE), dataFormat);
            writer = new DataWriter(outPath, IOMode.BINARY);
        } else {
            reader = new DataReader(cmd.getOption(CmdLine.Options.INPUT_FILE), IOMode.BINARY);
            writer = new BaseNWriter(outPath, dataFormat);
        }   
    }
    
    /**
     *
     * @return
     */
    public boolean isValid() {
        return (    reader    != null
                &&  writer   != null);
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
        ByteBuffer block = ByteBuffer.allocate((int)reader.getSize());
        block.limit((int)reader.getSize());
        
        // Blockweise Übertragung starten
        reader.begin();
        writer.begin();
        
        // Datenübertragung
        reader.read(block);
        writer.write(block);
        
        // Blockweise Übertragung beenden     
        reader.end();
        writer.end();
    }
}
