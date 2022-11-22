package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataCoder;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.IDataCallback;
import propra.imageconverter.data.IDataFilter;

/**
 *
 * @author pg
 */
public class ImageCoder implements DataCoder {
    
    protected int DATA_BLOCK_SIZE = 4096*3;
    protected ColorFormat colorFormat;
    protected IDataFilter dataFilter;
    
    /**
     *
     */
    @Override
    public void begin(  Operation op, 
                        IDataFilter dataFilter) {
        this.dataFilter = dataFilter;
    } 
    
    /**
     *
     * @param op
     * @param colorFormat
     * @param dataFilter
     */
    public void begin(  Operation op, 
                        ColorFormat colorFormat, 
                        IDataFilter dataFilter) {
        begin(op, dataFilter);
        this.colorFormat = colorFormat;
    }   

    /**
     * 
     * @param out
     * @param in
     * @throws IOException 
     */
    @Override
    public void encode(RandomAccessFile out, ByteBuffer in) throws IOException {
        
    }
    
    /**
     * 
     * @param in
     * @param out
     * @throws IOException 
     */
    @Override
    public void decode(RandomAccessFile in, IDataCallback out) throws IOException {
        
        ByteBuffer dataBlock = ByteBuffer.allocate(DATA_BLOCK_SIZE);
        
        // Datei in Blöcken kopieren
        while(in.getFilePointer() < in.length()) {
            
            // Blockpuffer anpassen gegen Ende
            if(in.getFilePointer() + dataBlock.capacity() > in.length()) {
                dataBlock.limit(dataBlock.capacity() - (int)((in.getFilePointer() + dataBlock.capacity()) - in.length()));
            }
            
            // Block lesen und ausgeben
            in.read(dataBlock.array(), 0, dataBlock.limit());
            out.dataCallback(dataBlock);
            
            // Eingabedaten filtern
            applyFilter(dataBlock);
        }
    }

    /**
     *
     */
    @Override
    public void end() {}

    /**
     *
     * @return
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * 
     * @param data
     */
    protected void applyFilter(ByteBuffer data) {
        data.flip();
        if(dataFilter != null) {
            dataFilter.apply(data);
        }
        data.clear();
    }
    
    /**
     * 
     * @param out
     * @p 
     * @throws java.io.IOExceptionaram data 
     */
    protected static void applyCallback(IDataCallback out, ByteBuffer data) throws IOException {
        data.flip();
        out.dataCallback(data);
        data.clear();
    }
    
    /**
     * 
     * @param buffer
     * @return 
     */
    @Override
    public int codedLength(ByteBuffer buffer) {
        if(buffer != null) {
            return buffer.limit();
        }
        return 0;
    }
    
    /**
     *
     * @return the value of inFormat
     */
    public ColorFormat getColorFormat() {
        return colorFormat;
    }
}
