package propra.imageconverter.image;

import java.io.IOException;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.DataFormat.Encoding;
import propra.imageconverter.data.DataFormat.IOMode;
import propra.imageconverter.data.DataResource;
import propra.imageconverter.data.IDataCodec;

/**
 *
 * @author pg
 */
public abstract class ImageResource extends DataResource {
    
    protected int fileHeaderSize;   
    protected ImageMeta header;
    protected ColorFormat colorFormat;
    protected IDataCodec inCodec;
    protected Checksum checksum;

    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public ImageResource(   String file, 
                            DataFormat.IOMode mode) throws IOException {
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
     * @param srcHeader
     * @throws IOException 
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
                case NONE:
                    return new ImageCodecRaw(this, null);
                case RLE:
                    return new ImageCodecRLE(this, null);

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
        
        ImageResource outImage = createImageResource(outFile, ext);
        if(outImage == null) {
            return null;
        }
        
        readHeader();
        
        ImageMeta outHeader = new ImageMeta(header);
        outHeader.colorFormat().encoding(outEncoding);    
        outImage.writeHeader(outHeader);
       
        // Bilddaten verarbeiten
        DataBlock dataBlock = new DataBlock();
        inCodec.begin(DataFormat.Operation.READ);
        outImage.getCodec().begin(DataFormat.Operation.WRITE);
        inCodec.decode(dataBlock, outImage.getCodec());
        inCodec.end();
        outImage.getCodec().end();
        
        // Falls nötig Header aktualisieren
        if( outImage.getChecksum() != null
        ||  outImage.getHeader().colorFormat().encoding() == DataFormat.Encoding.RLE) {
            outImage.writeHeader(outImage.getHeader());
        }
        
        return outImage;
    }
}
