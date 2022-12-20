package propra.imageconverter.image;

import propra.imageconverter.image.huffman.ImageCodecHuffman;
import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.data.DataCodec;
import propra.imageconverter.data.DataFormat.Encoding;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataCodec;
import propra.imageconverter.data.IDataListener;

/**
 *
 * @author pg
 */
public abstract class ImageResource extends DataResource 
                                    implements IDataListener {
    
    // Größe des Bildheaders in der Datei
    protected int fileHeaderSize; 
    
    // Logischer Bildheader
    protected ImageHeader header;
    
    // Farbformat
    protected ColorFormat colorFormat;
    
    // Konvertiertes Bild
    protected ImageResource transcodedImage;

    /**
     * 
     */
    public ImageResource(   String file,
                            boolean write) throws IOException {
        super(file, write);
        colorFormat = new ColorFormat();
    }
    
    /**
     * 
     */
    abstract public ImageHeader readHeader() throws IOException;
    
    /**
     * 
     * @param srcHeader
     * @throws IOException 
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        // Prüfsumme aktualisieren und Header speichern
        if(checksum != null) {
            header.checksum(checksum.getValue());
        }
        setHeader(srcHeader);
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
    public ImageResource transcode( String outFile, 
                                    String ext, 
                                    Encoding outEncoding) throws IOException {
        if(outFile == null) {
            throw new IllegalArgumentException();
        }
        
        // Neues Bild erstellen
        transcodedImage = createResource(outFile, ext, true);
        if(transcodedImage == null) {
            throw new IllegalArgumentException("Nicht unterstütztes Dateiformat!");
        }        
        
        // Bilddaten einlesen
        readHeader();
        
        // Neuen Header schreiben
        ImageHeader outHeader = new ImageHeader(header);
        outHeader.colorFormat().encoding(outEncoding);    
        transcodedImage.writeHeader(outHeader);
        
        // Bild ggfs. analysieren für Kodierungen
        analyze();
        
        // Bildkonvertierung initialisieren
        inCodec.begin(Operation.DECODE);
        transcodedImage.getCodec().begin(Operation.ENCODE);
        
        // Dekodierung starten
        inCodec.decode(this);
        
        // Bildkonvertierung abschließen
        inCodec.end();
        transcodedImage.getCodec().end();
        
        //  Falls nötig Header mit Prüfsumme, oder Länge des komprimierten Datensegements 
        //  aktualisieren
        if( transcodedImage.getChecksum() != null
        ||  transcodedImage.getHeader().colorFormat().encoding() != Encoding.NONE) {
            transcodedImage.writeHeader(transcodedImage.getHeader());
        }
        
        return transcodedImage;
    }

    /**
     * Bild durch die Codecs analysieren
     * 
     * @throws IOException 
     */
    private void analyze() throws IOException {
        if( !inCodec.analyzeNecessary(Operation.DECODE)
        &&  !transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            return;
        }
        
        // Position merken
        long p = position();
        
        ByteBuffer dataBlock = ByteBuffer.allocate(DataCodec.DEFAULT_BLOCK_SIZE);
        
        inCodec.begin(Operation.DECODER_ANALYZE);
        transcodedImage.getCodec().begin(Operation.ENCODER_ANALYZE);
        
        /*
         *  Analyse durch Ein- und Ausgabecodec erfordert je nach Kombination
         *  eine spezielle Behandlung
         */
        if( inCodec.analyzeNecessary(Operation.DECODE)
        &&  transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            
            // Durchlauf für Decoder-Analyse 
            while(position() < length()) {
                getInputStream().read(dataBlock);
                inCodec.analyze(dataBlock, false);
            }
            
            // Ursprüngliche Position wiederherstellen
            position(p);
            
            // Durchlauf mit Dekodierung und Encoder-Analyse 
            while(position() < length()) {
                inCodec.decode(this);
            } 
        } else if(inCodec.analyzeNecessary(Operation.DECODE)) {
            
            // Durchlauf für Decoder-Analyse 
            while(position() < length()) {
                getInputStream().read(dataBlock);
                inCodec.analyze(dataBlock, false);
            }
        } else if(transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            
            // Durchlauf mit Dekodierung und Encoder-Analyse 
            while(position() < length()) {
                inCodec.decode(this);
            } 
        }
        
        inCodec.end();
        transcodedImage.getCodec().end();
        
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
            case DATA_BLOCK_DECODED -> {
                if(caller.getOperation() == Operation.DECODER_ANALYZE) {
                    transcodedImage.getCodec().analyze( block, last);
                } else {
                    
                    // Farben ggfs. konvertieren
                    if(colorFormat.compareTo(transcodedImage.getHeader().colorFormat()) != 0) { 
                        ColorBuffer cb = new ColorBuffer(block, colorFormat);
                        cb.convert(cb,
                                              transcodedImage.getHeader().colorFormat());
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
    public static ImageResource createResource(String path, 
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
     */
    public int getFileHeaderSize() {
        return fileHeaderSize;
    }
    
    public ImageHeader getHeader() {
        return header;
    }

    public IDataCodec getCodec() {
        return inCodec;
    }
    
    public Checksum getChecksum() {
        return checksum;
    }

    public void setHeader(ImageHeader header) {
        this.header = new ImageHeader(header);
        inCodec = createCodec(header);
    }
}
