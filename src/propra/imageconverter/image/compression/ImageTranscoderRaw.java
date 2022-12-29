package propra.imageconverter.image.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataTranscoder;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.image.ImageAttributes;


/**
 * Basiscodec für die Konvertierung von unkomprimierten Pixelblöcken.
 */
public class ImageTranscoderRaw extends DataTranscoder implements IDataTarget {
    
    // Zugeordnete Bildattribute
    protected ImageAttributes attributes;
    
    /**
     * 
     */
    public ImageTranscoderRaw(ImageAttributes attributes) {
        this.attributes = attributes;
    }
    
    /**
     * 
     */
    @Override
    public Compression getCompression() {
        return Compression.NONE;
    }
    
    /**
     * Dekodiert die Daten der Resource blockweise an das Datenziel
     */
    @Override
    public void decode(CheckedInputStream in, IDataTarget target) throws IOException {
        
        // Lese Puffer  
        ByteBuffer data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        boolean bLast = false;
        long offset = 0;
        long len = attributes.getImageSize();
        
        /*
         * Lädt und sendet Pixelblöcke direkt an Listener  
         */
        while(offset < len) {
            
            // Blockgröße anpassen
            if(len - offset < data.capacity()) {
                data.limit((int)(len - offset));
                bLast = true;
            }
            
            // Datenblock von Resource lesen 
            int r = in.read(data);
            if(r == -1) {
                throw new IOException("Lesefehler!");
            }
            
            // Daten an das Ziel senden
            target.onData(data,bLast, this);  
            offset += r;
            data.clear();
        }
    }

    /**
     * Schreibt Datenblock 1:1 in die Resource
     */
    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException {
        outStream.write(block);
        encodedBytes += block.limit() - block.position();
    }
    
    /**
     * Schließt laufenden ByteBuffer ab und sendet ihn an das Datenziel
     */
    protected void pushData(ByteBuffer block, boolean last, IDataTarget target) throws IOException {
        block.flip();
        target.onData(block, last, this);
        block.clear();
    }

    /**
     * 
     */
    @Override
    public void onData(ByteBuffer data, boolean lastBlock, IDataTranscoder caller) throws IOException {
        switch(operation) {
            case ENCODE, ANALYZE -> {
                encode(data, lastBlock);
            }
        }
    }
}
