package propra.imageconverter.image;

import java.nio.ByteBuffer;

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
     * @return 
     * @return  
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
     * @param c1
     * @param c2
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
     * @param c1
     * @param index1
     * @param c2
     * @param index2
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
     * @param buffer
     * @param color
     * @param len
     * @return 
     */
    public static int fill(ByteBuffer buffer, Color color, int len) {
        byte[] b = buffer.array();
        byte[] bs = color.values;
        int offs = buffer.position();
        int co = color.getIndex();
        
        for(int i=0; i<len; i++){
            b[offs++] = bs[co];
            b[offs++] = bs[co + 1];
            b[offs++] = bs[co + 2];            
        }
        buffer.position(buffer.position() + len * ColorFormat.PIXEL_SIZE);
        return buffer.position();
    }
}
