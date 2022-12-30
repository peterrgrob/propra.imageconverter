package propra.imageconverter.util;

/**
 * Utility Klasse für BitCode, hält z.B den Code für ein Huffman-Symbol
 */
public class BitCode implements Comparable<BitCode> {

   // Bits des Codes
   private int code;

   // Länge des Codes in Bit
   private int length;

   public BitCode(int code, int length) {
       this.code = code;
       this.length = length;
   }

   public BitCode(BitCode src) {
       this.code = src.code;
       this.length = src.length;
   }
   
   public void setCode(int code) {
       this.code = code;
   }

   /**
    * Fügt ein Bit an aktuelle Position hinzu
    */
   public BitCode addBit(boolean bit) {
       code <<= 1;
       code |= ((bit == true) ? 1 : 0);
       length++;
       return this;
   }

   /**
    * Gibt den aktuellen Code zurück
    */
   public int getCode() {
       return code;
   }

   /**
    * Code-Länge
    */
   public int getLength() {
       return length;
   }

   /**
    * Vergleicht zwei Codes
    */
   @Override
   public int compareTo(BitCode o) {
       return (code - o.code) + 
              (length - o.length); 
   }   
}
