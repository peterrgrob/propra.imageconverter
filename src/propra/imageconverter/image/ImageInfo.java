package propra.imageconverter.image;

/**
 *
 * @author pg
 */
public class ImageInfo {
    /**
     * 
     */
    public enum Encoding {
        UNCOMPRESSED,
    }
    
    private int width;
    private int height;
    private int elementSize;    
    private Encoding encoding;
    private int checksum;
    private Color colorType = new Color();

    /**
     * 
     */
    public ImageInfo() {
    }

    /**
     *
     * @param src
     */
    public ImageInfo(ImageInfo src) {
        this.width = src.width;
        this.height = src.height;
        this.elementSize = src.elementSize;
        this.encoding = src.encoding;
        this.checksum = src.checksum;
        this.colorType = new Color(src.getColorType());
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return (    width > 0 
                &&  height > 0 
                &&  elementSize > 0);
    }
    
    /**
     * Get the value of encoding
     *
     * @return the value of encoding
     */
    public Encoding getEncoding() {
        return encoding;
    }

    /**
     * Set the value of encoding
     *
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
     * Get the value of colorType
     *
     * @return the value of colorType
     */
    public Color getColorType() {
        return colorType;
    }

    /**
     * Set the value of colorType
     *
     * @param colorType new value of colorType
     */
    public void setColorType(Color colorType) {
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
     * Get the value of elementSize
     *
     * @return the value of elementSize
     */
    public int getElementSize() {
        return elementSize;
    }

    /**
     * Set the value of elementSize
     *
     * @param elementSize new value of elementSize
     */
    public void setElementSize(int elementSize) {
        this.elementSize = elementSize;
    }

    /**
     * Get the value of checksum
     *
     * @return the value of checksum
     */
    public int getChecksum() {
        return checksum;
    }

    /**
     * Set the value of checksum
     *
     * @param checksum new value of checksum
     */
    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }
    
    /**
     * Get the value of height
     *
     * @return the value of height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set the value of height
     *
     * @param height new value of height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the value of width
     *
     * @return the value of width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the value of width
     *
     * @param width new value of width
     */
    public void setWidth(int width) {
        this.width = width;
    }

}
