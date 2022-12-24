package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataCompression;
import propra.imageconverter.data.IDataTarget;

/**
 * Filtert übergebene Farben mit ColorOp Objekt und leitet diese weiter an 
 * ein weiteres Datenziel.
 */
public class ColorFilter implements IDataTarget {
    
    // Ziel an das die Farben weitergeleitet werden
    private final IDataTarget target;
    
    // Operation die auf den Farben ausgeführt wird
    private final ColorOp op;

    public ColorFilter(ColorOp op, IDataTarget target) {
        this.target = target;
        this.op = op;
    }
    
    /**
     * 
     */
    @Override
    public void onData( Event event, IDataCompression caller, 
                        ByteBuffer data, boolean lastBlock) throws IOException {
        if(op != null) {
            ColorOperations.filterColorBuffer(data, data, op);
        }
        target.onData(event, caller, data, lastBlock);
    }
}
