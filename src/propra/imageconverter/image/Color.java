package propra.imageconverter.image;

import java.nio.ByteBuffer;
import static propra.imageconverter.image.ColorFormat.BLUE;
import static propra.imageconverter.image.ColorFormat.GREEN;
import static propra.imageconverter.image.ColorFormat.RED;

/**
 * Enthält einen Farbwert, oder indiziert einen Farbwert in einem 
 * byte-Array (Referenz)
 */
public class Color {

    // Farbwerte
    protected byte[] values;

    // Index des Pixels im Puffer
    private int index;

    /**
     * 
     */
    public Color() {
         values = new byte[ColorFormat.PIXEL_SIZE];
    }
   
    /**
     * 
     * @param values 
     */
    public Color(byte[] values) {
        set(values);
    }

    /**
     * 
     * @param values
     * @param index 
     */
    public Color(byte[] values,
                 int index) {
        this.values = values;
        this.index = index;
    }
   
   /**
    * 
    * @return 
    */
    public byte[] get() {
        return values;
    }
   
    /**
     * 
     */
    public int getIndex() {
        return index;
    }
   

    /**
     * 
     * @param values 
     */
    public void set(byte[] values) {
        System.arraycopy(this.values, 0, 
                        values, index, 
                        ColorFormat.PIXEL_SIZE);
    }

   /**
    * 
    * @param values
    * @param offs
    * @param len 
    */
    public void set(byte[] values, int offs, int len) {
        System.arraycopy(this.values, offs, 
                        values, index, 
                        ColorFormat.PIXEL_SIZE * len);
    }
   

    /**
     * Vergleicht zwei Farbwerte
     * 
     * @param offset1
     * @param offset2
     * @return 
     */
    public static boolean compareColor(Color c1, Color c2) {
        return (c1.values[c1.index + 0] == c2.values[c2.index + 0]
            &&  c1.values[c1.index + 1] == c2.values[c2.index + 1]
            &&  c1.values[c1.index + 2] == c2.values[c2.index + 2]);
    }
    
    /**
     * Vergleicht zwei Farbwerte in Array
     * 
     * @param offset1
     * @param offset2
     * @return 
     */
    public static boolean compareColor( byte[] c1, int index1,
                                        byte[] c2, int index2) {
        return (c1[index1 + 0] == c2[index2 + 0]
            &&  c1[index1 + 1] == c2[index2 + 1]
            &&  c1[index1 + 2] == c2[index2 + 2]);
    }
    
    /**
     * Füllt Buffer len-mal mit Farbwert und gibt neuen Offset zurück
     * 
     * @param color
     * @param len
     * @return 
     */
    public static int fill(ByteBuffer buffer, Color color, int len) {
        for(int i=0; i<len; i++){
            buffer.put( color.get(), 
                        color.index, 
                        ColorFormat.PIXEL_SIZE);
        }
        
        return buffer.position();
    }
    
    /**
     * Konvertiert Farbpuffer
     * 
     * @param input
     * @param srcFormat
     * @param output
     * @param dstFormat
     * @return 
     */
    public static ByteBuffer convert( ByteBuffer input, ColorFormat srcForm,
                                      ByteBuffer output, ColorFormat dstForm) {
        if (output == null
        ||  dstForm == null) {
            throw new IllegalArgumentException();
        }  
        
        byte[] inBytes = input.array();
        byte[] outBytes = output.array();
        byte r,g,b;
                
        int srcOffset = 0;
        int dstOffset = 0;
        
        int[] srcMap = srcForm.getMapping();
        int[] dstMap = dstForm.getMapping();
        
        for (int i=0; i<input.limit(); i+=3) {
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
}
