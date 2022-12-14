package propra.imageconverter.image;

import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;
import propra.imageconverter.data.BitStream;

/**
 *  Implementiert einen binären Baum zur Erstellung und Abbildung der Huffmancodes 
 *  und Symbole
 */
public class HuffmanTree {
        
    //  Wurzelknoten
    private Node rootNode;
    
    /*
     *  Hashmap aller Knoten mit dem Symbol als Schlüssel zur schnellen
     *  Zuordnung von Symbolen zu Codes
     */
    private HashMap<Byte, Node> nodeMap;
    
    
    /**
     *  Implementiert den Code für ein Symbol
     */
    public static class Code implements Comparable<Code> {
        
        // Bits des Codes
        private int code;
        
        // Länge des Codes in Bit
        private int length;

        /**
         *  Constructor
         */
        public Code(int code, 
                    int length) {
            this.code = code;
            this.length = length;
        }
        
        public Code(Code src) {
            this.code = src.code;
            this.length = src.length;
        }

        /**
         * Bit hinzufügen
         */
        public Code addBit(boolean bit) {
            code <<= 1;
            code |= (bit == true) ? 1 : 0;
            length++;
            return this;
        }
        
        /**
         *  Getter/Setter
         */
        public int getCode() {
            return code;
        }

        public int getLength() {
            return length;
        }
        
        @Override
        public int compareTo(Code o) {
            return (code - o.code) + 
                   (length - o.length); 
        }       
    }
    
    /**
     *  Klasse implementiert einen Knoten des Huffman Baumes
     */    
    private static class Node implements Comparable<Node>{
        
        /**
         *  Knoten
         */ 
        private Node leftNode;
        private Node rightNode;

        /*
         *  Symbol und seine Häufigkeit   
         */
        private byte symbol;
        private int frequency;
        
        /*
         *  Codewort des Knotens  
         */
        private Code code;
        
        /**
         *  Konstruktoren
         */
        public Node() {
        }
        
        public Node(byte s, 
                    int frequency) {
            this.symbol = s;
            this.frequency = frequency;
        }
        
        public Node(byte s,
                    int frequency,
                    Node leftNode,
                    Node rightNode) {
            this.symbol = s;
            this.frequency = frequency;
            this.leftNode = leftNode;
            this.rightNode = rightNode;
        }
        
        /**
         *  Berechnet rekursiv die Codes je Symbol nach Erstellung des Baumes
         */
        public void buildCode(Code c) {
            // Code speichern
            code = new Code(c);
            
            // Innerer Knoten?
            if(leftNode != null 
            && rightNode != null) {
                //  Linken und rechten Teilbaum besuchen
                leftNode.buildCode(c.addBit(false));
                rightNode.buildCode(c.addBit(true));
            }
        }
        
        /**
         *  Baum aus der ProPra Kodierung rekursiv wiederherstellen
         */
        public void buildFromResource(BitStream resource) throws IOException {
            /*
             * Nächstes Bit gibt an um welchen Knotentyp es sich handelt
             */
            if(resource.readBit() == 0) {
               // Innerer Knoten, daher mit Pre-Order Durchlauf fortfahren
               leftNode = new Node();
               leftNode.buildFromResource(resource);
               rightNode = new Node();
               rightNode.buildFromResource(resource); 
            } else {
               // Blatt erreicht, daher Symbol für das Blatt einlesen
               symbol = resource.readByte();
            }
        }
        
        /**
         *  Sucht Symbol zu den Bits im Stream, gibt -1 bei EOF zurück, 
         *  ansonsten Symbol
         */
        public int decodeBits(BitStream stream) throws IOException {
            
            // Symbol erreicht?
            if( leftNode == null
            &&  rightNode == null) {
                return symbol;
            }
            
            // Nächstes Bit lesen und rekursiv dem Pfad folgen
            switch(stream.readBit()) {
                case 1 -> {
                    return rightNode.decodeBits(stream);
                }
                case 0 -> {
                    return leftNode.decodeBits(stream);
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
        public int compareTo(Node o) {
            return frequency - o.frequency;
        }
        
        /**
         * Getter/Setter
         */
        public int getFrequency() {
            return frequency;
        }

        public Code getCode() {
            return code;
        }
        
        /**
         *  Kodiert einen Baum nach Konstruktionvorschrift der ProPra 
         *  Spezifikation in einen String
         */
        @Override
        public String toString() {
            if(leftNode == null 
            && rightNode == null) {
                return "1" + symbol;
            } else {
                String code = "0";
                if(leftNode != null) {
                    code += leftNode.toString();
                }
                if(rightNode != null) {
                   code += rightNode.toString();
                }    
                return code;
            }
        }
    }

    /**
     * 
     */
    public HuffmanTree() {
    }
    
    /**
     *  Rekonstruiert rekursiv einen Huffmantree aus der Resource nach 
     *  Propra-Konstruktionsvorschrift aus einem Code mit Preorder-Durchlauf
     */
    public void buildFromResource(BitStream resource) throws IOException {
        if(resource == null) {
            throw new IllegalArgumentException();
        }
        
        rootNode = new Node((byte)0, 0);
        rootNode.buildFromResource(resource);
        rootNode.buildCode(new Code(0,0));
    }
    
    /*
     *  Erstellt einen Huffmantree aus den Eingabesymbolen und Häufigkeiten
     *  durch Verwendung einer PriorityQueue
     */
    public void buildFromHistogram(int[] symbols) {
        PriorityQueue<Node> q = new PriorityQueue<>();
        nodeMap = new HashMap<>();
        
        /**
         *  Symbole nach Häufigkeit sortiert als Knoten 
         *  in PriorityQueue einfügen
         */ 
        for(int s=0; s<symbols.length; s++) {
            if(symbols[s] > 0) {
                Node n = new Node((byte)s, symbols[s]);
                q.add(n);
                nodeMap.put((byte)s, n);
            }
        }
        
        /*
         *  Baum iterativ konstruieren bis nur noch ein Knoten (Wurzel) in der 
         *  Queue enthalten ist.
         */
        while(q.size() > 1) {
                    
            /*
             *  Zwei häufigste Knoten zu einem Knoten verbinden
             *  und an das Ende der Queue einfügen
             */
            Node left = q.remove();
            Node right = q.remove();
            Node parentNode = new Node( (byte)0, 
                                        left.getFrequency() + right.getFrequency(),
                                        left,
                                        right);
                    
            q.add(parentNode);
        }
        
        // Wurzel speichern
        rootNode = q.remove();
        
        // Codes berechnen
        rootNode.buildCode(new Code(0, 0));
        
        System.out.print(toString());
    }
    
    /**
     *  Dekodiert Byte von Stream, gibt Symbol oder -1 bei EOF zurück
     */
    public int decodeBits(BitStream stream) throws IOException {
        return rootNode.decodeBits(stream);
    }
    
    /**
     * 
     */
    @Override
    public String toString() {
        if(rootNode != null) {
            return rootNode.toString();
        }
        return "";
    }
}
