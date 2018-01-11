package mo.core.plugin.gui;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.nio.file.Files.deleteIfExists;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author felo
 */
public class PluginUncompressor {
    
    private String tempFileName;
    
    private static final int BUFFER_SIZE_ZIP = 4096;
        
    public PluginUncompressor(ByteArrayOutputStream r, String fileName){
        
        setTempName();
        
        writeTempFile(r);
        
        
        if(fileName.endsWith(".zip")){
            System.out.println("Extension: ZIP");
            try{
                unzip(tempFileName, "uncompressed");
            } 
            catch(IOException e){            
            }
        } else if(fileName.endsWith(".rar")){
            System.out.println("Extension: RAR");
            try{
                unzip(tempFileName, "uncompressed");
            } 
            catch(IOException e){            
            }
        }
        
        cleanTemp();        
        
    }
    
    private void writeTempFile(ByteArrayOutputStream bytes){
        try (FileOutputStream fos = new FileOutputStream(tempFileName)) {
            fos.write(bytes.toByteArray());
            fos.close();
        } catch(IOException e){        
        }
    }
    
    private void setTempName(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        tempFileName = "temp" + sdf.format(cal.getTime());
    }
    
    private void cleanTemp(){        
        try {
            deleteIfExists(Paths.get(tempFileName));
        } catch(IOException e){            
        }       
    }
    
    
    
    private void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE_ZIP];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
    
    
}
