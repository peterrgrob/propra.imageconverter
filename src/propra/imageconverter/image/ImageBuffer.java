package propra.imageconverter.image;

/**
 *
 * @author pg
 */
public class ImageBuffer {
    /**
     * 
     */
    private ImageInfo info; 
    private Color[] buffer;    
  
    /**
     * 
     */
    ImageBuffer() {
        
    }
    
    /**
     * 
     * @param info 
     */
    ImageBuffer(ImageInfo info) {
        createBuffer(info);
    }
    
    /**
     * 
     * @param info
     */
    public void createBuffer(ImageInfo info) {
        this.info = info;
        buffer = new Color[info.getElementCount()];
    }
    
    /**
     * 
     * @return 
     */
    public ImageInfo getInfo() {
        return info;
    }
    
    /**
     * 
     * @param x
     * @param y
     * @param color 
     */
    public void set(int x, int y, Color color) {
        
    }
    
    /**
     * 
     * @param offset
     * @param color 
     */
    public void set(int offset, Color color) {
        if(offset > info.getTotalSize()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        buffer[offset] = color;
    }
    
    /**
     * 
     * @param offset
     * @return 
     */
    public Color get(int offset) {
        if(offset < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return buffer[offset];
    }
}
