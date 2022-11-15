package propra.imageconverter.util;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author pg
 */
public class DataModel implements Validatable {
    
    protected DataTranscoder encoder;
    protected DataTranscoder decoder;
    protected RandomAccessFile readFile;
    protected RandomAccessFile writeFile;
        
    public enum IOMode {
        READ,
        WRITE,
        NONE;
    } 
    
    /**
     *
     */
    public DataModel() {
        
    }
    
    /**
     *
     * @param mode
     * @param file
     * @param coder
     */
    public DataModel(IOMode mode,
                    RandomAccessFile file, 
                    DataTranscoder coder) {
        if(mode == IOMode.READ) {
            readFile = file;
            decoder = coder;
        } else if(mode == IOMode.WRITE) {
            writeFile = file;
            encoder = coder;            
        }
    }
    
    /**
     *
     * @param mode
     */
    public void begin(IOMode mode) {
        if(mode == IOMode.READ) {
            if(decoder != null) {
                decoder.begin();
            }
        } else if(mode == IOMode.WRITE) {
            if(encoder != null) {
                encoder.begin();
            }
        }
    }
    
    /**
     *
     * @param data
     * @return
     * @throws IOException
     */
    public long write(DataBuffer data) throws IOException {
        if(!isValid()
        ||  writeFile == null) {
            throw new IllegalStateException();
        }
        
        DataBuffer writeBuffer = data;
        
        if(encoder != null) {
            writeBuffer = new DataBuffer();
            encoder.transcode(DataTranscoder.Operation.ENCODE, data, writeBuffer);
        }
        
        // Bytes in Stream schreiben
        writeFile.write(writeBuffer.getBytes(), 0, writeBuffer.getCurrDataLength());
        return writeBuffer.getCurrDataLength();
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public long read(DataBuffer buffer) throws IOException {
        if(!isValid()
        ||  readFile == null) {
            throw new IllegalStateException();
        }
        
        // Daten einlesen
        readFile.read(buffer.getBytes(), 0, buffer.getSize());
        buffer.setCurrDataLength(buffer.getSize());
        
        // Dekodierung notwendig?
        if(decoder != null) {
            
            // In temporären Puffer dekodieren
            DataBuffer decodeBuffer = new DataBuffer(buffer.getSize());
            
            decoder.transcode(  DataTranscoder.Operation.DECODE, 
                                buffer, 
                                decodeBuffer);
            
            // In Ausgabepuffer kopieren
            buffer.getBuffer().put( 0, 
                                    decodeBuffer.getBytes(), 
                                    0, 
                                    decodeBuffer.getCurrDataLength());
            
            buffer.setCurrDataLength(decodeBuffer.getCurrDataLength());
        }

        return buffer.getCurrDataLength();
    }
    
    public void end(IOMode mode) {
        if(mode == IOMode.READ) {
            if(decoder != null) {
                decoder.end();
            }
        } else if(mode == IOMode.WRITE) {
            if(encoder != null) {
                encoder.end();
            }
        }
    }
    
    /**
     *
     * @return
     */
    public long getContentSize(IOMode mode) throws IOException {
        if(mode == IOMode.READ) {
            return readFile.length();
        } else if(mode == IOMode.WRITE) {
            return writeFile.length();
        }  
        return 0;
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
}
