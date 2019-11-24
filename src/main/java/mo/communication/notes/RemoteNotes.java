package mo.communication.notes;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.communication.Command;
import mo.communication.CommunicationConfiguration;
import mo.communication.ConnectionListener;
import mo.communication.PetitionResponse;

public class RemoteNotes implements CommunicationConfiguration, ConnectionListener{
    
    File storageFolder;
    File parentDir; //directorio en donde se guardará el archivo: partenrDir + \Analysis\ +nameFile.txt
    String path;
    String fileName;
    public RemoteNotes(String id){
        
    }
    

    public void createFileName(){
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
        String reportDate = df.format(now);
        this.fileName = reportDate + "_notes.txt";
    }
    
    public void saveNote(HashMap<String,Object> map){

        if(storageFolder != null){
            BufferedWriter output = null;
            try {
                long currentLocalTime = System.currentTimeMillis();
                String name = String.valueOf(map.get("name"));
                String noteContent = String.valueOf(map.get("noteContent"));
                System.out.println("remoteTimeBegin tiene valor "+map.get("timeBegin")+" clase: ");
                System.out.println("remoteTimeEnd tiene valor "+map.get("timeEnd")+" clase: ");
                long remoteTimeBegin = Long.parseLong(String.valueOf(map.get("timeBegin")));
                long remoteTimeEnd = Long.parseLong(String.valueOf(map.get("timeEnd")));
                
                long phase = remoteTimeEnd-remoteTimeBegin;
                
                String note = String.valueOf(currentLocalTime)
                        +","
                        +String.valueOf(currentLocalTime-phase)
                        +","
                        +name
                        +": "
                        +noteContent
                        +"\n\n";
                
                output = new BufferedWriter(new FileWriter(storageFolder, true));
                output.write(note);
                output.newLine();
                output.flush();
                //output.append(msg);
                output.close();
                System.out.println("GUARDANDO EN "+storageFolder.getAbsolutePath());
                return;
            } catch (IOException ex) {
                try {output.close();} catch (IOException ex1) {}
                Logger.getLogger(RemoteNotes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    @Override
    public void showPlayer() {
        // DO NOTHING
    }

    @Override
    public void closePlayer() {
        // DO NOTHING
    }

    @Override
    public void onMessageReceived(Object obj, PetitionResponse pr) {
        if(pr != null && pr.getHashMap() != null && pr.getType().equals(Command.TAKE_NOTE)){
            saveNote(pr.getHashMap());
        }
    }

    @Override
    public void setInfo(File parentDir, String name) {
        try{
            if(!parentDir.equals(this.parentDir)){
                this.parentDir = parentDir;
                createFileName();
                File path = new File(this.parentDir.getParent()+"\\analysis");
                if(!path.exists())
                    path.mkdir();
                this.storageFolder = new File(this.parentDir.getParent()+"\\analysis\\"+this.fileName);
                this.storageFolder.createNewFile();
            }
        }catch(NullPointerException | IOException ex){}
    }
}
