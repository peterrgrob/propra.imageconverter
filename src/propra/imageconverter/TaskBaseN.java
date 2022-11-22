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
 * Klasse implementiert BaseN Programmfunktionalität
 * 
 * @author pg
 */
public class TaskBaseN {
    
    // Ein- und Ausgabeobjekt 
    private DataReader reader;
    private DataWriter writer;
    
    /**
     * 
     */
    public TaskBaseN() {
        reader = null;
        writer = null;
    }
    
    /**
     * Konstruktor, initialisiert den Task 
     * 
     * @param cmd
     * @throws java.io.FileNotFoundException
     */
    public TaskBaseN(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        // Ausgabedatei Pfad ableiten
        String outPath = cmd.getOption(CmdLine.Options.INPUT_FILE);      
        if(outPath == null) {
            throw new FileNotFoundException("Keine Eingabedatei gegeben!");            
        }
        
        // Datenformat ableiten
        BaseNFormat dataFormat = cmd.getBaseNDataFormat();  
        if(dataFormat == null) {
            throw new IOException("Kein Alphabet gegeben!");            
        }
            
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

        System.out.println(outPath);
            
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
     * @return true, wenn IO gesetzt
     */
    public boolean isValid() {
        return (    reader    != null
                &&  writer   != null);
    }
    
    /**
     *
     * @return Statusstring
     */
    @Override
    public String toString() {
        String stateString = "";

        if(isValid()) {
        }
        
        return stateString;
    }
    
    /**
     * Aufgabe ausführen
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
        
        // Datenübertragung
        reader.read(block);
        writer.write(block);
    }
}
