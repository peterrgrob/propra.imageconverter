package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
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
public abstract class ImageResource extends DataResource implements IDataListener {
    
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
                    IOMode mode) throws IOException {
        super(file, mode);
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
    private IDataCodec createImageCodec(ImageMeta header) {
        if(header != null) {
            switch(header.colorFormat().encoding()) {
                case NONE -> {
                    return new ImageCodecRaw(this);
                }
                case RLE -> {
                    return new ImageCodecRLE(this);
                }

            }
        }
        return null;
    }
    
    /**
     *  Erstellt ein ImageResource Objekt basierend auf Dateipfad
     */
    private static ImageResource createImageResource(String path, String ext) throws IOException {
        switch(ext) {
            case "tga" -> {
                return new ImageResourceTGA(path, IOMode.BINARY);
            }
            case "propra" -> {
                return new ImageResourceProPra(path, IOMode.BINARY);
            }

        }
        return null;
    }
    
    /**
     * 
     */
    public ImageResource transcode( String outFile, 
                                    String ext, 
                                    Encoding outEncoding) throws IOException {
        if(outFile == null) {
            throw new IllegalArgumentException();
        }
        
        transcodedImage = createImageResource(outFile, ext);
        if(transcodedImage == null) {
            return null;
        }        
        Checksum transcodedChecksum = transcodedImage.getChecksum();
        
        readHeader();
        
        ImageMeta outHeader = new ImageMeta(header);
        outHeader.colorFormat().encoding(outEncoding);    
        transcodedImage.writeHeader(outHeader);
       
        // Bilddaten verarbeiten
        DataBlock dataBlock = new DataBlock();
        
        // Transcoding vorbereiten
        if(checksum != null) {
            checksum.begin();
        }
        if(transcodedChecksum != null) {
            transcodedChecksum.begin();
        }
        inCodec.begin(Operation.READ);
        transcodedImage.getCodec().begin(Operation.WRITE);
        
        // Dekodierung starten
        inCodec.decode(dataBlock, this);
        
        // Transcoding abschließen
        inCodec.end();
        transcodedImage.getCodec().end();
        if(checksum != null) {
            checksum.end();
        }
        if(transcodedChecksum != null) {
            transcodedChecksum.end();
        }
        
        // Falls nötig Header aktualisieren
        if( transcodedChecksum != null
        ||  transcodedImage.getHeader().colorFormat().encoding() == Encoding.RLE) {
            transcodedImage.writeHeader(transcodedImage.getHeader());
        }
        
        return transcodedImage;
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
