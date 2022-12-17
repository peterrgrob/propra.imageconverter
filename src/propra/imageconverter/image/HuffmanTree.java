package propra.imageconverter.image;

import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;
import propra.imageconverter.data.BitCode;
import propra.imageconverter.data.BitInputStream;
import propra.imageconverter.data.BitOutputStream;

/**
 *  Implementiert einen binären Baum zur Erstellung und Abbildung der Huffmancodes 
 *  und Symbole
 */
public class HuffmanTree {
        
    //  Wurzelknoten
    private Node rootNode;
    
    // Maximale Anzahl möglicher Blätter
    private static final int MAX_LEAFS = 256;
    
    /*
     *  Hashmap aller Knoten mit dem Symbol als Schlüssel zur schnellen
     *  Zuordnung von Symbolen zu Codes
     */
    private HashMap<Integer, Node> nodeMap;
    
    /**
     *  Klasse implementiert einen Knoten des Huffman Baumes
     */    
    private class Node implements Comparable<Node>{
        
        /**
         *  Knoten
         */ 
        private Node leftNode;
        private Node rightNode;

        /*
         *  Symbol und seine Häufigkeit   
         */
        private int symbol;
        private int frequency;
        
        /*
         *  Codewort des Knotens  
         */
        private BitCode code;
        
        /**
         *  Konstruktoren
         */
        public Node() {
        }
        
        public Node(int s, 
                    int frequency) {
            this.symbol = s;
            this.frequency = frequency;
        }
        
        public Node(int s,
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
        public void generateCode(BitCode c) {
            
            // Code speichern
            code = c;
            
            // Innerer Knoten?
            if(leftNode != null 
            && rightNode != null) {
                
                //  Linken und rechten Teilbaum besuchen
                leftNode.generateCode(new BitCode(c).addBit(false));
                rightNode.generateCode(new BitCode(c).addBit(true));
                
            } else {
                System.out.print("\n Symbol: "+symbol + " " + code.toString());
            }
        }
        
        /**
         *  Baum aus der ProPra Kodierung rekursiv wiederherstellen
         */
        public void buildFromResource(BitInputStream resource) throws IOException {
            
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
               symbol = resource.readByte() & 0xFF;
               
               // Blatt vermerken
               nodeMap.put(symbol, this);
               
               // Fehlerbehandlung bei möglicherweise korrupten Daten
               if(nodeMap.size() > MAX_LEAFS) {
                   throw new IOException("Anzahl Blätter zu groß, Datei möglicherweise ungültig.");
               }
            }
        }
        
        /**
         *  
         */
        public void storeNode(BitOutputStream stream) throws IOException {
            if( leftNode == null 
            &&  rightNode == null) {
                stream.write(1);
                stream.writeByte(symbol);
            } else {
                stream.write(0);
                leftNode.storeNode(stream);
                rightNode.storeNode(stream);
            }
        }
        
        /**
         *  Sucht Symbol zu den Bits im Stream, gibt -1 bei EOF zurück, 
         *  ansonsten Symbol
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
        public int compareTo(Node o) {
            int f = frequency - o.frequency;
            if(f == 0) {
                f = code.getLength() - o.code.getLength();
            }
            return f;
        }
        
        /**
         * Getter/Setter
         */
        public int getFrequency() {
            return frequency;
        }

        public BitCode getCode() {
            return code;
        }
        
        /**
         *  Kodiert einen Baum nach Konstruktionvorschrift der ProPra 
         *  Spezifikation in einen String
         */
        @Override
        public String toString() {
            return "";
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
    public void buildFromResource(BitInputStream resource) throws IOException {
        if(resource == null) {
            throw new IllegalArgumentException();
        }
        
        nodeMap = new HashMap<>();
        rootNode = new Node((byte)0, 0);
        rootNode.buildFromResource(resource);
        rootNode.generateCode(new BitCode(0,0));
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
                Node n = new Node(s, symbols[s]);
                q.offer(n);
                nodeMap.put(s, n);
            }
        }
        
        /*
         *  Baum iterativ konstruieren bis nur noch ein Knoten (Wurzel) in der 
         *  Queue enthalten ist.
         */
        while(q.size() > 1) {
                    
            /*
             *  Zwei niedrigste Knoten zu einem Knoten verbinden
             *  und in der Queue einfügen
             */
            Node right = q.poll();
            Node left = q.poll();
            Node parentNode = new Node( (byte)0, 
                                        left.getFrequency() + right.getFrequency(),
                                        left,
                                        right);
                    
            q.offer(parentNode);
        }
        
        // Wurzel speichern
        rootNode = q.remove();
        
        // Codes berechnen
        rootNode.generateCode(new BitCode(0, 0));
        
        System.out.print(toString());
    }
    
    /**
     *  Baum als kodierte Bitfolge in Stream speichern
     */
    public void storeTree(BitOutputStream stream) throws IOException {
        rootNode.storeNode(stream);
    }
    
    /**
     *  Dekodiert Byte von Stream, gibt Symbol oder -1 bei EOF zurück
     */
    public int decodeSymbol(BitInputStream stream) throws IOException {
        return rootNode.decodeSymbol(stream);
    }
    
    /**
     *  Gibt den Bitcode für ein Symbol zurück
     */
    public BitCode encodeSymbol(int symbol) {
        Node n = nodeMap.get(symbol);
        return n != null ? n.getCode() : null;
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
