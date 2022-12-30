package propra.imageconverter.image;

import propra.imageconverter.data.IDataTranscoder.Compression;
import propra.imageconverter.image.Color.Format;

/**
 * Klasse für eine allgemeine Bildattribute
 */
public class ImageAttributes {
    
    // Bildattribute
    private int width;
    private int height; 
    private long dataLength;
    private long checksum;
    private Compression encoding;
    private Format format;

    public ImageAttributes() {
        format = Color.Format.COLOR_BGR;
        encoding = Compression.UNCOMPRESSED;
    }
    
    /**
     * Copyconstructor
     */
    public ImageAttributes(ImageAttributes src) {
        if (src == null) {
            throw new IllegalArgumentException();
        }
        
        this.width = src.width;
        this.height = src.height;
        this.checksum = src.checksum;
        this.dataLength = src.dataLength;
        this.encoding = src.encoding;
        this.format = src.format;
    }

    /**
     * Gibt Attribute als String zurück
     */
    @Override
    public String toString() {
        String stateString;
        stateString = "" + width;
        stateString = stateString.concat("x" + height);
        stateString = stateString.concat("x" + (Color.PIXEL_SIZE*8));
        stateString = stateString.concat(" (" + format.toString());  
        stateString = stateString.concat(", " + encoding.toString()+")");
          return stateString;
    }
    
    /*
     *  Getter/Setter
     */
    public int getPixelCount() {
        return width * height;
    }
    
    public int getImageSize() {
        return width * height * Color.PIXEL_SIZE;
    }

    public Compression getCompression() {
        return encoding;
    }

    public void setCompression(Compression encoding) {
        this.encoding = encoding;
    }
    
    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }
    
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public long getDataLength() {
        return dataLength;
    }

    public void setDataLength(long encodedSize) {
        this.dataLength = encodedSize;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }
}
