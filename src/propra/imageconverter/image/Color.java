package propra.imageconverter.image;

/**
 *
 * @author pg
 */
public class Color {
    /**
     * 
     */
    public static final int BLUE = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    
    protected ChannelInfo[] channel = new ChannelInfo[3];
    
    /**
     * 
     */
    public static class ChannelInfo {
        protected int index;
        protected int mask;
        
        /**
         * 
         * @param shift
         * @param mask 
         */
        ChannelInfo(int index) {
            this.index = index;
        }
        
        /**
         * 
         * @param shift
         * @param mask 
         */
        ChannelInfo(int shift, int mask) {
            this.index = index;
            this.mask = mask;
        }
        
        /**
         *
         * @return
         */
        public int getIndex() {
            return index;
        }
    }

    /**
     * 
     */
    public Color() {
        /**
         * Standard RGB Farben
         */
        setChannel(RED,new ChannelInfo(2));
        setChannel(GREEN,new ChannelInfo(1));
        setChannel(BLUE,new ChannelInfo(0));
    }
    
    
    /**
     * 
     * @param src
     */
    public Color(Color src) {
        /**
         * Standard RGB Farben
         */
        setChannel(RED,src.getChannelInfo(RED));
        setChannel(GREEN,src.getChannelInfo(GREEN));
        setChannel(BLUE,src.getChannelInfo(BLUE));
    }
    
    /**
     * 
     * @param id
     * @param channelInfo
     */
    public void setChannel(int id, ChannelInfo channelInfo) {
        channel[id] = new ChannelInfo(channelInfo.getIndex());
    }
    
    /**
     *
     * @param id
     * @return
     */
    public ChannelInfo getChannelInfo(int id) {
        return channel[id];
    }
    
    /**
     * 
     * @param data
     * @param colorInfo
     * @return 
     */
    public byte[] convertColor(byte[] input, Color colorInfo) {
        if (input == null) {
            throw new IllegalArgumentException();
        }   
        
        byte[] v = new byte[3];
        v[RED] = input[colorInfo.getChannelInfo(RED).getIndex()];
        v[GREEN] = input[colorInfo.getChannelInfo(GREEN).getIndex()];
        v[BLUE] = input[colorInfo.getChannelInfo(BLUE).getIndex()];
        return v;
    }
}
