package propra.imageconverter.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class DataModel implements Validatable {
    
    protected DataTranscoder encoder;
    protected DataTranscoder decoder;
    protected RandomAccessFile readFile;
    protected RandomAccessFile writeFile;
    protected BufferedReader txtReader;    
    protected BufferedWriter txtWriter;     
    
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
     * @param coder
     */
    public DataModel(IOMode mode, 
                    DataTranscoder coder) {
        if(mode == IOMode.READ) {
            decoder = coder;
        } else if(mode == IOMode.WRITE) {
            encoder = coder;            
        }
    }
    
    /**
     *
     * @param txtReader
     * @param txtWriter
     */
    public void SetInputOutput( BufferedReader txtReader, 
                                BufferedWriter txtWriter) {
        this.txtReader = txtReader;
        this.txtWriter = txtWriter;
    }
    
        /**
     *
     * @param binReader
     * @param binWriter
     */
    public void SetInputOutput( RandomAccessFile binReader, 
                                RandomAccessFile binWriter) {
        this.readFile = binReader;
        this.writeFile = binWriter;
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
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        DataBuffer writeBuffer = data;
        
        if(encoder != null) {
            writeBuffer = new DataBuffer();
            encoder.transcode(DataTranscoder.Operation.ENCODE, data, writeBuffer);
            
            // Alphabet in Datei schreiben
            if(encoder.getDataFormat().isBaseN()) {
                txtWriter.write(encoder.getDataFormat().getAlphabet() + "\n");
            }
            
            // Zeichen in Datei schreiben
            writeBinaryToTextFile(writeBuffer);
        } else {
            // Bytes in Stream schreiben
            writeFile.write(writeBuffer.getBytes(), 0, writeBuffer.getCurrDataLength());
            return writeBuffer.getCurrDataLength();
        }
        
        return 0;
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public long read(DataBuffer buffer) throws IOException {
        if(!isValid()) {
            throw new IllegalStateException();
        }
        
        // Dekodierung notwendig?
        if(decoder != null) {
            
            buffer.setCurrDataLength(buffer.getSize());
            
            // Alphabet vorhanden?
            if(decoder.getDataFormat().getAlphabet().length() == 0) {
                
                // Alphabet aus Datei einlesen und DatenFormat ableiten
                String alphabet = readFile.readLine();
  
                decoder.getDataFormat().setEncoding(alphabet);
                buffer.setCurrDataLength(buffer.getSize() - alphabet.length() - 1);
            }
            
            // Daten einlesen
            readFile.read(buffer.getBytes(), 0, buffer.getCurrDataLength());

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
            
        } else {
            // Binärdaten einlesen
            readFile.read(buffer.getBytes(), 0, buffer.getSize());
            buffer.setCurrDataLength(buffer.getSize());
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
     * @param buffer
     * @throws IOException 
     */
    private void writeBinaryToTextFile(DataBuffer buffer) throws IOException {
        if(buffer == null) {
            throw new IllegalArgumentException();
        }
        
        while(buffer.getBuffer().hasRemaining()) {
            txtWriter.write(buffer.getBuffer().get());
        }
        txtWriter.flush();
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
