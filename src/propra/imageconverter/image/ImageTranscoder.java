package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataCallback;
import propra.imageconverter.data.IDataFilter;
import propra.imageconverter.data.IDataTranscoder;

/**
 *
 * @author pg
 */
public class ImageTranscoder implements IDataTranscoder {
    
    protected int DATA_BLOCK_SIZE = 4096*3;
    protected ColorFormat colorFormat;
    protected IDataFilter dataFilter;
    
    /**
     *
     */
    @Override
    public void begin(IDataFilter dataFilter) {
        this.dataFilter = dataFilter;
    } 
    
    /**
     *
     * @param colorFormat
     * @param dataFilter
     */
    public void begin(  ColorFormat colorFormat, 
                        IDataFilter dataFilter) {
        begin(dataFilter);
        this.colorFormat = colorFormat;
        if(dataFilter != null) {
            dataFilter.beginFilter();
        }
    }   

    /**
     * 
     * @param out
     * @param in
     * @throws IOException 
     */
    @Override
    public void encode( RandomAccessFile out, 
                        ByteBuffer in) throws IOException {
        
        // Eingabedaten filtern
        applyFilter(in);
        
        // Daten schreiben
        out.write(in.array(), 0, in.limit());
    }
    
    /**
     * 
     * @param in
     * @param out
     * @throws IOException 
     */
    @Override
    public void decode( RandomAccessFile in, 
                        IDataCallback out) throws IOException {
        
        ByteBuffer dataBlock = ByteBuffer.allocate(DATA_BLOCK_SIZE);
        
        // Datei in Blöcken kopieren
        while(in.getFilePointer() < in.length()) {
            
            // Blockpuffer anpassen gegen Ende
            if(in.getFilePointer() + dataBlock.capacity() > in.length()) {
                dataBlock.limit(dataBlock.capacity() - (int)((in.getFilePointer() + dataBlock.capacity()) - in.length()));
            }
            
            // Block lesen und ausgeben
            in.read(dataBlock.array(), 0, dataBlock.limit());
            
            // Eingabedaten filtern
            applyFilter(dataBlock);
            
            // Daten ausgeben
            out.dataCallback(dataBlock);
        }
    }

    /**
     *
     */
    @Override
    public void end() {
        if(dataFilter != null) {
            dataFilter.endFilter();
        }
    }

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
        if(dataFilter != null) {
            dataFilter.apply(data);
        }
    }
    
    /**
     * 
     * @param out
     * @param data
     * @throws java.io.IOException
     */
    protected static void applyCallback(IDataCallback out, 
                                        ByteBuffer data) throws IOException {
        data.flip();
        out.dataCallback(data);
        data.clear();
    }
    
    /**
     *
     * @return the value of inFormat
     */
    public ColorFormat getColorFormat() {
        return colorFormat;
    }
}
