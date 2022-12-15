package propra.imageconverter.image;

import java.io.IOException;
import java.nio.channels.Channels;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.DataFormat.Encoding;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.DataInputStream;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataCodec;
import propra.imageconverter.data.IDataListener;

/**
 *
 * @author pg
 */
public abstract class ImageResource extends DataResource 
                                    implements IDataListener {
    
    protected int fileHeaderSize;   
    protected ImageMeta header;
    protected ColorFormat colorFormat;
    protected IDataCodec inCodec;
    protected Checksum checksum;
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
    @Override
    public boolean isValid() {
        return super.isValid();
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
    public ImageMeta getHeader() {
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
    public void setHeader(ImageMeta header) {
        this.header = new ImageMeta(header);
        inCodec = createImageCodec(header);
    }
    
    /**
     * 
     */
    abstract public ImageMeta readHeader() throws IOException;
    
    /**
     * 
     */
    public void writeHeader(ImageMeta srcHeader) throws IOException {
        setHeader(srcHeader);
    }
    
    /**
     *  
     */
    @Override
    public DataInputStream getBufferedInput() {
        checkState();
        return new DataInputStream(Channels.newInputStream(binaryFile.getChannel()), checksum);
    }
    
    /**
     * 
     */
    protected IDataCodec createImageCodec(ImageMeta header) {
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
        Checksum transcodedChecksum = transcodedImage.getChecksum();
        
        
        // Bildkopf einlesen und mit neuem Format in Ausgabedatei schreiben
        readHeader();
        
        ImageMeta outHeader = new ImageMeta(header);
        outHeader.colorFormat().encoding(outEncoding);    
        transcodedImage.writeHeader(outHeader);
        
        // Konvertierung vorbereiten
        if(checksum != null) {
            checksum.begin();
        }
        if(transcodedChecksum != null) {
            transcodedChecksum.begin();
        }
        
        // Bild analysieren
        analyze();
        
        // Bilddaten verarbeiten
        inCodec.begin(Operation.DECODE);
        transcodedImage.getCodec().begin(Operation.ENCODE);
        
        // Dekodierung starten
        inCodec.decode(new DataBlock(), this);
        
        // Konvertierung abschließen
        inCodec.end();
        transcodedImage.getCodec().end();
        if(checksum != null) {
            checksum.end();
        }
        if(transcodedChecksum != null) {
            transcodedChecksum.end();
        }
        
        //  Falls nötig Header mit Prüfsumme, oder Länge des komprimierten Datensegements 
        //  aktualisieren
        if( transcodedChecksum != null
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
