package propra.imageconverter.image;

import propra.imageconverter.image.huffman.ImageCodecHuffman;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.DataFormat.Encoding;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataCodec;
import propra.imageconverter.data.IDataCodec.Operation;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.util.CheckedInputStream;

/**
 *
 * @author pg
 */
public abstract class ImageResource extends DataResource 
                                    implements IDataTarget {
    
    // Größe des Bildheaders in der Datei
    protected int fileHeaderSize; 
    
    // Logischer Bildheader
    protected ImageHeader header;
    
    // Farbformat
    protected ColorFormat colorFormat;
    
    // Konvertiertes Bild
    protected ImageResource transcodedImage;
    
    // Prüfsumme 
    protected Checksum checksum;
    
    // Farbkonvertierung Methode 
    protected ColorFilter colorConverter;

    /**
     * 
     * @param file
     * @param write
     * @throws java.io.IOException
     */
    public ImageResource(String file, boolean write) throws IOException {
        super(file, write);
        colorFormat = new ColorFormat();
    }
    
    /**
     *
     * @return 
     * @throws IOException
     */
    abstract public ImageHeader readHeader() throws IOException;
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        // Prüfsumme aktualisieren und Header speichern
        setHeader(srcHeader);
        if(checksum != null) {
            header.checksum(checksum.getValue());
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
    public ImageResource transferTo(String outFile, String ext, 
                                    Encoding outEncoding) throws IOException {
        if(outFile == null) {
            throw new IllegalArgumentException();
        }
        
        // Neues Bild erstellen
        transcodedImage = createResource(outFile, ext, true);
        if(transcodedImage == null) {
            throw new IllegalArgumentException("Nicht unterstütztes Dateiformat!");
        }        
        
        // Infos einlesen
        readHeader();
        
        // Neuen Header schreiben
        ImageHeader outHeader = new ImageHeader(header);
        outHeader.colorFormat().encoding(outEncoding); 
        outHeader.colorFormat().setOrder(transcodedImage.colorFormat.getOrder());  
        transcodedImage.writeHeader(outHeader);
        
        // Farbconverter setzen
        if(colorFormat.compareTo(transcodedImage.getHeader().colorFormat()) != 0) { 
            switch(colorFormat.getOrder()) {
                case ORDER_BGR -> {
                    colorConverter = ColorFormat::convertBGRtoRBG;
                }
                case ORDER_RBG -> {
                    colorConverter = ColorFormat::convertRBGtoBGR;
                }
            }
        }
        
        // Bild ggfs. analysieren für Kodierungen
        analyzeTranscoding();
        
        // Bildkonvertierung initialisieren
        IDataCodec outCodec = transcodedImage.getCodec();
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
        ||  transcodedImage.getHeader().colorFormat().encoding() != Encoding.NONE) {
            transcodedImage.writeHeader(transcodedImage.getHeader());
        }
        
        return transcodedImage;
    }

    /**
     * Analyse der Codecs durchführen.         
     * Analyse durch Ein- und Ausgabecodec erfordert je nach Kombination
     * eine spezielle Behandlung
     * 
     * @throws IOException 
     */
    private void analyzeTranscoding() throws IOException {
        IDataCodec outCodec = transcodedImage.getCodec();
        
        // Keine Analyse notwendig?
        if( !inCodec.analyzeNecessary(Operation.DECODE)
        &&  !transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            return;
        }

        // Position merken
        long p = position();

        ByteBuffer dataBlock = ByteBuffer.allocate(DataCodec.DEFAULT_BLOCK_SIZE);
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
    public void onData( Event event, 
                        IDataCodec caller, 
                        ByteBuffer block,
                        boolean last) throws IOException {
        switch(event) {
            case DATA_DECODED -> {
                if(caller.getOperation() == Operation.DECODE_ANALYZE) {
                    transcodedImage.getCodec().analyze( block, last);
                } else {
                    
                    // Farben ggfs. konvertieren
                    if(colorConverter != null) {
                        ColorFormat.filterColorBuffer(block, block, colorConverter);
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
    protected IDataCodec createCodec(ImageHeader header) {
        if(header != null) {
            switch(header.colorFormat().encoding()) {
                case NONE -> {
                    return new ImageCodec(this);
                }
                case RLE -> {
                    return new ImageCodecRLE(this);
                }
                case HUFFMAN -> {
                    return new ImageCodecHuffman(this);
                }
            }
        }
        return null;
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
    public static ImageResource createResource( String path, 
                                                String ext,
                                                boolean write) throws IOException {
        switch(ext) {
            case "tga" -> {
                return new ImageResourceTGA(path, write);
            }
            case "propra" -> {
                return new ImageResourceProPra(path, write);
            }

        }
        return null;
    }
    
    /**
     *
     * @return  
     */
    public int getFileHeaderSize() {
        return fileHeaderSize;
    }
    
    /**
     *
     * @return
     */
    public ImageHeader getHeader() {
        return header;
    }
    
    /**
     *
     * @return
     */
    public ColorFormat getColorFormat() {
        return header.colorFormat();
    }

    /**
     *
     * @return
     */
    public IDataCodec getCodec() {
        return inCodec;
    }
    
    /**
     *
     * @return
     */
    public Checksum getChecksum() {
        return checksum;
    }

    /**
     *
     * @param header
     */
    public void setHeader(ImageHeader header) {
        this.header = new ImageHeader(header);
        inCodec = createCodec(header);
    }
}
