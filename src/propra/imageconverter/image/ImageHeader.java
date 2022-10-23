package propra.imageconverter.image;

import propra.imageconverter.util.Validatable;

/**
 * Klasse für einen allgemeinen Bildkopf
 * 
 * @author pg
 */
public class ImageHeader implements Validatable {
    
    private int width;
    private int height;
    private int pixelSize;    
    private Encoding encoding;
    private long checksum;
    private ColorFormat colorType = new ColorFormat();

    public enum Encoding {
        UNCOMPRESSED,
    }
        
    /**
     * 
     */
    public ImageHeader() {
    }

    /**
     *
     * @param src
     */
    public ImageHeader(ImageHeader src) {
        if (src == null) {
            throw new IllegalArgumentException();
        }
        
        this.width = src.width;
        this.height = src.height;
        this.pixelSize = src.pixelSize;
        this.encoding = src.encoding;
        this.checksum = src.checksum;
        this.colorType = new ColorFormat(src.getColorFormat());
    }
    
    /**
     * 
     * @return true, wenn zulässiger Bildkopf vorliegt.
     */
    public boolean isValid() {
        return (    width > 0 
                &&  height > 0 
                &&  pixelSize == 3);
    }
    
    /**
     * @return the value of encoding
     */
    public Encoding getEncoding() {
        return encoding;
    }

    /**
     * @param encoding new value of encoding
     */
    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }
    
    /**
     * 
     * @return 
     */
    public int getImageSize() {
        return width * height * pixelSize;
    }
    
    /**
     * @return the value of colorType
     */
    public ColorFormat getColorFormat() {
        return colorType;
    }

    /**
     * @param colorType new value of colorType
     */
    public void setColorFormat(ColorFormat colorType) {
        this.colorType = colorType;
    }
    
    /**
     * 
     * @return 
     */
    public int getPixelCount() {
        return width * height;
    }
    
    /**
     * @return the value of pixelSize
     */
    public int getPixelSize() {
        return pixelSize;
    }

    /**
     * @param elementSize new value of pixelSize
     */
    public void setPixelSize(int elementSize) {
        this.pixelSize = elementSize;
    }

    /**
     * @return the value of checksum
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     * @param checksum new value of checksum
     */
    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }
    
    /**
     * @return the value of height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height new value of height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the value of width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width new value of width
     */
    public void setWidth(int width) {
        this.width = width;
    }

}
