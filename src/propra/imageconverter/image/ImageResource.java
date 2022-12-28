package propra.imageconverter.image;

import propra.imageconverter.image.compression.ImageTranscoderHuffman;
import java.io.IOException;
import propra.imageconverter.util.IChecksum;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.IDataTranscoder.Operation;
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
     * @param file
     * @param write
     * @throws java.io.IOException
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
     * @return 
     * @throws IOException
     */
    abstract public ImageAttributes readHeader() throws IOException;
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageAttributes srcHeader) throws IOException {
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
        
        // Neuen Header schreiben
        ImageAttributes outHeader = new ImageAttributes(header);
        outHeader.setCompression(outEncoding); 
        outHeader.setFormat(transcodedImage.getAttributes().getFormat());  
        transcodedImage.writeHeader(outHeader);
        
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
        if(outCodec.analyzeNecessary(Operation.ENCODE)) {
            
            // Position merken
            long p = position();

            getInputStream().enableChecksum(false);

            // Dekodierung für Analyse starten
            inCodec.decode(new ColorFilter(colorConverter, outCodec.beginOperation(Operation.ANALYZE)));

            getInputStream().enableChecksum(true);
            outCodec.endOperation();

            // Ursprüngliche Position wiederherstellen
            position(p);
        }
        
        // Dekodierung und Enkodierung starten
        inCodec.decode(new ColorFilter(colorConverter, outCodec.beginOperation(Operation.ENCODE)));

        // Bildkonvertierung abschließen
        outCodec.endOperation();
        
        //  Falls nötig Header mit Prüfsumme, oder Länge des komprimierten Datensegements 
        //  aktualisieren
        if( transcodedImage.getChecksum() != null
        ||  transcodedImage.getAttributes().getCompression() != Compression.NONE) {
            transcodedImage.writeHeader(transcodedImage.getAttributes());
        }
        
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
