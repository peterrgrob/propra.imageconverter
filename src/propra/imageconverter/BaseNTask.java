package propra.imageconverter;

import propra.imageconverter.data.basen.BaseNResource;
import propra.imageconverter.util.CmdLine;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.*;
import propra.imageconverter.util.CmdLine.Options;

/**
 * Klasse implementiert BaseN Programmfunktionalität
 * 
 * @author pg
 */
public class BaseNTask implements AutoCloseable, IDataTarget {
    
    // Ressourcen
    private BaseNResource baseNFile;
    private DataResource binaryFile;
    
    // Kommandozeilenobjekt
    private final CmdLine cmd;
    
    /**
     * Konstruktor, initialisiert die Operation
     * 
     */
    public BaseNTask(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        this.cmd = cmd;
    }

    /**
     * Aufgabe ausführen
     */
    public void run() throws IOException {
        
        // Ausgabedatei Pfad ableiten
        String outPath = cmd.getOption(Options.INPUT_FILE);      
        if(outPath == null) {
            throw new FileNotFoundException("Keine Eingabedatei gegeben!");            
        }
        
        // BaseN Kodierung ableiten
        String alphabet = cmd.getAlphabet();
        
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
            outPath = outPath.concat(outExt);
        }          
        ImageConverter.printMessage(outPath);
        
        /*
         *  Kodieren / Dekodieren 
         */
        if(cmd.isBaseNDecode()) {
            // Resourcenobjekte erstellen
            baseNFile = new BaseNResource(  cmd.getOption(Options.INPUT_FILE), 
                                            alphabet,false);
            binaryFile = new DataResource(  outPath,true);
            
            baseNFile.decodeTo(this);
            
        } else {
            baseNFile = new BaseNResource(  outPath,alphabet,true);
            binaryFile = new DataResource(  cmd.getOption(Options.INPUT_FILE),false);
            
            baseNFile.encodeFrom(binaryFile);
        }  
        
        baseNFile.close();
        binaryFile.close();
    }
    
    
    
    /**
     * Schließt geöffnete Resourcen, wird automatisch bei Verwendung mit 
     * try-with-resources aufgerufen
     */
    @Override
    public void close() throws Exception {
        if(baseNFile != null) {
            baseNFile.close();
        }
        if(binaryFile != null) {
            baseNFile.close();            
        }
    }

    /**
     * 
     * @param event
     * @param caller
     * @param data
     * @param lastBlock
     * @throws IOException 
     */
    @Override
    public void onData( Event event, IDataCompression caller, 
                        ByteBuffer data, boolean lastBlock) throws IOException {
        if(event == Event.DATA_DECODED) {
            binaryFile.getOutputStream().write(data);
        }
    }
}
