package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.IDataTarget;

/**
 *
 * @author pg
 */
public class ImageCodecRLE extends ImageCodec {

    private ByteBuffer writeRleBuffer;
    private ByteBuffer tmpRleBuffer;

    /**
     * 
     * @param resource
     * @param checksum 
     */
    public ImageCodecRLE(   ImageResource resource, 
                            Checksum checksum) {
        super(resource, checksum);
        
        writeRleBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        tmpRleBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE * 2);
    }
    
    /**
     * 
     * @param outBlock
     * @param target
     * @throws java.io.IOException
     */
    public void decode( DataBlock outBlock, IDataTarget target) throws IOException {
        
        if(!isValid()
        ||  outBlock == null
        ||  target == null) {
            throw new IllegalArgumentException();
        }
        
        byte[] readRleColor = new byte[3];
        int readColorCounter = 0;
        int readPacketHeader = 0;
        
        // Temporäre Variablen zur Performanceoptimierung
        int inOffset = (int)resource.position();
        int inLength = (int)resource.length();
        int outOffset = 0;
        byte r,g,b;
    
        ByteBuffer readBlock = tmpRleBuffer;
        
        if(outBlock.data == null) {
            outBlock.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        }
        
        ByteBuffer dataOut = outBlock.data;
        byte[] outBytes = dataOut.array();
        
        // Blockweise Daten einlesen und dekodieren bis Ausgabeblock gefüllt
        while( inOffset < inLength ) {
            
            // Blockgröße anpassen gegen Ende
            if(inOffset + readBlock.capacity() > inLength) {
                readBlock.limit(readBlock.capacity() - ((inOffset + readBlock.capacity()) - inLength));
            }
            
            // Block lesen
            int len = resource.read(readBlock);
            inOffset += len;
            
            // Block gemäß RLE verarbeiten
            while(readBlock.remaining() != 0) {

                // Paketkopf und Wiederholungen einlesen
                readPacketHeader =  readBlock.get() & 0xFF;
                readColorCounter = getRepetitionCount(readPacketHeader);
                
                // Anzahl der zu extrahierenden Bytes
                int currentBytes = readColorCounter * 3;

                // Wenn gefüllt, Datenblock zum Empfänger senden
                if( dataOut.position() + currentBytes > dataOut.capacity()) {
                    pushDataToTarget(target, outBlock);
                }

                // RLE oder RAW Paket?
                if(isRlePacket(readPacketHeader)) {
                    
                    // Neuen Leseblock beginnen?
                    if(readBlock.remaining() < 3) {
                        inOffset = inOffset - readBlock.remaining() - 1;
                        resource.position(inOffset);
                        break;
                    }
                    
                    // Farbwert auslesen
                    readBlock.get(readRleColor);
                    
                    // Farbwert in Ausgabebuffer schreiben
                    r = readRleColor[0];
                    g = readRleColor[1];
                    b = readRleColor[2];

                    outOffset = dataOut.position();
                    for(int i=0;i<readColorCounter;i++) {
                        outBytes[outOffset++] = r;
                        outBytes[outOffset++] = g;
                        outBytes[outOffset++] = b; 
                    }
                    dataOut.position(outOffset);
                } else {
                    
                    // Neuen Leseblock beginnen?
                    if(readBlock.remaining() < currentBytes) {
                        inOffset = inOffset - readBlock.remaining() - 1;
                        resource.position(inOffset);
                        break;
                    } 

                    // RAW Farben übertragen
                    dataOut.put(readBlock.array(), 
                                readBlock.position(), 
                                currentBytes);
                    readBlock.position(readBlock.position() + currentBytes);
                }
            }

            // Eingabedaten filtern
            if(checksum != null) {
                readBlock.flip();
                checksum.apply(readBlock);
            }
            
            readBlock.clear();
        } 
        
        // Restdaten übertragen
        if(resource.position() == resource.length()) {
            outBlock.lastBlock = true;
        }
        pushDataToTarget(target, outBlock);
    }
    /**
     * 
     */
    public void encode( DataBlock block) throws IOException{
        
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        int colorSize = 3;
        byte[] writeRleColor = new byte[3];
        
        ByteBuffer rleLine = ByteBuffer.allocate(192*3);
        ByteBuffer in = tmpRleBuffer;
                
        tmpRleBuffer.clear();
        
        // Gepufferter Block vorhanden?
        if(writeRleBuffer.position() > 0) {
            writeRleBuffer.flip();
            tmpRleBuffer.put(writeRleBuffer);
            writeRleBuffer.clear();
        }
        tmpRleBuffer.put(block.data);
        tmpRleBuffer.flip();
        
        if(block.lastBlock) {
            block.lastBlock = true;
        }
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(in.position() < in.limit()) {
            
            // Anzahl gleicher Farben zählen
            int writeColorCounter = countRleColor(in);
            if( writeColorCounter == -1
            &&  !block.lastBlock) {
                writeRleBuffer.put(0, in, in.position(), in.remaining());
                writeRleBuffer.position(in.remaining());
                return;
                
            } else if(writeColorCounter > 1 ) {
                
                if(writeColorCounter == -1) {
                    writeColorCounter = in.remaining() / 3;
                }
                
                in.get(writeRleColor);
                
                // RLE Block verarbeiten
                rleLine.put((byte)(127 + writeColorCounter));
                rleLine.put(writeRleColor);

                // Paketkopf und Farbwert schreiben
                rleLine.flip();
                resource.write(rleLine);
                rleLine.clear();

                // Gleiche Farben im Eingabepuffer überspringen
                in.position(in.position() + (writeColorCounter - 1) * colorSize);

                writeColorCounter = 0;

            } else {
                int writeRawCounter = countDifferentColors(in);
                if( writeRawCounter == -1
                &&  !block.lastBlock) {
                    
                    writeRleBuffer.put(0, in, in.position(), in.remaining());
                    writeRleBuffer.position(in.remaining());
                    return;
                    
                } else {
                    
                    if(writeRawCounter == -1) {
                        writeRawCounter = in.remaining() / 3;
                    }
                    
                    // Line speichern
                    rleLine.put((byte)(writeRawCounter - 1));
                    rleLine.put(in.array(), in.position(), writeRawCounter * 3);
                    
                    in.position(in.position() + writeRawCounter * 3);
                    
                    rleLine.flip();
                    resource.write(rleLine); 
                    rleLine.clear();
                    
                    writeRawCounter = 0;
                }
            }
        }
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    /** 
     *
     * @return
     */
    private static boolean isRlePacket(int packet) {
        return packet > 127;
    }
    
    /**
     *
     * @return
     */
    private static int getRepetitionCount(int packet) {
        return (packet & 127) + 1;
    }
    
    /**
     * 
     * @param data
     * @return 
     */
    private int countRleColor(ByteBuffer data) {
        byte[] array = data.array();
        int baseOffset = data.position();
        int runningOffset = baseOffset + 3;
        int counter = -1;

        if(runningOffset < data.limit()) {
            counter = 1;

            while ( compareColor(array, baseOffset, runningOffset)
            &&      counter < 127) {
                // Zähler max, oder Ende erreicht?
                if(runningOffset == data.limit() - 3) {
                    return -1;
                }
                
                counter++;
                runningOffset += 3;         
            }
        }
        
        return counter;
    }
    
    /**
     * 
     * @param data
     * @return 
     */
    private int countDifferentColors(ByteBuffer data) {
        
        byte[] src = data.array();
        int offset = data.position();
        int counter = -1;
        
        if(offset < data.limit()) {
            counter = 1;
            
            while (!compareColor(src, offset, offset + 3)
                && counter < 128) {

                if(offset == data.limit() - 3) {
                    return -1;
                }

                counter++;
                offset += 3;
            }
        }

        return counter;
    }
   
    /**
     * 
     * @param array
     * @param offset0
     * @param offset1
     * @return 
     */
    boolean compareColor(byte[] array, int offset0, int offset1) {
        return (array[offset0 + 0] == array[offset1 + 0]
            &&  array[offset0 + 1] == array[offset1 + 1]
            &&  array[offset0 + 2] == array[offset1 + 2]);
    }
}
