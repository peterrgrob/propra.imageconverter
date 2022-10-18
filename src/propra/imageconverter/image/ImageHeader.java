package propra.imageconverter.image;

/**
 * Klasse für einen allgemeinen Bildkopf
 * 
 * @author pg
 */
public class ImageHeader {
    
    private int width;
    private int height;
    private int elementSize;    
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
        this.elementSize = src.elementSize;
        this.encoding = src.encoding;
        this.checksum = src.checksum;
        this.colorType = new ColorFormat(src.getColorType());
    }
    
    /**
     * 
     * @return true, wenn zulässiger Bildkopf vorliegt.
     */
    public boolean isValid() {
        return (    width > 0 
                &&  height > 0 
                &&  elementSize == 3);
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
    public int getTotalSize() {
        return width * height * elementSize;
    }
    
    /**
     * @return the value of colorType
     */
    public ColorFormat getColorType() {
        return colorType;
    }

    /**
     * @param colorType new value of colorType
     */
    public void setColorType(ColorFormat colorType) {
        this.colorType = colorType;
    }
    
    /**
     * 
     * @return 
     */
    public int getElementCount() {
        return width * height;
    }
    
    /**
     * @return the value of elementSize
     */
    public int getElementSize() {
        return elementSize;
    }

    /**
     * @param elementSize new value of elementSize
     */
    public void setElementSize(int elementSize) {
        this.elementSize = elementSize;
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
