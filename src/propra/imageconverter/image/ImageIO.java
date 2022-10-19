package propra.imageconverter.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import propra.imageconverter.util.CmdLine;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class ImageIO implements Validatable {
    
    private ImageModule inPlugin;
    private ImageModule outPlugin;
    private RandomAccessFile inStream;
    private RandomAccessFile outStream;
    
    public ImageIO() {
    }
    
    /**
     *
     * @param inStream
     * @param outStream
     * @param inPlugin
     * @param outPlugin
     */
    public ImageIO( RandomAccessFile inStream,
                    RandomAccessFile outStream,
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
    public void wrap(   RandomAccessFile inStream,
                        RandomAccessFile outStream,
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
     *
     * @param cmd
     */
    public void setup(CmdLine cmd) throws FileNotFoundException, IOException {
        if(cmd == null) {
            throw new IllegalArgumentException();
        }
        
        inStream = new RandomAccessFile(cmd.getOption(CmdLine.Options.INPUT_FILE),"r");
        inPlugin = getModule(cmd.getOption(CmdLine.Options.INPUT_EXT), 
                            inStream.length());
        if(inPlugin == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }

        String outPath = cmd.getOption(CmdLine.Options.OUTPUT_FILE);  
        // Wenn Datei nicht vorhanden, neue Datei erstellen.
        File file = new File(outPath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        outStream = new RandomAccessFile(file,"rw");
        outPlugin = getModule(cmd.getOption(CmdLine.Options.OUTPUT_EXT), 
                            outStream.length());
        if(outPlugin == null) {
            throw new IOException("Nicht unterstütztes Bildformat.");
        }
    }
    
    /**
     *
     * @throws IOException
     */
    public void beginTransfer() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Header IO
        ImageHeader inHeader = loadHeader();
        writeHeader(inHeader);
    }
    
    /**
     *
     * @throws IOException
     */
    public void finishTransfer() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Header updaten
        outStream.seek(0);
        writeHeader(outPlugin.getHeader());
    }
    
    /**
     *
     * @return 
     * @throws java.io.IOException 
     */
    public long doTransfer() throws IOException {
        if(!isValid()) {
            throw new IllegalArgumentException();
        }
        
        long len = inPlugin.getHeader().getTotalSize();
        int blockCount = (int)(len / getBlockSize());
        int blockMod = (int)(len % getBlockSize());
        DataBuffer block = new DataBuffer(getBlockSize());
        
        inPlugin.beginDataTransfer();
        outPlugin.beginDataTransfer();
        
        for(int i=0; i<blockCount; i++) {
            processBlock(block, inPlugin, outPlugin);
        }
        
        if(blockMod > 0) {
            processBlock(new DataBuffer(blockMod), inPlugin, outPlugin);
        }
        
        inPlugin.finishDataTransfer();
        outPlugin.finishDataTransfer();
        checkChecksum();
        
        return len;
    }
    
    /**
     *  Liest Header vom Stream und gibt einen allgemeinen Header
     *  zurück
     * 
     * @return 
     * @throws java.io.IOException
    */
    protected ImageHeader loadHeader() throws IOException {
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
    protected DataBuffer writeHeader(ImageHeader header) throws IOException {
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
        
        // Farbdaten umwandeln mit spezifischen Modulen
        block = inPlugin.dataIn(block);
        outPlugin.dataOut(block, inPlugin.getHeader().getColorType());
        
        // In Stream schreiben
        write(block);
        
        return block;
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws java.io.IOException
     */
    protected int read(DataBuffer buffer) throws IOException {
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
    protected void write(DataBuffer buffer) throws IOException {
        if(!isValid()
        || buffer == null) {
            throw new IllegalStateException();
        }
        outStream.write(buffer.getBytes());
    }
    
    /**
     *
     * @throws java.io.IOException
     */
    public void checkChecksum() throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        if(inPlugin.isCheckable()) {
            if(inPlugin.getChecksumObj().getValue() 
            != inPlugin.getHeader().getChecksum()) {
                throw new IOException("Eingabe Prüfsummenfehler!");
            }
        }
        if(outPlugin.isCheckable()
        && inPlugin.isCheckable()) {
            if(inPlugin.getChecksumObj().getValue() 
            != inPlugin.getHeader().getChecksum()) {
                throw new IOException("Ausgabe Prüfsummenfehler!");
            }
        }
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
    
    /**
     *
     * @param ext
     * @param streamLen
     * @return
     */
    private static ImageModule getModule(String ext, long streamLen) {
        switch(ext) {
            case "tga":
                return new ImageModuleTGA(streamLen);
            case "propra":
                return new ImageModuleProPra(streamLen);
        }
        return null;
    }
}
