package propra.imageconverter.image;

/**
 *
 * @author pg
 */
public class Color implements Comparable<Color> {
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
    public static class ChannelInfo implements Comparable<ChannelInfo>{
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
        ChannelInfo(int index, int mask) {
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

        /**
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(ChannelInfo o) {
            if (index == o.index &&
                mask == o.mask) {
                return 0;
            }
            return -1;
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
     * @param o
     * @return
     */
    @Override
    public int compareTo(Color o) {
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
    public void setChannel(int id, ChannelInfo channelInfo) {
        channel[id] = new ChannelInfo(channelInfo.getIndex());
    }
    
    /**
     * 
     * @param id
     * @param index
     */
    public void setChannel(int id, int index) {
        channel[id] = new ChannelInfo(index);
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
     * @param input
     * @param colorInfo
     * @param output
     * @return 
     */
    public byte[] convertColor(byte[] input, Color colorInfo, byte[] output) {
        if ( input == null
          || output == null ) {
            throw new IllegalArgumentException();
        }   
        output[RED] = input[colorInfo.getChannelInfo(RED).getIndex()];
        output[GREEN] = input[colorInfo.getChannelInfo(GREEN).getIndex()];
        output[BLUE] = input[colorInfo.getChannelInfo(BLUE).getIndex()];
        return output;
    }
}
