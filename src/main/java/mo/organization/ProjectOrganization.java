package mo.organization;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mo.filemanagement.project.Project;

public class ProjectOrganization {
    public final static Logger LOGGER = Logger.getLogger(ProjectOrganization.class.getName());
    
    Project project;
    List<Participant> participants;
    List<Stage> stages;
    
    public ProjectOrganization(Project project) {
        participants = new ArrayList<>();
        stages = new ArrayList<>();
        this.project = project;
    }
    
    public void addStage(Stage stage) {
        for (Stage s : stages) {
            if (s.getName().equals(stage.getName())) {
                throw new IllegalArgumentException("Stage already exists");
            }
        }
        
        stages.add(stage);
    }
    
    public List<Stage> getStages() {
        return stages;
    }
    
    public void store() {
        File orgXml = new File(project.getFolder(), "organization.xml");
        try {
            orgXml.createNewFile();
            XElement root = new XElement("organization");
            XElement ps = new XElement("participants");

            for (Participant participant : participants) {
                XElement xParticipant = new XElement("participant");
                xParticipant.addElement("id").setString(participant.id);
                xParticipant.addElement("name").setString(participant.name);
                xParticipant.addElement("notes").setString(participant.notes);
                XElement date = new XElement("date");
                Calendar c = Calendar.getInstance();
                c.setTime(participant.date);
                date.addElement("day").setInt(c.get(Calendar.DAY_OF_MONTH));
                date.addElement("month").setInt(c.get(Calendar.MONTH));
                date.addElement("year").setInt(c.get(Calendar.YEAR));
                xParticipant.addElement(date);
                ps.addElement(xParticipant);
            }
            
            for (Stage stage : stages) {
                XElement st = new XElement("stage");
                XElement name = new XElement("name");
                name.setString(stage.getName());
                st.addElement(name);
                root.addElement(st);
            }
            XIO.writeUTF(root, new FileOutputStream(orgXml));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void restore() {
        File orgXml = new File(project.getFolder(), "organization.xml");
        if (orgXml.exists()) {
            try {
                XElement root = XIO.readUTF(new FileInputStream(orgXml));
                XElement[] ps = root.getElement("participants").getElements("participant");
                for (XElement participant : ps) {
                    Participant p = new Participant();
                    p.id = participant.getElement("id").getString();
                    p.name = participant.getElement("name").getString();
                    p.notes = participant.getElement("notes").getString();

                    SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy");
                    String day = participant.getElement("date").getElement("day").getString();
                    String month = participant.getElement("date").getElement("month").getString();
                    String year = participant.getElement("date").getElement("year").getString();
                    Date date = new Date();
                    try {
                        date = formatter.parse(day + " " + month + " " + year);
                    } catch (ParseException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                    p.date = date;

                    participants.add(p);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}
