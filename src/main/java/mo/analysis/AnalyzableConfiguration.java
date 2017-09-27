package mo.analysis;

import java.io.File;
import java.util.List;
import mo.organization.Configuration;

public interface AnalyzableConfiguration extends Configuration {
	// necesito getCompatibleCreators? 
	// para un plugin de estadistica defino el creator.
	// para el plugin de tomar notas leo todos los plugins de captura y los agrego a la lista 
    List<String> getCompatibleCreators();
    // necesito agregar archivos?
    // para el plugin de notas
    // para otros tipos de plugin no se.
    void addFile(File file);
    void removeFile(File file);
    Analyzable getAnalyzer();
}
