package propra.imageconverter.image;

import java.nio.ByteBuffer;
import java.util.Iterator;
import static propra.imageconverter.image.ColorFormat.BLUE;
import static propra.imageconverter.image.ColorFormat.GREEN;
import static propra.imageconverter.image.ColorFormat.RED;

/**
 *
 * @author pg
 */
public class ColorBuffer implements Iterable<Color> {
    
    // Interner Puffer
    private ByteBuffer buffer;
    
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
        buffer = ByteBuffer.wrap(data);
        this.format = new ColorFormat(format);
    }
    
    /**
     * 
     * @param data
     * @param format 
     */
    public ColorBuffer( ByteBuffer data,
                        ColorFormat format) {
        buffer = data;
        this.format = new ColorFormat(format);
    }
    
    /**
     * 
     * @param data
     * @param format 
     */
    public ColorBuffer( int size,
                        ColorFormat format) {
        buffer = ByteBuffer.allocate(size * ColorFormat.PIXEL_SIZE);
        this.format = new ColorFormat(format);
    }
    
    /**
     * Füllt Buffer len-mal mit Farbwert und gibt neuen Offset zurück
     * 
     * @param color
     * @param len
     * @return 
     */
    public int fill(Color color, int len) {
        for(int i=0; i<len; i++){
            put(color);
        }
        
        return buffer.position();
    }
    
    /**
     * 
     * @param offset1
     * @param offset2
     * @return 
     */
    public boolean compareColor(int offset1, int offset2) {
        byte[] array = buffer.array();
        return (array[offset1 + 0] == array[offset2 + 0]
            &&  array[offset1 + 1] == array[offset2 + 1]
            &&  array[offset1 + 2] == array[offset2 + 2]);
    }
    
    /**
     * 
     * @param input
     * @param srcFormat
     * @param output
     * @param dstFormat
     * @return 
     */
    public ColorBuffer convert( ColorBuffer output,
                                ColorFormat dstFormat) {
        if (output == null
        ||  dstFormat == null) {
            throw new IllegalArgumentException();
        }  
        
        byte[] inBytes = buffer.array();
        byte[] outBytes = output.array();
        byte r,g,b;
                
        int srcOffset = 0;
        int dstOffset = 0;
        
        int[] srcMap = format.getMapping();
        int[] dstMap = dstFormat.getMapping();
        
        for (int i=0; i<buffer.limit(); i+=3) {
            int sIndex = srcOffset + i;
            int dIndex = dstOffset + i;
            
            r = inBytes[sIndex + srcMap[RED]];
            g = inBytes[sIndex + srcMap[GREEN]];
            b = inBytes[sIndex + srcMap[BLUE]];
            
            outBytes[dIndex + dstMap[RED]] = r;
            outBytes[dIndex + dstMap[GREEN]] = g;
            outBytes[dIndex + dstMap[BLUE]] = b;
        }
        
        return output;
    }
    
    /**
     * 
     * @return 
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    /**
     * 
     * @return 
     */
    public byte[] array() {
        return buffer.array();
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public ColorBuffer get(Color p) {
        buffer.get(p.get());
        return this;
    }
    
    /**
     * 
     * @param p
     * @return 
     */
    public ColorBuffer put(Color p) {
        buffer.put(p.get());
        return this;
    }
    
    /**
     * 
     * @param buff
     * @return 
     */
    public ColorBuffer put(ColorBuffer buff) {
        buffer.put(buff.getBuffer());
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
                return buffer.hasRemaining();
            }
            
            /**
             * 
             * @return 
             */
            @Override
            public Color next() {
                Color p = new Color(buffer.array(),
                                    buffer.position());
                buffer.position(buffer.position() + 1);
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
        return buffer.capacity();
    }
    
    /**
     * 
     */
    public int limit() {
        return buffer.limit();
    }
    
    /**
     * 
     */
    public void limit(int l) {
        buffer.limit(l);
    }
    
    /**
     * 
     */
    public int position() {
        return buffer.position();
    }
    
    /**
     * 
     */
    public void position(int p) {
        buffer.position(p);
    }
    
    /**
     * 
     */
    public void skip(int len) {
        buffer.position(buffer.position() + len);
    }
    
    /**
     * 
     */
    public int remaining() {
        return buffer.remaining();
    }
    
    /**
     * 
     */
    public void clear() {
        buffer.clear();
    }
    
    /**
     * 
     */
    public void reset() {
        buffer.reset();
    }
    
    /**
     * 
     */
    public void flip() {
        buffer.flip();
    }
}
