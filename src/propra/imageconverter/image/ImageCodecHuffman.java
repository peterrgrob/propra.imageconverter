package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import propra.imageconverter.data.BitInputStream;
import propra.imageconverter.data.BitOutputStream;
import propra.imageconverter.data.DataBlock;
import static propra.imageconverter.data.DataCodecRaw.DEFAULT_BLOCK_SIZE;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.DataOutputStream;
import propra.imageconverter.data.IDataListener;

/**
 *
 * @author pg
 */
public class ImageCodecHuffman extends ImageCodecRaw {
    
    //  Histogramm der Daten
    private final int[] histogram = new int[256]; 
    
    //  Huffman-Baum zur Kodierung
    private HuffmanTree huffmanTree;
    
    // Bitstreams
    BitOutputStream outStream;
    BitInputStream inStream;   
    
    /*
     *  Konstruktor
     */
    public ImageCodecHuffman(ImageResource resource) {
        super(resource);
    }

    /**
     * 
     */
    @Override
    public void begin(Operation op) throws IOException {
        super.begin(op);
        
        if(op == Operation.ANALYZE_ENCODER) {
            Arrays.fill(histogram, 0);
        } else if(op == Operation.ENCODE) {
            // BitStream erstellen
            outStream = new BitOutputStream(resource.getCheckedOutputStream());
        }
    }
 
    /**
     *  Ermittelt die Häufigkeit der Symbole im Datenblock
     */
    @Override
    public void analyze(DataBlock block) {
        if(block == null) {
            throw new IllegalArgumentException();
        }
        
        if(operation == Operation.ANALYZE_ENCODER) {
            
            // Histogram aktualisieren für den aktuellen Block
            byte[] buffer = block.array();
            int offset = 0;

            while(offset < block.data.limit()) {
                histogram[buffer[offset] & 0xFF]++;
                offset++;
            }
        } 
    }
    
    /**
     * 
     */
    @Override
    public void end() throws IOException {
        if(operation == Operation.ANALYZE_ENCODER) {
            
            /**
             *  Histogram prüfen
             */
            int hCtr = 0;
            for(int i:histogram) {
                hCtr += i;
            }  
            if(hCtr != image.getHeader().imageSize()) {
                //throw new IOException("Fehlerhafte Bilddaten (Histogram)");
            }
            System.out.println("Huffman Symbole (Ok): " + hCtr);
            
            /*
             *  Nach der Encoder-Analyse den entsprechenden Huffman Baum aus dem 
             *  ermittelten Histogram erstellen
             */
            huffmanTree = new HuffmanTree();
            huffmanTree.buildFromHistogram(histogram);
            
        } else if(operation == Operation.ENCODE) {
            outStream.flush();
            image.getHeader().encodedSize(outStream.getByteCounter());
            outStream.flush();
        }
        
        super.end();
    }
    
    /**
     * 
     */
    @Override
    public boolean analyzeNecessary(Operation op) {
        return op == Operation.ENCODE;
    }

    /**
     * 
     */
    @Override
    public void decode( DataBlock block, 
                        IDataListener listener) throws IOException {
        
        // Ausgabepuffer vorbereiten
        if(block.data == null) {
            block.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        }
        
        int symbolCtr = 0;
        
        // BitStream erstellen
        BitInputStream stream = new BitInputStream(resource.getCheckedInputStream());
        
        // Kodierten Baum einlesen und erstellen
        huffmanTree = new HuffmanTree();
        huffmanTree.buildFromResource(stream);
        
        // Lädt, dekodiert und sendet Pixelblöcke an Listener  
        while(symbolCtr++ < image.getHeader().imageSize()) {
            
            // Symbol dekodieren
            int symbol = huffmanTree.decodeSymbol(stream);
            if(symbol == -1) {
                break;
            }
            
            // Symbol speichern
            block.data.put((byte)symbol);

            // Wenn Blockgröße erreicht an Listener senden
            if(block.data.capacity() == block.data.position()) {
                
                // Farbkonvertierung
                if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {   
                    ColorFormat.convertColorBuffer( block.data,
                                                    image.getHeader().colorFormat(),
                                                    block.data, 
                                                    ColorFormat.FORMAT_RGB);
                }
                
                dispatchData(   IDataListener.Event.DATA_BLOCK_DECODED, 
                                listener, 
                                block);    
            }
        }
        
        // Farbkonvertierung
        if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {   
            ColorFormat.convertColorBuffer( block.data,
                                            image.getHeader().colorFormat(),
                                            block.data, 
                                            ColorFormat.FORMAT_RGB);
        }
        
        // Restliche Daten im Puffer übertragen
        block.lastBlock = true;
        dispatchData(   IDataListener.Event.DATA_BLOCK_DECODED, 
                        listener, 
                        block);     
    }

    /*
     * 
     */
    @Override
    public void encode( DataBlock block, 
                        IDataListener listener) throws IOException {
        if(huffmanTree == null) {
            throw new IllegalStateException("Huffmann-Tree nicht initialisiert.");
        }
        
        ByteBuffer buff = block.data;
                  
        /**
         *   Baum als Bitfolge kodieren
         */
        huffmanTree.storeTree(outStream);
        
        /**
         *  Symbole im Puffer iterieren, per Huffmantree zu Bitcode umsetzen und 
         *  diesen in der Resource speichern
         */
        ByteBuffer c = ByteBuffer.allocate(3);
        while(buff.position() < buff.limit()) {
            
            // Pixel lesen und Farbe konvertieren
            buff.get(c.array());
            ColorFormat.convertColorBuffer( c, ColorFormat.FORMAT_RGB, 
                                            c, image.colorFormat);
                    
            outStream.write(huffmanTree.encodeSymbol(c.get() & 0xFF));
            outStream.write(huffmanTree.encodeSymbol(c.get() & 0xFF));
            outStream.write(huffmanTree.encodeSymbol(c.get() & 0xFF));
            c.clear();
        }
    }
    
    /**
     * 
     */
    
}
