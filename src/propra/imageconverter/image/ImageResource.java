package propra.imageconverter.image;

import propra.imageconverter.image.compression.ImageCompressionHuffman;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.util.IChecksum;
import propra.imageconverter.data.DataCompression;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataCompression.Operation;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.data.IDataCompression;

/**
 *
 * @author pg
 */
public abstract class ImageResource extends DataResource 
                                    implements IDataTarget {
    
    // Größe des Bildheaders in der Datei
    protected int fileHeaderSize;
    
    // Bildattribute
    protected ImageAttributes header;
    
    // Konvertiertes Bild
    protected ImageResource transcodedImage;
    
    // Prüfsumme 
    protected IChecksum checksum;
    
    // Farbkonvertierung Methode 
    protected ColorFilter colorConverter;

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
        
        // Farbconverter setzen
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
        
        // Bild ggfs. analysieren für Kodierungen
        analyzeCompression();
        
        // Bildkonvertierung initialisieren
        IDataCompression outCodec = transcodedImage.getCodec();
        
        inCodec.begin(Operation.DECODE);
        outCodec.begin(Operation.ENCODE);

        // Dekodierung starten
        inCodec.decode(this);

        // Bildkonvertierung abschließen
        inCodec.end();
        outCodec.end();
        
        //  Falls nötig Header mit Prüfsumme, oder Länge des komprimierten Datensegements 
        //  aktualisieren
        if( transcodedImage.getChecksum() != null
        ||  transcodedImage.getAttributes().getCompression() != Compression.NONE) {
            transcodedImage.writeHeader(transcodedImage.getAttributes());
        }
        
        return transcodedImage;
    }

    /**
     * Analyse für die Kompression durchführen.         
     * Analyse durch Ein- und Ausgabecodec erfordert je nach Kombination
     * eine spezielle Behandlung
     * 
     * @throws IOException 
     */
    private void analyzeCompression() throws IOException {
        IDataCompression outCodec = transcodedImage.getCodec();
        
        // Keine Analyse notwendig?
        if( !inCodec.analyzeNecessary(Operation.DECODE)
        &&  !transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            return;
        }

        // Position merken
        long p = position();

        ByteBuffer dataBlock = ByteBuffer.allocate(DataCompression.DEFAULT_BLOCK_SIZE);
        CheckedInputStream in = getInputStream();
        in.enableChecksum(false);
        
        inCodec.begin(Operation.DECODE_ANALYZE);
        outCodec.begin(Operation.ENCODE_ANALYZE);

        // Analyse für Ein- und Ausgabe
        if( inCodec.analyzeNecessary(Operation.DECODE)
        &&  outCodec.analyzeNecessary(Operation.ENCODE)) {
            // Durchlauf für Decoder-Analyse 
            while(position() < length()) {
                in.read(dataBlock);
                inCodec.analyze(dataBlock, false);
            }

            // Ursprüngliche Position wiederherstellen
            position(p);

            // Durchlauf mit Dekodierung und Encoder-Analyse 
            while(position() < length()) {
                inCodec.decode(this);
            } 

        } else if(inCodec.analyzeNecessary(Operation.DECODE)) {
            // Durchlauf nur für Decoder-Analyse 
            while(position() < length()) {
                in.read(dataBlock);
                inCodec.analyze(dataBlock, false);
            }
            
        } else if(outCodec.analyzeNecessary(Operation.ENCODE)) {
            // Durchlauf mit Dekodierung und Encoder-Analyse 
            while(position() < length()) {
                inCodec.decode(this);
            } 
        }

        in.enableChecksum(true);
        inCodec.end();
        outCodec.end();

        // Ursprüngliche Position wiederherstellen
        position(p);
    }
        
    /**
     * 
     * @param event
     * @param caller
     * @param block
     * @param last
     * @throws IOException 
     */
    @Override
    public void onData( Event event, IDataCompression caller, 
                        ByteBuffer block, boolean last) throws IOException {
        switch(event) {
            case DATA_DECODED -> {
                if(caller.getOperation() == Operation.DECODE_ANALYZE) {
                    transcodedImage.getCodec().analyze( block, last);
                } else {
                    // Farben ggfs. konvertieren
                    if(colorConverter != null) {
                        ColorUtil.filterColorBuffer(block, block, colorConverter);
                    }
                    
                    // An Encoder weiterreichen
                    transcodedImage.getCodec().encode(block, last);
                }
            }
        }
    }
    
    /**
     * 
     * @param header
     * @return 
     */
    protected IDataCompression createCodec(ImageAttributes header) {
        if(header != null) {
            switch(header.getCompression()) {
                case NONE -> {
                    return new ImageCompressionRaw(this);
                }
                case RLE -> {
                    return new ImageCompressionRLE(this);
                }
                case HUFFMAN -> {
                    return new ImageCompressionHuffman(this);
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
    public IDataCompression getCodec() {
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
