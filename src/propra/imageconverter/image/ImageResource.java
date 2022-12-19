package propra.imageconverter.image;

import propra.imageconverter.image.huffman.ImageCodecHuffman;
import java.io.IOException;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.data.DataBlock;
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
     */
    public void writeHeader(ImageHeader srcHeader) throws IOException {
        setHeader(srcHeader);
    }
    
    /**
     *  Konvertiert ein Bild in ein neues Bild mit gegebenem Format und 
     *  Kodierung
     */
    public ImageResource transcode( String outFile, 
                                    String ext, 
                                    Encoding outEncoding) throws IOException {
        if(outFile == null) {
            throw new IllegalArgumentException();
        }
        
        // Neues Bild erstellen
        transcodedImage = createImageResource(outFile, ext, true);
        if(transcodedImage == null) {
            return null;
        }        
        
        // Bildkopf einlesen und mit neuem Format in Ausgabedatei schreiben
        readHeader();
        
        ImageHeader outHeader = new ImageHeader(header);
        outHeader.colorFormat().encoding(outEncoding);    
        transcodedImage.writeHeader(outHeader);
        
        // Bild durch Codecs analysieren
        analyze();
        
        // Bildverarbeitung initialisieren
        inCodec.begin(Operation.DECODE);
        transcodedImage.getCodec().begin(Operation.ENCODE);
        
        // Dekodierung starten
        inCodec.decode(new DataBlock(), this);
        
        // Bildverarbeitung abschließen
        inCodec.end();
        transcodedImage.getCodec().end();
        
        if(transcodedImage.getChecksum() != null) {
            transcodedImage.getHeader().checksum(transcodedImage.getChecksum().getValue());
        }
        
        //  Falls nötig Header mit Prüfsumme, oder Länge des komprimierten Datensegements 
        //  aktualisieren
        if( transcodedImage.getChecksum() != null
        ||  transcodedImage.getHeader().colorFormat().encoding() != Encoding.NONE) {
            transcodedImage.writeHeader(transcodedImage.getHeader());
        }
        
        return transcodedImage;
    }

    /**
     *  Bild in Blöcken durch Codecs analysieren
     */
    private void analyze() throws IOException {
        if( !inCodec.analyzeNecessary(Operation.DECODE)
        &&  !transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            return;
        }
        
        // Position merken
        long p = position();
        
        DataBlock dataBlock = new DataBlock(DataCodec.DEFAULT_BLOCK_SIZE);
        
        inCodec.begin(Operation.DECODER_ANALYZE);
        transcodedImage.getCodec().begin(Operation.ENCODER_ANALYZE);
        
        /*
         *  Analyse durch Ein- und Ausgabecodec erfordert eine spezielle Behandlung
         */
        if( inCodec.analyzeNecessary(Operation.DECODE)
        &&  transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            
            // Durchlauf für Decoder-Analyse 
            while(position() < length()) {
                getInputStream().read(dataBlock.data);
                inCodec.analyze(dataBlock);
            }
            
            // Ursprüngliche Position wiederherstellen
            position(p);
            
            // Durchlauf mit Dekodierung und Encoder-Analyse 
            while(position() < length()) {
                inCodec.decode(dataBlock, this);
            } 
        } else if(inCodec.analyzeNecessary(Operation.DECODE)) {
            
            // Durchlauf für Decoder-Analyse 
            while(position() < length()) {
                getInputStream().read(dataBlock.data);
                inCodec.analyze(dataBlock);
            }
        } else if(transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            
            // Durchlauf mit Dekodierung und Encoder-Analyse 
            while(position() < length()) {
                inCodec.decode(dataBlock, this);
            } 
        }
        
        inCodec.end();
        transcodedImage.getCodec().end();
        
        // Ursprüngliche Position wiederherstellen
        position(p);
    }
    
    /**
     * 
     */
    @Override
    public void onData( Event event, 
                        IDataCodec caller, 
                        DataBlock block) throws IOException {
        switch(event) {
            case DATA_BLOCK_DECODED -> {
                if(caller.getOperation() == Operation.DECODER_ANALYZE) {
                    transcodedImage.getCodec().analyze( block);
                } else {
                    transcodedImage.getCodec().encode(  block, 
                                                        transcodedImage);
                }
            }
        }
    }
    
    /**
     * 
     */
    protected IDataCodec createImageCodec(ImageHeader header) {
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
     *  Erstellt ein ImageResource Objekt basierend auf Dateipfad
     */
    public static ImageResource createImageResource(String path, 
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
        inCodec = createImageCodec(header);
    }
}
