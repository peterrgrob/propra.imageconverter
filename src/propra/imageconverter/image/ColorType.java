package propra.imageconverter.image;

/**
 *
 * @author pg
 */
public class ColorType implements Comparable<ColorType> {
    /**
     * 
     */
    public static final int BLUE = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    protected ColorChannel[] channel = new ColorChannel[3];

    /**
     * 
     */
    public ColorType() {
        /**
         * Standard RGB Farben
         */
        setChannel(RED,new ColorChannel(2));
        setChannel(GREEN,new ColorChannel(1));
        setChannel(BLUE,new ColorChannel(0));
    }
    
    
    /**
     * 
     * @param src
     */
    public ColorType(ColorType src) {
        /**
         * Standard RGB Farben
         */
        setChannel(RED,src.getChannelInfo(RED));
        setChannel(GREEN,src.getChannelInfo(GREEN));
        setChannel(BLUE,src.getChannelInfo(BLUE));
    }
    
    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(ColorType o) {
        if( channel[0] == o.getChannelInfo(0) &&
            channel[1] == o.getChannelInfo(1) &&
            channel[2] == o.getChannelInfo(2)) {
            return 0;
        }
        return -1;
    }
    
    /**
     * 
     * @param id
     * @param channelInfo
     */
    public void setChannel(int id, ColorChannel channelInfo) {
        channel[id] = new ColorChannel(channelInfo.getIndex());
    }
    
    /**
     * 
     * @param id
     * @param index
     */
    public void setChannel(int id, int index) {
        channel[id] = new ColorChannel(index);
    }
    
    /**
     *
     * @param id
     * @return
     */
    public ColorChannel getChannelInfo(int id) {
        return channel[id];
    }
    
    /**
     * 
     * @param input
     * @param colorInfo
     * @return 
     */
    public byte[] convertColor(byte[] input, ColorType colorInfo) {
        if ( input == null) {
            throw new IllegalArgumentException();
        }  
        byte t0,t1,t2;
        
        t2 = input[colorInfo.getChannelInfo(RED).getIndex()];
        t1 = input[colorInfo.getChannelInfo(GREEN).getIndex()];
        t0 = input[colorInfo.getChannelInfo(BLUE).getIndex()];
        
        input[RED] = t2;
        input[GREEN] = t1;
        input[BLUE] = t0;
                
        return input;
    }
    
    /**
     *
     * @param color
     * @return
     */
    public static byte[] switchEndian(byte[] color) {
        if (color == null) {
            throw new IllegalArgumentException();
        }
        byte tmp = color[0];
        color[0] = color[2];
        color[2] = tmp;
        return color;
    }
    
    /**
     *
     * @param src
     * @param dst
     * @return
     */
    public static byte[] toLittleEndian(byte[] src, byte[] dst) {
        if (src == null || dst == null) {
            throw new IllegalStateException();
        }
        
        dst[0] = src[2];
        dst[1] = src[1];
        dst[2] = src[0];
        return dst;
    }
}
