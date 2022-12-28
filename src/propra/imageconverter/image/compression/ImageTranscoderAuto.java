package propra.imageconverter.image.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;
import propra.imageconverter.util.PropraException;

/**
 * Bündelt mehrere Encoder und ermittelt über den Analyse-Lauf das beste
 * Verfahren und verwendet dieses anschließend für die Kodierung
 */
public class ImageTranscoderAuto extends ImageTranscoderRaw {
    
    // Encoder
    private final ArrayList<ImageTranscoderRaw> encoderList;
    
    // Bester Codec
    private ImageTranscoderRaw winner;
    
    /**
     * 
     */
    public ImageTranscoderAuto(ArrayList<ImageTranscoderRaw> encoder) {
        super(null);
        this.encoderList = encoder;
    }

    @Override
    public long endOperation() throws IOException {
        if(null != operation) switch (operation) {
            case ANALYZE -> {
                // Analyse abschließen
                for(ImageTranscoderRaw e : encoderList) {
                    e.endOperation();
                }
            }
            case AUTO -> {
                // Besten Codec ermitteln
                long ctr = 0;
                for(ImageTranscoderRaw e : encoderList) {
                    if(e.endOperation() > ctr) {
                        winner = e;
                    }
                }
            }
            case ENCODE -> encodedBytes = winner.endOperation();
        }
        return super.endOperation();
    }

    @Override
    public IDataTranscoder beginOperation(Operation op, CheckedOutputStream out) throws IOException {
        if(operation == Operation.ANALYZE) {
            super.beginOperation(op, out);

            // Encoding mit NullStream beginnen
            for(ImageTranscoderRaw e : encoderList) {
                e.beginOperation(op, new CheckedOutputStream(OutputStream.nullOutputStream()));
            }
        } else if(operation == Operation.ENCODE){
            // Testlauf starten um den besten Codec zu ermitteln
        }
        
        return this;
    }
    
    @Override
    public boolean analyzeNecessary() {
        return true;
    }

    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException {
        if( operation == Operation.ANALYZE
        ||  operation == Operation.AUTO) {
            // Block an Codecs weiterreichen
            for(ImageTranscoderRaw e : encoderList) {
                e.encode(block, last);
            }            
        } else {
            PropraException.assertArgument(winner);        
            // Block mit dem besten Codec komprimieren
            winner.encode(block, last);
        }
    }

    @Override
    public void decode(CheckedInputStream in, IDataTarget target) throws IOException {
        throw new UnsupportedOperationException();
    }
}
