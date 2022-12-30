package propra.imageconverter.image.compression;

import propra.imageconverter.util.BitCode;
import java.io.IOException;
import java.util.PriorityQueue;
import propra.imageconverter.util.BitInputStream;
import propra.imageconverter.util.BitOutputStream;

/**
 *  Implementiert einen binären Baum zur Erstellung und Abbildung der Huffmancodes 
 *  und Symbole
 */
public class HuffmanTree {
        
    //  Wurzelknoten
    private HuffmanNode rootNode;
    
    /*
     *  Array aller Knoten mit dem Symbol als Schlüssel zur schnellen
     *  Zuordnung von Symbolen zu Codes
     */
    private HuffmanNode[] nodeArray = new HuffmanNode[256];

    /**
     * 
     */
    public HuffmanTree() {
    }
    
    /**
     * Rekonstruiert rekursiv einen Huffmantree aus dem Bitcode in der Ressource nach 
     * Propra-Konstruktionsvorschrift mit einem Preorder-Durchlauf
     * 
     */
    public void buildTreeFromResource(BitInputStream resource) throws IOException {
        rootNode = new HuffmanNode((byte)0, 0);
        rootNode.buildTreeFromResource(resource, nodeArray);
        rootNode.buildBitCodes(new BitCode(0,0));
    }
    
    /**
     * Erstellt einen Huffmantree aus den Eingabesymbolen und Häufigkeiten
     * durch Verwendung einer PriorityQueue
     *
     */
    public void buildTreeFromHistogram(long[] symbols) {
        
        PriorityQueue<HuffmanNode> q = new PriorityQueue<>();
        
        /**
         *  Symbole nach Häufigkeit sortiert als Knoten 
         *  in PriorityQueue einfügen
         */ 
        for(int s=0; s<symbols.length; s++) {
            if(symbols[s] > 0) {
                HuffmanNode n = new HuffmanNode(s, symbols[s]);
                q.offer(n);
                nodeArray[s] = n;
            }
        }
        
        /*
         *  Baum iterativ konstruieren bis nur noch ein Knoten (Wurzel) in der 
         *  Queue enthalten ist.
         */
        while(q.size() > 1) {
                    
            /*
             *  Zwei niedrigste Knoten zu einem Knoten verbinden
             *  und in die Queue einfügen
             */
            HuffmanNode right = q.poll();
            HuffmanNode left = q.poll();
            HuffmanNode parentNode = new HuffmanNode( (byte)0, 
                                                    left.getFrequency() + right.getFrequency(),
                                                    left,right);
                    
            q.offer(parentNode);
        }
        
        // Wurzel speichern
        rootNode = q.remove();
        
        // Codes berechnen
        rootNode.buildBitCodes(new BitCode(0, 0));
    }
    
    /**
     * Baum als kodierte Bitfolge in Stream speichern
     *
     */
    public void storeTreeInStream(BitOutputStream stream) throws IOException {
        rootNode.storeTreeInStream(stream);
    }
    
    /**
     * Dekodiert ein Byte von Stream, gibt Symbol oder -1 bei EOF zurück
     * 
     */
    public int decodeSymbol(BitInputStream stream) throws IOException {
        return rootNode.decodeSymbol(stream);
    }
    
    /**
     * Gibt den Bitcode für ein Symbol zurück
     * 
     */
    public BitCode encodeSymbol(int symbol) {
        HuffmanNode n = nodeArray[symbol];
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
