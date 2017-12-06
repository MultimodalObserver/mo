package mo.export;

import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.organization.ProjectOrganization;
import mo.organization.Participant;
import mo.export.plugin.ExportProjectPlugin;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;
import java.util.Iterator;
import java.util.Calendar;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import bibliothek.util.xml.XAttribute;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;

@Extension(
    xtends = {
        @Extends(
                extensionPointId = "mo.export.plugin.ExportProjectPlugin"
        )
    }
)
public class ExportProject implements ExportProjectPlugin {

	public final static Logger logger = Logger.getLogger(ExportProject.class.getName());

	private ProjectOrganization organization;
	private File exportFolderRoot;
	private List<File> originFolders;
	private File workingFolder;
	private File tmpDir;

	@Override
	public String getCodeName() {
		return "EXPORTPROJECT";
	}

	@Override
	public String getName() {
		return "Exportar proyecto";
	}

	@Override
	public String getDescription() {
		return "Un plugin que permite exportar un projecto de MO";
	}

	@Override
	public void setOrganization(ProjectOrganization organization) {
		this.organization = organization;
	}

	@Override
	public void setDataSourcesLocations(List<File> originFolders) {
		this.originFolders = originFolders;
	}

	@Override
	public void setExportLocationRoot(File exportFolderRoot) {
		this.exportFolderRoot = exportFolderRoot;
	}

	@Override
	public void export() {
		try {
			tmpDir = makeTemporaryDirectory(exportFolderRoot);

			copyProjectStructure(organization.getLocation(), tmpDir);

			copyData(originFolders, tmpDir);

			updateOrganization(tmpDir);

			renameProject(tmpDir,exportFolderRoot);

		} catch (Exception ignore) {}
	}

	@Override
	public void cancelExport() {
		try {
			FileUtils.deleteDirectory(exportFolderRoot);
		} catch(IOException ex) {
			Logger.getLogger(IOException.class.getName()).log(Level.SEVERE, null, ex);
		}
	}


	private void updateOrganization(File project) {
		List<String> folders = new ArrayList();

		for (File file : originFolders) {
			folders.add(file.getParentFile().getName());
		}

		try {

			File org = new File(project, "organization.xml");
			XElement root = XIO.readUTF(new FileInputStream(org));
        	XElement xParticipants = root.getElement("participants");
        	XElement[] xStages = root.getElements("stage");

        	if (xParticipants != null) {
        		XElement[] ps = root.getElement("participants").getElements("participant");
        		List<Participant> participants = new ArrayList();
                for (XElement participant : ps) {
                    Participant p = new Participant();
                    p.id = participant.getElement("id").getString();
                    p.name = participant.getElement("name").getString();
                    p.notes = participant.getElement("notes").getString();
                    p.folder = participant.getElement("folder").getString();

                    SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy");
                    String day = participant.getElement("date").getElement("day").getString();
                    String month = participant.getElement("date").getElement("month").getString();
                    String year = participant.getElement("date").getElement("year").getString();
                    Date date = new Date();
                    try {
                        date = formatter.parse(day + " " + (Integer.parseInt(month) + 1) + " " + year);
                    } catch (ParseException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    p.date = date;

                    if (participant.attributeExists("isLocked")) {
                        p.isLocked = participant.getAttribute("isLocked").getBoolean();
                    }

                    participants.add(p);
                }

                Iterator<Participant> iter = participants.iterator();
				while (iter.hasNext()) {
    			String str = iter.next().folder;
				    if (!contains(str,folders))
				        iter.remove();
				}

                XElement updatedParticipants = new XElement("participants");
                for (Participant participant : participants) {
	                XElement xParticipant = new XElement("participant");
	                xParticipant.addElement("id").setString(participant.id);
	                xParticipant.addElement("name").setString(participant.name);
	                xParticipant.addElement("notes").setString(participant.notes);
	                xParticipant.addElement("folder").setString(participant.folder);
	                XElement date = new XElement("date");
	                Calendar c = Calendar.getInstance();
	                c.setTime(participant.date);
	                date.addElement("day").setInt(c.get(Calendar.DAY_OF_MONTH));
	                date.addElement("month").setInt(c.get(Calendar.MONTH));
	                date.addElement("year").setInt(c.get(Calendar.YEAR));
	                xParticipant.addElement(date);
	                XAttribute locked = new XAttribute("isLocked");
	                locked.setBoolean(participant.isLocked);
	                xParticipant.addAttribute(locked);
	                updatedParticipants.addElement(xParticipant);
            	}
            	root = new XElement("organization");
            	root.addElement(updatedParticipants);
            	for (XElement st : xStages) {
            		root.addElement(st);
            	}
            	XIO.writeUTF(root, new FileOutputStream(org));
    		}
		} catch(IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	private boolean contains(String folder, List<String> folders) {
		for (String f : folders) {
			if (folder.equals(f)) {
				return true;
			}
		}
		return false;
	}

	private File makeTemporaryDirectory(File location) {
		File dir = new File(location.getParentFile(),"tmp");
		dir.mkdir();
		return dir;
	}

	private void copyProjectStructure(File source, File target) {
		File[] listFiles = source.listFiles();
		try {
			for (File file : listFiles) {
				if (file.isDirectory()) {
					if (!file.getName().startsWith("participant-")) {
						FileUtils.copyDirectoryToDirectory(file, target);
					}
				} else {
					FileUtils.copyFileToDirectory(file,target);
				}
			}
        } catch (IOException ex) {
            Logger.getLogger(IOException.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	private void copyData(List<File> originsFolders, File target) {
		try {
			for (File file : originFolders) {
				if (file.isDirectory()) {
					File participantDir = new File(target,file.getParentFile().getName());
					participantDir.mkdir();
					FileUtils.copyDirectoryToDirectory(file, participantDir);
					File[] dataFiles = file.listFiles();
				}
			}
		} catch (IOException ex) {
            Logger.getLogger(IOException.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	private void renameProject(File oldNameProject, File newNameProject) {
		try {
			Files.move(oldNameProject.toPath(),newNameProject.toPath());
        } catch (IOException ex) {
            Logger.getLogger(IOException.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}