package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class ImageIO implements Validatable {
    
    protected ImageModule inPlugin;
    protected ImageModule outPlugin;
    BufferedInputStream inStream;
    BufferedOutputStream outStream;
    
    public ImageIO() {
    }
    
    /**
     *
     * @param inStream
     * @param outStream
     * @param inPlugin
     * @param outPlugin
     */
    public ImageIO(BufferedInputStream inStream,
                        BufferedOutputStream outStream,
                        ImageModule inPlugin,
                        ImageModule outPlugin) {
        wrap(inStream,
             outStream,
             inPlugin,
             outPlugin);
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return (inStream    != null 
            &&  outStream   != null
            &&  inPlugin    != null
            &&  outPlugin   != null);
    }
    
    /**
     *
     * @param inStream
     * @param outStream
     * @param inPlugin
     * @param outPlugin
     */
    public void wrap(   BufferedInputStream inStream,
                        BufferedOutputStream outStream,
                        ImageModule inPlugin,
                        ImageModule outPlugin) {
        if( inStream == null
        ||  outStream == null
        ||  inPlugin == null
        ||  outPlugin == null) {
            throw new IllegalArgumentException();
        }
        
        this.inStream = inStream;
        this.outStream = outStream;
        this.inPlugin = inPlugin;
        this.outPlugin = outPlugin;
    }
    
    /**
     *  Liest Header vom Stream und gibt einen allgemeinen Header
     *  zurück
     * 
     * @return 
     * @throws java.io.IOException
    */
    public ImageHeader loadHeader() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Header-Bytes von Stream lesen
        DataBuffer rawBytes = new DataBuffer(inPlugin.getHeaderSize());
        if(read( rawBytes) != inPlugin.getHeaderSize()) {
            throw new java.io.IOException("Ungültiger Dateikopf!");
        }
        
        // In logischen Header umwandeln und für Ausgabe merken
        return inPlugin.headerIn(rawBytes);
    }
    
    
    /**
     * @param header
     * @return 
     * @throws java.io.IOException
    */
    public DataBuffer writeHeader(ImageHeader header) throws IOException {
        if(!isValid()
        || header == null) {
            throw new IllegalStateException();
        }
   
        // In Formatheader umwandeln und in den Stream schreiben
        DataBuffer rawBytes = outPlugin.headerOut(header);
        outStream.write(rawBytes.getBytes());
        return rawBytes;
    }
    
    
    /**
     *
     * @return 
     * @throws java.io.IOException 
     */
    public long transferData() throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }
        
        long len = inPlugin.getHeader().getTotalSize();
        int blockCount = (int)(len / getBlockSize());
        int blockMod = (int)(len % getBlockSize());
        DataBuffer block = new DataBuffer(getBlockSize());
        
        for(int i=0; i<blockCount; i++) {
            processBlock(block, inPlugin, outPlugin);
        }
        
        if(blockMod > 0) {
            processBlock(new DataBuffer(blockMod), inPlugin, outPlugin);
        }
        
        return len;
    }
    
    /**
     *
     * @param block
     * @param inPlugin
     * @param outPlugin
     * @return
     * @throws IOException
     */
    protected DataBuffer processBlock(DataBuffer block, ImageModule inPlugin, ImageModule outPlugin) throws IOException {
        if(!isValid()
        || inPlugin == null
        || outPlugin == null
        || block == null) {
            throw new IllegalArgumentException();
        }    
        
        // Farbdaten lesen
        if(read(block) != block.getSize()) {
            throw new IOException("IO Fehler!");
        }
        
        // Eingabe Prüfsumme updaten für den aktuellen Block
        if(inPlugin.isCheckable()) {
            inPlugin.check(block.getBytes());
        }
        
        // Farbdaten umwandeln mit spezifischen Modulen
        block = inPlugin.dataIn(block);
        outPlugin.dataOut(block, inPlugin.getHeader().getColorType());
        
        // In Stream schreiben
        write(block);
        outStream.flush();
        
        // Ausgabe Prüfsumme updaten für den aktuellen Block
        if(outPlugin.isCheckable()) {
            outPlugin.check(block.getBytes());
        }
        
        return block;
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws java.io.IOException
     */
    public int read(DataBuffer buffer) throws IOException {
        if(!isValid()
        || buffer == null) {
            throw new IllegalStateException();
        }
        return inStream.read(buffer.getBytes());
    }
    
    /**
     *
     * @param buffer
     * @throws java.io.IOException
     */
    public void write(DataBuffer buffer) throws IOException {
        if(!isValid()
        || buffer == null) {
            throw new IllegalStateException();
        }
        outStream.write(buffer.getBytes());
    }
    
    /**
     *
     * @return
     */
    public long getInChecksum() {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        if(inPlugin.isCheckable()) {
            return inPlugin.getChecksumObj().getValue();
        }
        return 0;
    }
    
    /**
     *
     * @return
     */
    public long getOutChecksum() {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        if(outPlugin.isCheckable()) {
            return outPlugin.getChecksumObj().getValue();
        }
        return 0;
    }
    
    /**
     *
     * @return
     */
    public int getBlockSize() {
        return 4096 * 3;
    }

    public ImageModule getInPlugin() {
        return inPlugin;
    }

    public ImageModule getOutPlugin() {
        return outPlugin;
    }
}
