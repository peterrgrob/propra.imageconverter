package propra.imageconverter.image;

/**
 *
 * @author pg
 */
public class Color {
    public byte r;
    public byte g;
    public byte b;
    
    /**
     * 
     */
    public static class ColorOrder {
        public int redShift = 2;
        public int greenShift = 1;
        public int blueShift = 0;
    }

    /**
     * 
     */
    public Color() {
    }

    /**
     * 
     * @param r
     * @param g
     * @param b 
     */
    public Color(byte r, byte g, byte b) {
        set(r,g,b);
    }
    
    /**
     * 
     * @param r
     * @param g
     * @param b 
     */
    public void set(byte r, byte g, byte b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
    
    /**
     * 
     * @return 
     */
    protected int getRaw() {
        return buildRGB(this);
    }
    
    /**
     * 
     * @param c
     * @return 
     */
    public static int buildRGB(Color c) {
        return ((c.r << 16) | (c.g << 8) | c.b);
    }
    
    /**
     * 
     * @param c
     * @return 
     */
    public static int buildBGR(Color c) {
        return ((c.b << 16) | (c.g << 8) | c.r);
    }
    
    /**
     *
     * @param buff
     * @param offset
     * @param c
     * @return
     */
    public static byte[] buildBGR(byte[] buff, int offset, Color c) {
        buff[offset+0] = c.b;
        buff[offset+1] = c.g;
        buff[offset+2] = c.r;
        return buff;
    }
    
    /**
     *
     * @param buff
     * @param offset
     * @param c
     * @return
     */
    public static byte[] buildRGB(byte[] buff, int offset, Color c) {
        buff[offset+2] = c.b;
        buff[offset+1] = c.g;
        buff[offset+0] = c.r;
        return buff;
    }
}
