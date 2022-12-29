package propra.imageconverter.image;

import propra.imageconverter.image.compression.ImageTranscoderHuffman;
import java.io.IOException;
import java.util.ArrayList;
import propra.imageconverter.util.IChecksum;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.DataUtil;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTranscoder.Compression;
import propra.imageconverter.data.IDataTranscoder.Operation;
import propra.imageconverter.image.compression.ImageTranscoderAuto;
import propra.imageconverter.image.compression.ImageTranscoderRLE;
import propra.imageconverter.image.compression.ImageTranscoderRaw;

/**
 *
 */
public abstract class ImageResource extends DataResource {
    
    // Größe des Bildheaders in der Datei
    protected int fileHeaderSize;
    
    // Bildattribute
    protected ImageAttributes header;

    /**
     * 
     */
    protected ImageResource(String file, boolean write) throws IOException {
        super(file, write);
        this.header = new ImageAttributes();
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
    public void setHeader(ImageAttributes srcHeader) {
        this.header = new ImageAttributes(srcHeader);
        inCodec = createTranscoder(header);
        
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
        ColorOperation colorConverter = null;
        if(transcodedImage.getAttributes().getFormat() != header.getFormat()) { 
            switch(header.getFormat()) {
                case COLOR_BGR -> {
                    colorConverter = ColorOperations::convertBGRtoRBG;
                }
                case COLOR_RBG -> {
                    colorConverter = ColorOperations::convertRBGtoBGR;
                }
            }
        }
        
        // Position des Datenblocks merken
        long p = position();
         
        /*
         * ggfs. Analyselauf für den Encoder durchführen
         */
        IDataTranscoder encoder = transcodedImage.getTranscoder();
        if(encoder.analyzeNecessary()) {

            getInputStream().enableChecksum(false);

            // Dekodierung für Analyse starten
            encoder.beginOperation(Operation.ANALYZE, transcodedImage.getOutputStream());
            inCodec.decode(getInputStream(), new ColorFilter( colorConverter, encoder));
                                            
            getInputStream().enableChecksum(true);
            encoder.endOperation();

            // Ursprüngliche Position wiederherstellen
            position(p);
        }
        
        /*
         * Bei Automodus einen Vorlauf durchführen und bestes Verfahren wählen
         */
        if(outEncoding == Compression.AUTO) {
            
            encoder.beginOperation(Operation.ENCODE, transcodedImage.getOutputStream());
            inCodec.decode(getInputStream(), new ColorFilter( colorConverter, encoder));
            encoder.endOperation();
            encoder = ((ImageTranscoderAuto)encoder).getWinner();
            transcodedImage.getAttributes().setCompression(encoder.getCompression());
            
            // Ursprüngliche Position wiederherstellen
            position(p);
        }
        
        /* 
         * Finale Dekodierung und Kodierung starten
         */
        
        // Header überspringen, wird später geschrieben
        transcodedImage.position(transcodedImage.fileHeaderSize);
        
        // Dekodieren und Kodieren
        encoder.beginOperation(Operation.ENCODE, transcodedImage.getOutputStream());
        inCodec.decode(getInputStream(), new ColorFilter( colorConverter, encoder));

        // Konvertierung abschließen und Header schreiben 
        transcodedImage.getAttributes().setDataLength(encoder.endOperation());
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
     */
    protected static IDataTranscoder createTranscoder(ImageAttributes header) {
        if(header != null) {
            switch(header.getCompression()) {
                case NONE -> {
                    return new ImageTranscoderRaw(header);
                }
                case RLE -> {
                    return new ImageTranscoderRLE(header);
                }
                case HUFFMAN -> {
                    return new ImageTranscoderHuffman(header);
                }
                case AUTO -> {
                    ArrayList<ImageTranscoderRaw> encoderList = new ArrayList<>();
                    encoderList.add(new ImageTranscoderHuffman(header));
                    encoderList.add(new ImageTranscoderRLE(header));                
                    return new ImageTranscoderAuto(encoderList);
                }
            }
        }
        throw new UnsupportedOperationException("Nicht unterstützte Kompression!");
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
        return inCodec;
    }
    
    /**
     *
     * @return
     */
    public IChecksum getChecksum() {
        return checksum;
    }
}
