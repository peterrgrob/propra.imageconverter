package propra.imageconverter.image;

import propra.imageconverter.data.DataResource.Compression;
import propra.imageconverter.image.Color.Format;

/**
 * Klasse für einen allgemeine Bildinformationen
 */
public class ImageAttributes {
    
    // Bildattribute
    private int width;
    private int height; 
    private long dataLength;
    private long checksum;
    private Compression encoding;
    private Format format;

    /**
     * 
     */
    public ImageAttributes() {
        format = Color.Format.COLOR_BGR;
        encoding = Compression.NONE;
    }
    
    /**
     *
     * @param src
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
     *
     * @return
     */
    public int getPixelCount() {
        return width * height;
    }
    
    /**
     * 
     * @return 
     */
    public int getImageSize() {
        return width * height * Color.PIXEL_SIZE;
    }

    /**
     * 
     * @return 
     */
    public Compression getCompression() {
        return encoding;
    }

    /**
     * 
     * @param encoding 
     */
    public void setCompression(Compression encoding) {
        this.encoding = encoding;
    }
    
    /**
     *
     * @return
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     *
     * @param checksum
     */
    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }
    
    /**
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     *
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     *
     * @return
     */
    public long getDataLength() {
        return dataLength;
    }

    /**
     *
     * @param encodedSize
     */
    public void setDataLength(long encodedSize) {
        this.dataLength = encodedSize;
    }

    /**
     * 
     * @return 
     */
    public Format getFormat() {
        return format;
    }

    /**
     * 
     * @param format 
     */
    public void setFormat(Format format) {
        this.format = format;
    }
}
