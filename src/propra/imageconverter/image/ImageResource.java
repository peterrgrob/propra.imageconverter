package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.checksum.ChecksumPropra;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.DataFormat.Encoding;
import propra.imageconverter.data.DataFormat.IOMode;
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
                            IOMode mode,
                            boolean write) throws IOException {
        super(file, mode, write);
        colorFormat = new ColorFormat();
    }
    
    /**
     *
     */
    public int getFileHeaderSize() {
        return fileHeaderSize;
    }
    
    /**
     *
     */
    public ImageHeader getHeader() {
        return header;
    }
    
    /**
     * 
     */
    public IDataCodec getCodec() {
        return inCodec;
    }
    
    /**
     * 
     */
    public Checksum getChecksum() {
        return checksum;
    }
    
    /**
     * 
     */
    public void setHeader(ImageHeader header) {
        this.header = new ImageHeader(header);
        inCodec = createImageCodec(header);
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
     * 
     */
    protected IDataCodec createImageCodec(ImageHeader header) {
        if(header != null) {
            switch(header.colorFormat().encoding()) {
                case NONE -> {
                    return new ImageCodecRaw(this);
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
                return new ImageResourceTGA(path, IOMode.BINARY, write);
            }
            case "propra" -> {
                return new ImageResourceProPra(path, IOMode.BINARY, write);
            }

        }
        return null;
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
        
        // Bild analysieren
        analyze();
        
        if(checksum != null) {
            checksum.reset();
        }
        if(transcodedImage.getChecksum() != null) {
            transcodedImage.getChecksum().reset();
        }
        
        // Bildverarbeitung initialisieren
        inCodec.begin(Operation.DECODE);
        transcodedImage.getCodec().begin(Operation.ENCODE);
        
        // Dekodierung starten
        inCodec.decode(new DataBlock(), this);
        
        // Bildverarbeitung abschließen
        inCodec.end();
        transcodedImage.getCodec().end();
        
        if(checksum != null) {
            header.checksum(checksum.getValue());
        }
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
        
        if( inCodec.analyzeNecessary(Operation.DECODE)
        ||  transcodedImage.getCodec().analyzeNecessary(Operation.ENCODE)) {
            
            // Position merken
            long p = position();
            
            inCodec.begin(Operation.ANALYZE_DECODER);
            transcodedImage.getCodec().begin(Operation.ANALYZE_ENCODER);
            DataBlock dataBlock = new DataBlock(DataCodecRaw.DEFAULT_BLOCK_SIZE);
            
            // Daten in Blöcken durchlaufen und an Codec geben
            while(position() < length()) {
                read(dataBlock.data);
                inCodec.analyze(dataBlock);
                transcodedImage.getCodec().analyze(dataBlock);
            }

            inCodec.end();
            transcodedImage.getCodec().end();
                
            // Ursprüngliche Position wiederherstellen
            position(p);
        }
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
                transcodedImage.getCodec().encode(  block, 
                                                    transcodedImage);
            }
            case DATA_IO_READ, DATA_IO_WRITE  -> {
                if(checksum != null) {
                    checksum.update(block.data);
                }
            }
        }
    }
}
