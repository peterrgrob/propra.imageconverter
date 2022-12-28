package propra.imageconverter.image;

import propra.imageconverter.image.compression.ImageTranscoderHuffman;
import java.io.IOException;
import java.util.ArrayList;
import propra.imageconverter.util.IChecksum;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTranscoder.Operation;
import propra.imageconverter.image.compression.ImageTranscoderAuto;
import propra.imageconverter.image.compression.ImageTranscoderRLE;
import propra.imageconverter.image.compression.ImageTranscoderRaw;

/**
 *
 * @author pg
 */
public abstract class ImageResource extends DataResource {
    
    // Größe des Bildheaders in der Datei
    protected int fileHeaderSize;
    
    // Bildattribute
    protected ImageAttributes header;
    
    // Aktuelles konvertiertes Bild
    protected ImageResource transcodedImage;

    /**
     * 
     */
    protected ImageResource(String file, boolean write) throws IOException {
        super(file, write);
        this.header = new ImageAttributes();
    }
    
    /**
     * Erstellt ein ImageResource Objekt
     * 
     * @param path
     * @param ext
     * @param write
     * @return
     * @throws IOException 
     */
    public static ImageResource createResource(String path, String ext, boolean write) throws IOException {
        switch(ext) {
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
        inCodec = createCodec(header);
        
        if(checksum != null) {
            header.setChecksum(checksum.getValue());
        }
    }
    
    /**
     * Konvertiert ein Quellbild in ein neues Bild mit gegebenem Format und 
     * Kodierung
     * 
     * @param outFile
     * @param ext
     * @param outEncoding
     * @return
     * @throws IOException 
     */
    public ImageResource convertTo(String outFile, String ext, Compression outEncoding) throws IOException {
        if(outFile == null) {
            throw new IllegalArgumentException();
        }
        
        // Neues Bild erstellen
        transcodedImage = createResource(outFile, ext, true);    
        
        // Bildattribute einlesen
        readHeader();
        
        // Neuen Header erstellen
        ImageAttributes outHeader = new ImageAttributes(header);
        outHeader.setCompression(outEncoding); 
        outHeader.setFormat(transcodedImage.getAttributes().getFormat());  
        transcodedImage.setHeader(outHeader);
        
        // Header erstmal überspringen, wird später geschrieben
        transcodedImage.position(transcodedImage.fileHeaderSize);
        
        // Farbkonvertierung ermitteln
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
        
        // Bildkonvertierung initialisieren
        IDataTranscoder outCodec = transcodedImage.getCodec();
        
        // Analyselauf für den Encoder notwendig?
        if(outCodec.analyzeNecessary()) {
            
            // Position merken
            long p = position();

            getInputStream().enableChecksum(false);

            // Dekodierung für Analyse starten
            outCodec.beginOperation(Operation.ANALYZE, transcodedImage.getOutputStream());
            inCodec.decode(getInputStream(), new ColorFilter( colorConverter, outCodec));
                                            
            getInputStream().enableChecksum(true);
            outCodec.endOperation();

            // Ursprüngliche Position wiederherstellen
            position(p);
        }
        
        // Dekodierung und Enkodierung starten
        outCodec.beginOperation(Operation.ENCODE, transcodedImage.getOutputStream());
        inCodec.decode(getInputStream(), new ColorFilter( colorConverter, outCodec));

        // Bildkonvertierung abschließen
        transcodedImage.getAttributes()
                       .setDataLength(outCodec.endOperation());

        //  Falls nötig Header mit Prüfsumme, oder Länge des komprimierten Datensegements 
        //  aktualisieren
        transcodedImage.writeHeader();
        
        return transcodedImage;
    }
    
    /**
     * 
     */
    protected IDataTranscoder createCodec(ImageAttributes header) {
        if(header != null) {
            switch(header.getCompression()) {
                case NONE -> {
                    return new ImageTranscoderRaw(this);
                }
                case RLE -> {
                    return new ImageTranscoderRLE(this);
                }
                case HUFFMAN -> {
                    return new ImageTranscoderHuffman(this);
                }
                case AUTO -> {
                    ArrayList<ImageTranscoderRaw> encoderList = new ArrayList<>();
                    encoderList.add(new ImageTranscoderHuffman(this));
                    encoderList.add(new ImageTranscoderRLE(this));                
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
    public IDataTranscoder getCodec() {
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
