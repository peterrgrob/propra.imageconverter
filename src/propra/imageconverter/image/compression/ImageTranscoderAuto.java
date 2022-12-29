package propra.imageconverter.image.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;

/**
 * Bündelt mehrere Encoder und ermittelt das beste Verfahren
 */
public class ImageTranscoderAuto extends ImageTranscoderRaw {
    
    // Liste der Encoder
    private final ArrayList<ImageTranscoderRaw> encoderList;
    
    // Bester Codec
    private ImageTranscoderRaw winner;
    
    /**
     * 
     */
    public ImageTranscoderAuto( ArrayList<ImageTranscoderRaw> encoder) {
        super(null);
        this.encoderList = encoder;
    }
    
    /**
     * 
     */
    @Override
    public Compression getCompression() {
        return Compression.AUTO;
    }

    /**
     * 
     */
    public ImageTranscoderRaw getWinner() {
        return winner;
    }
    
    /**
     * 
     */
    @Override
    public boolean analyzeNecessary() {
        return true;
    }

    /**
     * 
     */
    @Override
    public IDataTranscoder beginOperation(Operation op, CheckedOutputStream out) throws IOException {
        super.beginOperation(op, out);

        // Encoding mit NullStream beginnen
        for(ImageTranscoderRaw e : encoderList) {
            e.beginOperation(op, new CheckedOutputStream(OutputStream.nullOutputStream()));
        }
        
        return this;
    }
    
    /**
     * 
     */
    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException {
        // Block an Codecs weiterreichen
        for(ImageTranscoderRaw e : encoderList) {
            e.encode(block, last);
            block.rewind();
        }            
    }

    /**
     * 
     */
    @Override
    public long endOperation() throws IOException {
        if(null != operation) switch (operation) {
            case ANALYZE -> {
                // Analyse abschließen
                for(ImageTranscoderRaw e : encoderList) {
                    e.endOperation();
                }
            }
            case ENCODE -> {
                // Besten Codec ermitteln
                long min = Long.MAX_VALUE;
                long t = 0;
                for(ImageTranscoderRaw e : encoderList) {
                    if((t = e.endOperation()) < min) {
                       winner = e;
                       min = t; 
                    }
                }
                encodedBytes = min;
            }
        }
        return super.endOperation();
    }
    
    @Override
    public void decode(CheckedInputStream in, IDataTarget target) throws IOException {
        throw new UnsupportedOperationException();
    }
}
