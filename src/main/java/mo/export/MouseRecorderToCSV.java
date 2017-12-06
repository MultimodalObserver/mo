package mo.export;

import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.DataFileFinder;
import mo.organization.ProjectOrganization;
import mo.export.plugin.ExportDataPlugin;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;

@Extension(
    xtends = {
        @Extends(
                extensionPointId = "mo.export.plugin.ExportDataPlugin"
        )
    }
)
public class MouseRecorderToCSV implements ExportDataPlugin {
	private ProjectOrganization organization;
	private List<String> creators = Arrays.asList("mo.mouse.capture.MouseRecorder");
	private List<File> locations;
	private List<File> targetFiles;
	private File root;
	private File tmpDir;

	public final static Logger logger = Logger.getLogger(ExportProject.class.getName());

	@Override
	public String getCodeName() {
		return "MouseRecorderToCSV";
	}

	@Override
	public String getName() {
		return "MouseRecorder to CSV";
	}

	@Override
	public String getDescription() {
		return "Un plugin que permite exportar los datos desde MouseRecorder a un formato CSV";
	}

	@Override
	public void setOrganization(ProjectOrganization organization) {
		this.organization = organization;
	}

	@Override
	public void setDataSourcesLocations(List<File> locations) {
		this.locations = locations;
	}

	@Override
	public void setExportLocationRoot(File root) {
		this.root = root;
	}

	@Override
	public List<String> getCompatibleCreators() {
		return creators;
	}


	@Override
	public void export() {
		try {
			tmpDir = makeTemporaryDirectoryIn(root.getParentFile());

			targetFiles = findFilesCreatedByMouseRecorder();

			copyDataToRoot();

			renameProject(tmpDir,root);
		} catch(Exception ignore) {}
	}

	@Override
	public void cancelExport() {
		try {
			FileUtils.deleteDirectory(root);
		} catch(IOException ex) {
			Logger.getLogger(IOException.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private File makeTemporaryDirectoryIn(File location) {
		File dir = new File(location,"tmp");
		dir.mkdir();
		return dir;
	}

	private List<File> findFilesCreatedByMouseRecorder() {
		List<File> result = new ArrayList();
		List<File> partialResult;
		for (File location : locations) {
			partialResult = DataFileFinder.findFilesCreatedBy(location, creators);
			result.addAll(partialResult);
		}

		return result;
	}

	private int getLastDotIndexOf(String fileName) {
		int i = fileName.length()-1;
		while(i >= 0) {
			if(fileName.charAt(i) == '.')	return i;

			i--;
		}

		return -1;
	}

	private String getNameWithoutExtensionOf(File file) {
		String fileName = file.getName();
		String fileNameWithoutExtension = "";

		int i=0;
		int j = getLastDotIndexOf(fileName);
		while(i < j) {
			fileNameWithoutExtension = fileNameWithoutExtension + fileName.charAt(i);
			i++;
		}

		return fileNameWithoutExtension;
	}

	private void copyDataToRoot() {
		for (File file : targetFiles) {
			copyFileToRoot(file);
		}
	}

	private void copyFileToRoot(File file) {
		String name = getNameWithCsvExtension(file);
		File srcFile = new File(tmpDir,name);

		try {
			FileUtils.copyFile(file,srcFile);
		} catch(IOException ignore) {
		}
	}

	private String getNameWithCsvExtension(File file) {
		String fileNameWithoutExtension = getNameWithoutExtensionOf(file);
		String csvName = fileNameWithoutExtension + ".csv";

		return csvName;
	}

	private void renameProject(File oldNameProject, File newNameProject) {
		try {
			Files.move(oldNameProject.toPath(),newNameProject.toPath());
        } catch (IOException ex) {
            Logger.getLogger(IOException.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}