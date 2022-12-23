package propra.imageconverter.image.huffman;

import propra.imageconverter.util.BitCode;
import java.io.IOException;
import propra.imageconverter.util.BitInputStream;
import propra.imageconverter.util.BitOutputStream;

/**
 *  Klasse implementiert einen Knoten des Huffman Baumes
 */    
public class HuffmanNode implements Comparable<HuffmanNode>{
    
    // Kindknoten 
    private HuffmanNode leftNode;
    private HuffmanNode rightNode;

    // Symbol und seine Häufigkeit   
    private int symbol;
    private long frequency;

    // Code des Symbols
    private BitCode code;

    /**
     *  Konstruktoren
     */
    public HuffmanNode() {
    }

    /**
     *
     * @param s
     * @param frequency
     */
    public HuffmanNode(int s, long frequency) {
       this.symbol = s;
       this.frequency = frequency;
   }

    /**
     *
     * @param s
     * @param frequency
     * @param leftNode
     * @param rightNode
     */
    public HuffmanNode(int s, long frequency,
                       HuffmanNode leftNode, HuffmanNode rightNode) {
       this.symbol = s;
       this.frequency = frequency;
       this.leftNode = leftNode;
       this.rightNode = rightNode;
   }

   /**
    * Baum aus der ProPra Kodierung rekursiv wiederherstellen
    * 
    * @param resource
    * @param nodeMap
    * @throws java.io.IOException
    */
   public void buildTreeFromResource(BitInputStream resource, HuffmanNode[] nodeMap) throws IOException {

       /*
        * Nächstes Bit gibt an um welchen Knotentyp es sich handelt
        */
       if(resource.readBit() == 0) {

          // Innerer Knoten, daher mit Pre-Order Durchlauf fortfahren
          leftNode = new HuffmanNode();
          leftNode.buildTreeFromResource(resource, nodeMap);
          rightNode = new HuffmanNode();
          rightNode.buildTreeFromResource(resource, nodeMap); 

       } else {

          // Blatt erreicht, daher Symbol für das Blatt einlesen
          symbol = resource.readByte() & 0xFF;

          // Fehlerbehandlung bei möglicherweise korrupten Daten
          if(nodeMap[symbol] != null) {
              throw new IOException("Doppeltes Huffman-Blatt, Daten korrupt?");
          }
          
          // Blatt vermerken
          nodeMap[symbol] =  this;
       }
   }

   /**
    *  
    * @param stream
    * @throws IOException
    */
   public void storeTreeInStream(BitOutputStream stream) throws IOException {
       if( leftNode == null 
       &&  rightNode == null) {
           stream.write(1);
           stream.writeByte(symbol);
       } else {
           stream.write(0);
           leftNode.storeTreeInStream(stream);
           rightNode.storeTreeInStream(stream);
       }
   }
   
   /**
    * Berechnet rekursiv die Codes je Symbol nach Erstellung des Baumes
    * @param c
    */
   public void buildBitCodes(BitCode c) {

       // Code speichern
       code = c;

       // Innerer Knoten?
       if(leftNode != null 
       && rightNode != null) {

           //  Linken und rechten Teilbaum besuchen
           leftNode.buildBitCodes(new BitCode(c).addBit(false));
           rightNode.buildBitCodes(new BitCode(c).addBit(true));

       } 
   }

   /**
    * Sucht Symbol zu den Bits im Stream, gibt -1 bei EOF zurück, 
    * ansonsten Symbol
    * 
    * @param stream
    * @return 
    * @throws java.io.IOException 
    */
   public int decodeSymbol(BitInputStream stream) throws IOException {

       // Symbol erreicht?
       if( leftNode == null
       &&  rightNode == null) {
           return symbol;
       }

       // Nächstes Bit lesen und rekursiv dem Pfad folgen
       switch(stream.readBit()) {
           case 1 -> {
               return rightNode.decodeSymbol(stream);
           }
           case 0 -> {
               return leftNode.decodeSymbol(stream);
           }
           case -1 -> {
               return -1;
           }
       }

       return -1;
   }

   /**
    *  Vergleicht zwei Knoten anhand ihrer Symbolhäufigkeit, benötigt für die 
    *  Sortierung nach Häufigkeit
    */
   @Override
   public int compareTo(HuffmanNode o) {
       return (int)(frequency - o.frequency);
   }

   /**
    * Getter/Setter
    * @return 
    */
    public long getFrequency() {
        return frequency;
    }

    /**
     *
     * @return
     */
    public BitCode getCode() {
       return code;
    }
}
