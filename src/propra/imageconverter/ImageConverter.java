package propra.imageconverter;

import propra.imageconverter.CmdLine.Options;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;
import java.util.logging.Level;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.basen.BaseNResource;
import propra.imageconverter.data.IDataTranscoder.Compression;
import propra.imageconverter.image.ImageResource;


/**
 *  Einstiegsklasse für ImageConverter.
 * 
 *  Änderungen zu KE2
 *  -   Meine Abgabe für KE2 hat die Verarbeitung großer Dateien nicht korrekt
 *      unterstützt und war teilweise aus Zeitgründen nicht ganz optimal umgesetzt.
 *      Deshalb habe ich jetzt nochmal Änderungen bei der Klassenstruktur 
 *      vorgenommen um die Verarbeitung großer Dateien zu ermöglichen. Die Reader/Writer
 *      sind nun jeweils in einer ImageResource zusammengefasst welche CheckedReader/Writer 
 *      anbietet, zudem ist das Transcoderinterface nun etwas besser strukturiert.
 *
 *  -   Die Farbkonvertierung ist aus Performancegründen jetzt weniger allgemein 
 *      implementiert mit Methoden-Referenzen je nach Kombination.
 *
 *  -   Mit Hilfe der BitStreams, die ich für die Huffman Kompression implementiert habe,
 *      konnte ich die BaseN Kodierung deutlich vereinfachen.
 *      
 *  -   Ich habe allgemein versucht, wie einige Reviewer angemerkt haben, die Komplexität 
 *      mit teilweise unklaren Abhängigkeiten sowie nicht genutzte Teile der 
 *      Klassenstruktur, die im Laufe des Projekts entstanden sind, zu reduzieren. 
 *      
 *  Kleine Übersicht über die Klassenstruktur. Die Resource Klassen bieten Schreiben und Lesen 
 *  der Daten per Stream und jeweils individuelles Lesen und Schreiben der formatspezifischen 
 *  Daten, wie z.B. Bildheader oder BaseN Alphabet. Die Transcoder Klassen implementieren die 
 *  verschiedenen Kompressionsalgorithmen unabhängig vom Ressourcentyp, bzw. die BaseN Kodierung.
 * 
 *  Ablauf der Konvertierung/Kodierung
 *    Stream -> Resource <-> Transcoder(Decoder) -> Filter -> Transcoder(Encoder) <-> Resource -> Stream 
 *       
 *  Klassenstruktur
 *          IDataResource
 *            DataResource     
 *              BaseNResource
 *              ImageResource
 *                  ImageResourcePropra
 *                  ImageResourceTGA
 *          IDataTranscoder
 *              DataTranscoder
 *                  BaseN
 *                  ImageTranscoder
 *                      ImageTranscoderRaw
 *                      ImageTranscoderRLE
 *                      ImageTranscoderHuffman
 *                      ImageTranscoderAuto
 *          IChecksum
 *              NullChecksum
 *                  ChecksumPropra
 *          CheckedInputStream
 *              BitInputStream
 *          CheckedOutputStream
 *              BitOutputStream
 *
 */
public class ImageConverter {
    
    /** 
     * Programmeinstieg
     */
    public static void main(String[] args) {
        try {
            // Zeitmessung starten
            long start = System.currentTimeMillis();   
        
            if(args.length == 0) {
                PropraException.printErrorAndQuit("Keine Parameter übergeben!", null);
            }
            
            // Klasseninstanz erstellen und gewünschten Task starten
            CmdLine cmd = new CmdLine(args);
            ImageConverter converter = new ImageConverter(); 
            if(cmd.isBaseTask()) {
                converter.doBaseNTask(cmd);
            } else {
                converter.doImageTask(cmd);
            }
        
            // Zeitmessung beenden
            long timeElapsed = System.currentTimeMillis() - start;

            // Infos Programmablauf ausgeben
            PropraException.printMessage("Zeit: " + String.valueOf(timeElapsed) + "ms");
            
        } catch(FileNotFoundException e) {
            PropraException.printErrorAndQuit("Datei nicht gefunden!", e);
        }
        catch(IOException e) {
            PropraException.printErrorAndQuit("I/O Fehler:\n", e);
        } 
        catch(Exception e) {
            Logger.getLogger(ImageConverter.class.getName()).log(Level.SEVERE, null, e);
            PropraException.printErrorAndQuit("Unbehandelter Fehler:", e);
        }
    }  
    
    /**
     * Führt Bildkonvertierung entsprechend der Kommandozeilenparameter durch
     */
    public void doImageTask(CmdLine cmd) throws IOException {
        String inPath = cmd.getOption(Options.INPUT_FILE);
        String outPath = cmd.getOption(Options.OUTPUT_FILE);
        
        if(outPath == null) {
            throw new IOException("Kein Ausgabepfad gegeben!");            
        }  
        
        // Ein- und Ausgabedateipfad auf der Konsole ausgeben
        PropraException.printMessage("\n\nBildkonvertierung");
        PropraException.printMessage("Eingabe: " + inPath);
        PropraException.printMessage("Ausgabe: " + outPath);
        
        if(cmd.getCompression() == Compression.AUTO) {
            if(DataUtil.getExtension(outPath).compareTo("tga") == 0) {
                throw new PropraException("AUTO Kompression nicht von tga unterstützt.");
            }
        }
        
        // Readerobjekt erstellen
        try(ImageResource inImage = ImageResource.createResource(inPath,false)) {
            DataUtil.createFileAndDirectory(outPath);

            // Konvertierung starten
            inImage.convertTo(outPath,  cmd.getCompression());
            inImage.close();
        }
    }
    
    /**
     * BaseN Kodierung entsprechend der Kommandozeilenparameter
     */
    public void doBaseNTask(CmdLine cmd) throws FileNotFoundException, 
                                                IOException,    
                                                Exception {    
        // Ausgabedatei Pfad ableiten
        String outPath = cmd.getOption(Options.INPUT_FILE);      
        if(outPath == null) {
            throw new FileNotFoundException("Keine Eingabedatei gegeben!");            
        }

        // Eingabedateipfad auf der Konsole ausgeben
        PropraException.printMessage("BaseN Konvertierung\nEingabedatei: " + outPath);
        
        // Pfad für die Ausgabedatei anpassen
        String outExt = (cmd.isBase32() ? ".base-32" : ".base-n");
        if(cmd.isBaseNDecode()) {
            outPath = outPath.replaceAll(outExt, "");  
        } else {    
            outPath = outPath.concat(outExt);
        }          
        PropraException.printMessage("Ausgabedatei: " + outPath);
        
        /*
         *  Kodieren / Dekodieren 
         */
        String alphabet = cmd.getAlphabet();
        if(cmd.isBaseNDecode()) {
            try(BaseNResource baseNFile = new BaseNResource(cmd.getOption(Options.INPUT_FILE), 
                                                            alphabet,false)) {
                try(DataResource binaryFile = new DataResource(  outPath,true)) {
                    // Dekodierung
                    baseNFile.decode(binaryFile);
                }
            }
        } else {
            try(BaseNResource baseNFile = new BaseNResource(outPath, alphabet,true)) {
                try(DataResource binaryFile = new DataResource(cmd.getOption(Options.INPUT_FILE),false)) {
                    // Kodierung
                    baseNFile.encode(binaryFile);
                }
            }
        }  
    } 
}
