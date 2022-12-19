package propra.imageconverter.image.huffman;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import propra.imageconverter.util.*;
import propra.imageconverter.data.DataBlock;
import static propra.imageconverter.data.DataCodecRaw.DEFAULT_BLOCK_SIZE;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.IDataListener;
import propra.imageconverter.image.ColorFormat;
import propra.imageconverter.image.ImageCodecRaw;
import propra.imageconverter.image.ImageResource;

/**
 *
 */
public class ImageCodecHuffman extends ImageCodecRaw {
    
    //  Histogramm der Daten
    private final long[] histogram = new long[256]; 
    
    //  Huffman-Baum zur Kodierung
    private HuffmanTree huffmanTree;
    
    // Ausgabe Bitstream
    BitOutputStream outStream;  
    
    /*
     *  Konstruktor
     */
    public ImageCodecHuffman(ImageResource resource) {
        super(resource);
    }

    /**
     *  Bereitet Blockweise Datenverarbeitung vor
     */
    @Override
    public void begin(Operation op) throws IOException {
        super.begin(op);
        
        switch(op) {
            case ENCODER_ANALYZE -> {
                Arrays.fill(histogram, 0);
            }
            case ENCODE -> {
                /*
                 *  BitStream erstellen und Baum als Bitfolge in Stream kodieren
                 */
                outStream = new BitOutputStream(resource.getOutputStream());
                huffmanTree.storeTreeInStream(outStream);
            }
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
        
        if(operation == Operation.ENCODER_ANALYZE) {           
            // Histogram aktualisieren fmit dem Datenblock
            byte[] buffer = block.array();
            int offset = 0;

            while(offset < block.data.limit()) {
                histogram[buffer[offset] & 0xFF]++;
                offset++;
            }
        } 
    }
    
    /**
     *   Schließt Blockweise Datenverarbeitung ab
     */
    @Override
    public void end() throws IOException {
        switch(operation) {
            case ENCODER_ANALYZE -> {
               /**
                *  Histogram prüfen
                */
                long sum = 0;
                for(long i:histogram) {
                    sum += i;
                }  
                if(sum != image.getHeader().imageSize()) {
                    throw new IOException("Fehlerhafte Bilddaten (Histogram)");
                }
                System.out.println("Huffman Symbole: " + sum);

                /*
                 *  Nach der Encoder-Analyse den entsprechenden Huffman Baum aus dem 
                 *  ermittelten Histogram erstellen
                 */
                huffmanTree = new HuffmanTree();
                huffmanTree.buildTreeFromHistogram(histogram);
            }
            case ENCODE -> {
                /*
                 *  Stream flushen und kodierte Datengröße aktualisieren
                 */
                outStream.flush();
                image.getHeader().encodedSize(outStream.getByteCounter()); 
            }
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
     *  Dekodiert Huffman kodierten Datenblock
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
        BitInputStream stream = new BitInputStream(resource.getInputStream());
        ByteBuffer data = block.data;
        
        // Kodierten Baum einlesen und erstellen
        huffmanTree = new HuffmanTree();
        huffmanTree.buildTreeFromResource(stream);
        
        // Lädt, dekodiert und sendet Pixelblöcke an Listener  
        while(symbolCtr++ < image.getHeader().imageSize()) {
            
            // Symbol dekodieren
            int symbol = huffmanTree.decodeSymbol(stream);
            if(symbol == -1) {
                break;
            }
            
            // Symbol speichern
            data.put((byte)symbol);

            // Wenn Blockgröße erreicht an Listener senden
            if(data.capacity() == data.position()) {
                
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
         *  Symbole im Puffer iterieren, per Huffmantree zu Bitcode umsetzen und 
         *  diesen in der Resource speichern
         */
        byte[] c = new byte[3];
        while(buff.position() < buff.limit()) {
            
            // Pixel lesen und Farbe konvertieren
            buff.get(c);
            ColorFormat.convertColor(   c, ColorFormat.FORMAT_RGB, 
                                        c, image.getHeader().colorFormat());
                    
            outStream.write(huffmanTree.encodeSymbol(c[0] & 0xFF));
            outStream.write(huffmanTree.encodeSymbol(c[1] & 0xFF));
            outStream.write(huffmanTree.encodeSymbol(c[2] & 0xFF));
        }
    }   
}
