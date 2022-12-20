package propra.imageconverter.image;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 *
 * @author pg
 */
public class PixelBuffer implements Iterable<Pixel> {
    
    // Interner Puffer
    private ByteBuffer dataBuffer;
    
    // Pixelformat
    private ColorFormat format;
    
    /**
     * 
     */
    public PixelBuffer() {
    }
    
    /**
     * 
     * @param data
     * @param format 
     */
    public PixelBuffer( byte[] data,
                        ColorFormat format) {
        dataBuffer = ByteBuffer.wrap(data);
        this.format = new ColorFormat(format);
    }
    
    /**
     * 
     * @param data
     * @param format 
     */
    public PixelBuffer( ByteBuffer data,
                        ColorFormat format) {
        dataBuffer = data;
        this.format = new ColorFormat(format);
    }
    
    /**
     * 
     * @param data
     * @param format 
     */
    public PixelBuffer( int size,
                        ColorFormat format) {
        dataBuffer = ByteBuffer.allocate(size * ColorFormat.PIXEL_SIZE);
        this.format = new ColorFormat(format);
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
    public PixelBuffer get(Pixel p) {
        dataBuffer.get(p.get());
        return this;
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public PixelBuffer put(Pixel p) {
        dataBuffer.put(p.get());
        return this;
    }

    /**
     * 
     * @return 
     */
    @Override
    public Iterator<Pixel> iterator() {
        Iterator<Pixel> it = new Iterator<Pixel>() {
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
            public Pixel next() {
                Pixel p = new Pixel(dataBuffer.array(),
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
}
