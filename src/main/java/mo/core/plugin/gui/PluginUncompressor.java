package mo.core.plugin.gui;

import com.github.junrar.extract.ExtractArchive;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import static java.nio.file.Files.deleteIfExists;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mo.core.Utils;

/**
 *
 * @author felo
 */
public class PluginUncompressor {
    
    private String tempFileName;
    
    private String fileName;
    
    private static final int BUFFER_SIZE_ZIP = 4096;
    
    private final String pluginsFolder
            = Utils.getBaseFolder() + "/plugins";
    
    private String pluginName;
    
    private String version;
    
    ByteArrayOutputStream file;
        
    public PluginUncompressor(ByteArrayOutputStream r, String fileName, String pluginName, String version){        
        this.fileName = fileName;        
        setTempName();        
        this.file = r; 
        this.pluginName = pluginName;
        this.version = version;
    }

    public boolean uncompress() throws IOException{
        
        writeTempFile();
        
        boolean compress = false;
        
        String destination = pluginsFolder + "/" + this.pluginName + "-" + this.version;
        
        try {
            
            if(fileName.endsWith(".zip")){
                compress = true;
                unzip(tempFileName, destination);

            } else if(fileName.endsWith(".rar")){
                compress = true;
                unrar(tempFileName, destination);

            } else {
                compress = false;
                
                Path destDirectory = Paths.get(pluginsFolder, this.pluginName + "-" + this.version);
                
                File destDir = new File(destDirectory.toString());
                if (!destDir.exists()) {
                    destDir.mkdir();                    
                }                                
                
                Files.copy((new File(tempFileName)).toPath(), Paths.get(destDir.getAbsolutePath(), this.fileName), StandardCopyOption.REPLACE_EXISTING);

            }
            
        } catch(IOException e){
            throw e;
            
        } finally {
            cleanTemp();
            return compress;
        }

    }
    
    
    
    private void writeTempFile() throws IOException{        
        FileOutputStream fos = new FileOutputStream(tempFileName);
        fos.write(this.file.toByteArray());
        fos.close();
    }
    
    private void setTempName(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        tempFileName = "temp" + sdf.format(cal.getTime());
    }
    
    private void cleanTemp() throws IOException{
        deleteIfExists(Paths.get(tempFileName));   
    }
    
    private void unrar(String rarFilePath, String destDirectory){
        
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        
        final File rar = new File(rarFilePath);  
        final File destinationFolder = new File(destDirectory);  
        ExtractArchive extractArchive = new ExtractArchive();  
        extractArchive.extractArchive(rar, destinationFolder);        
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
