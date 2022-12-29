package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.util.IChecksum;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTranscoder.Compression;
import propra.imageconverter.data.IDataTranscoder.EncodeMode;
import propra.imageconverter.image.ColorUtil.ColorOp;
import propra.imageconverter.image.compression.ImageTranscoderAuto;
import propra.imageconverter.image.compression.ImageTranscoderRaw;

/**
 *
 */
public abstract class ImageResource extends DataResource {
    
    // Größe des Bildheaders in der Datei
    protected int fileHeaderSize;
    
    // Bildattribute
    protected ImageAttributes header;

    // Zugeordneter Codec zum kodieren/dekodieren der Bilddaten
    protected IDataTranscoder transcoder;
    
    /**
     * 
     */
    protected ImageResource(String file, boolean write) throws IOException {
        super(file, write);
        header = new ImageAttributes();
        transcoder = ImageTranscoderRaw.createTranscoder(header);
    }
    
    /**
     *
     */
    abstract public ImageAttributes readHeader() throws IOException;
    
    /**
     * 
     */
    abstract public void writeHeader() throws IOException;
    
    /**
     * 
     */
    public void setHeader(ImageAttributes newHeader) {
        this.header = new ImageAttributes(newHeader);
        transcoder = ImageTranscoderRaw.createTranscoder(header);
        
        if(checksum != null) {
            header.setChecksum(checksum.getValue());
        }
    }
    
    /**
     * Konvertiert ein Quellbild in ein neues Bild mit gegebenem Format und 
     * Kompression
     */
    public ImageResource convertTo(String outFile, Compression outEncoding) throws IOException {
        if(outFile == null) {
            throw new IllegalArgumentException();
        }   
        
        // Bildattribute einlesen
        readHeader();
        
        /* 
         * Neues Bild anlegen
         */
        ImageResource transcodedImage = createResource(outFile, true); 
        
        // Neuen Header erstellen  
        ImageAttributes outHeader = new ImageAttributes(header);
        outHeader.setCompression(outEncoding); 
        outHeader.setFormat(transcodedImage.getAttributes().getFormat());
        transcodedImage.setHeader(outHeader);
        
        /*
         * ggfs. notwendige Farbkonvertierung ermitteln
         */
        ColorOp colorConverter = null;
        if(transcodedImage.getAttributes().getFormat() != header.getFormat()) { 
            switch(header.getFormat()) {
                case COLOR_BGR -> {
                    colorConverter = ColorUtil::convertBGRtoRBG;
                }
                case COLOR_RBG -> {
                    colorConverter = ColorUtil::convertRBGtoBGR;
                }
            }
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
         * Bei Automodus einen Vorlauf durchführen und bestes Verfahren wählen
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
     *
     * @return
     */
    public ImageAttributes getAttributes() {
        return header;
    }

    /**
     *
     * @return
     */
    public IDataTranscoder getTranscoder() {
        return transcoder;
    }
    
    /**
     *
     * @return
     */
    public IChecksum getChecksum() {
        return checksum;
    }
}
