package propra.imageconverter.image;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 *
 * @author pg
 */
public class ColorBuffer implements Iterable<Color> {
    
    // Interner Puffer
    private ByteBuffer dataBuffer;
    
    // Pixelformat
    private ColorFormat format;
    
    /**
     * 
     */
    public ColorBuffer() {
    }
    
    /**
     * 
     * @param data
     * @param format 
     */
    public ColorBuffer( byte[] data,
                        ColorFormat format) {
        dataBuffer = ByteBuffer.wrap(data);
        this.format = new ColorFormat(format);
    }
    
    /**
     * 
     * @param data
     * @param format 
     */
    public ColorBuffer( ByteBuffer data,
                        ColorFormat format) {
        dataBuffer = data;
        this.format = new ColorFormat(format);
    }
    
    /**
     * 
     * @param data
     * @param format 
     */
    public ColorBuffer( int size,
                        ColorFormat format) {
        dataBuffer = ByteBuffer.allocate(size * ColorFormat.PIXEL_SIZE);
        this.format = new ColorFormat(format);
    }
    
    /**
     *  Füllt Buffer len-mal mit Farbwert und gibt neuen Offset zurück
     */
    public int fill(Color color, int len) {
        /*byte r = color[0];
        byte g = color[1];
        byte b = color[2];

        for(int i=0;i<len;i++) {
            array[offset++] = r;
            array[offset++] = g;
            array[offset++] = b; 
        }
        return offset;*/
        
        for(int i=0; i<len; i++){
            put(color);
        }
        
        return dataBuffer.position();
    }
    
    /**
     * 
     * @param offset1
     * @param offset2
     * @return 
     */
    public boolean compareColor(int offset1, int offset2) {
        byte[] array = dataBuffer.array();
        return (array[offset1 + 0] == array[offset2 + 0]
            &&  array[offset1 + 1] == array[offset2 + 1]
            &&  array[offset1 + 2] == array[offset2 + 2]);
    }
    
    /**
     * 
     * @return 
     */
    public ByteBuffer getBuffer() {
        return dataBuffer;
    }
    
    /**
     * 
     * @return 
     */
    public byte[] array() {
        return dataBuffer.array();
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public ColorBuffer get(Color p) {
        dataBuffer.get(p.get());
        return this;
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public ColorBuffer put(Color p) {
        dataBuffer.put(p.get());
        return this;
    }
    
    /**
     * 
     * @param buff
     * @return 
     */
    public ColorBuffer put(ColorBuffer buff) {
        dataBuffer.put(buff.getBuffer());
        return this;
    }

    /**
     * 
     * @return 
     */
    @Override
    public Iterator<Color> iterator() {
        Iterator<Color> it = new Iterator<Color>() {
            /**
             * 
             * @return 
             */
            @Override
            public boolean hasNext() {
                return dataBuffer.hasRemaining();
            }
            
            /**
             * 
             * @return 
             */
            @Override
            public Color next() {
                Color p = new Color(dataBuffer.array(),
                                    dataBuffer.position());
                dataBuffer.position(dataBuffer.position() + 1);
                return p;
            }

            /**
             * 
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
    }    
    
    /**
     * 
     */
    public int capacity() {
        return dataBuffer.capacity();
    }
    
    /**
     * 
     */
    public int limit() {
        return dataBuffer.limit();
    }
    
    /**
     * 
     */
    public void limit(int l) {
        dataBuffer.limit(l);
    }
    
    /**
     * 
     */
    public int position() {
        return dataBuffer.position();
    }
    
    /**
     * 
     */
    public void position(int p) {
        dataBuffer.position(p);
    }
    
    /**
     * 
     */
    public int remaining() {
        return dataBuffer.remaining();
    }
    
    /**
     * 
     */
    public void clear() {
        dataBuffer.clear();
    }
    
    /**
     * 
     */
    public void reset() {
        dataBuffer.reset();
    }
    
    /**
     * 
     */
    public void flip() {
        dataBuffer.flip();
    }
}
