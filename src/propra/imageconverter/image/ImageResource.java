package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTranscoder.Compression;
import propra.imageconverter.data.IDataTranscoder.EncodeMode;
import propra.imageconverter.image.Color.Format;
import propra.imageconverter.image.ColorUtil.ColorOp;
import propra.imageconverter.image.compression.ImageTranscoderAuto;
import propra.imageconverter.image.compression.ImageTranscoderRaw;
import propra.imageconverter.util.PropraException;

/**
 *  Basisklasse für ImageRessources
 */
public abstract class ImageResource extends DataResource {
    
    // Größe des Bildheaders in der Datei
    protected int fileHeaderSize;
    
    // Bildattribute
    protected ImageAttributes header;

    // Passender Codec zum kodieren/dekodieren der Bilddaten
    protected IDataTranscoder transcoder;
    
    /**
     * Initialisiert die Bild-Ressource zum Lesen/Schreiben
     */
    protected ImageResource(String file, boolean write) throws IOException {
        super(file, write);
        header = new ImageAttributes();
        transcoder = ImageTranscoderRaw.createTranscoder(header);
    }
    
    /**
     *  Liest Header ein, zu implementieren
     */
    abstract public ImageAttributes readHeader() throws IOException;
    
    /**
     * Schreibt Header, zu implementieren
     */
    abstract public void writeHeader() throws IOException;
    
    /**
     * Bildattribute setzen
     */
    public void setHeader(ImageAttributes newHeader) {
        this.header = new ImageAttributes(newHeader);
        transcoder = ImageTranscoderRaw.createTranscoder(header);
    }
    
    /**
     * Konvertiert ein Quellbild in ein neues Bild mit gegebenem Format und 
     * Kompression
     */
    public ImageResource convertTo(String outFile, Compression outEncoding) throws IOException {
        PropraException.assertArgument(outFile);
        
        // Bildattribute einlesen
        readHeader();
        PropraException.printMessage("Quellbild: " + header.toString());
        
        /* 
         * Neues Bild anlegen
         */
        ImageResource transcodedImage = createResource(outFile, true); 
        
        // Neuen Header erstellen  
        ImageAttributes outHeader = new ImageAttributes(header);
        outHeader.setCompression(outEncoding); 
        outHeader.setFormat(transcodedImage.getAttributes().getFormat());
        transcodedImage.setHeader(outHeader);
        PropraException.printMessage("Zielbild: " + outHeader.toString() + "\nKonvertierung starten...");
        
        /*
         * ggfs. notwendige Farbkonvertierung ermitteln
         */
        ColorOp colorConverter = null;
        if(transcodedImage.getAttributes().getFormat() != header.getFormat()) { 
            colorConverter = header.getFormat() == Format.COLOR_BGR 
                           ? ColorUtil::convertBGRtoRBG : ColorUtil::convertRBGtoBGR; 
        }
        
        // Position des Datenblocks merken
        long p = position();
        getInputStream().enableChecksum(false); 
        
        /*
         * ggfs. Analyselauf für den Encoder durchführen
         */
        IDataTranscoder encoder = transcodedImage.getTranscoder();
        if(encoder.analyzeNecessary()) {

            // Dekodierung für Analyse starten
            encoder.beginEncoding(EncodeMode.ANALYZE, transcodedImage.getOutputStream());
            transcoder.decode(getInputStream(), new ColorConverter( colorConverter, encoder));
            encoder.endEncoding();

            // Ursprüngliche Position wiederherstellen
            position(p);
        }
        
        /*
         * Bei Automodus einen Vorlauf ohne Speichern durchführen und bestes Verfahren wählen
         */
        if(outEncoding == Compression.AUTO) {
                        
            encoder.beginEncoding(EncodeMode.ENCODE, transcodedImage.getOutputStream());
            transcoder.decode(getInputStream(), new ColorConverter( colorConverter, encoder));
            encoder.endEncoding();
            
            encoder = ((ImageTranscoderAuto)encoder).getWinner();
            transcodedImage.getAttributes().setCompression(encoder.getCompression());
            
            // Ursprüngliche Position wiederherstellen
            position(p);
        }
        
        /* 
         * Finale Dekodierung und Kodierung starten
         */                                        
        getInputStream().enableChecksum(true);
            
        // Header überspringen, wird später geschrieben
        transcodedImage.position(transcodedImage.fileHeaderSize);
        
        // Dekodieren und Kodieren
        encoder.beginEncoding(EncodeMode.ENCODE, transcodedImage.getOutputStream());
        transcoder.decode(getInputStream(), new ColorConverter( colorConverter, encoder));

        // Konvertierung abschließen und Header schreiben 
        transcodedImage.getAttributes().setDataLength(encoder.endEncoding());
        transcodedImage.writeHeader();
        
        // Prüfsumme prüfen
        if(isChecked()) {
            if(getCurrentChecksum() != getAttributes().getChecksum()) {
                throw new IOException(  "Prüfsumme " 
                                        + String.format("0x%08X", (int)getCurrentChecksum()) 
                                        + " ungleich " 
                                        + String.format("0x%08X", (int)getAttributes().getChecksum()));
            }
        }
        
        // Infos ausgeben
        PropraException.printMessage("abgeschlossen ("  + transcodedImage.getAttributes().getDataLength() 
                                                        + " Bytes, Leseprüfsumme (Ok): " + String.format("0x%08X", (int)getAttributes().getChecksum()) 
                                                        + (transcodedImage.isChecked() ? ", Schreibprüfsumme:" + String.format("0x%08X", (int)getAttributes().getChecksum()) : ""
                                                        + ")"));
        return transcodedImage;
    }
   
    /**
     * Erstellt ein ImageResource Objekt passend zur Dateiendung
     */
    public static ImageResource createResource(String path, boolean write) throws IOException {
        switch(DataUtil.getExtension(path)) {
            case "tga" -> {
                return new ImageResourceTGA(path, write);
            }
            case "propra" -> {
                return new ImageResourceProPra(path, write);
            }
        }
        throw new UnsupportedOperationException("Nicht unterstütztes Dateiformat!");
    }
   
    /**
     * Gibt Attribute zurück
     */
    public ImageAttributes getAttributes() {
        return header;
    }

    /**
     * Gibt aktuellen Transcoder zurück
     */
    public IDataTranscoder getTranscoder() {
        return transcoder;
    }
    
    /**
     * Ressource unterstützt Prüfsumme?
     */
    public boolean isChecked() {
        return false;
    }
    
    /**
     * Gibt die gespeicherte Prüfsumme zurück
     */
    public long getCurrentChecksum() {
        return header.getChecksum();
    }
}
