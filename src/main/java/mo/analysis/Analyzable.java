package mo.analysis;

public interface Analyzable {
    
	// para el caso del plugin de toma de notas, tengo que leer el archivo que guardo con notas
	// para el caso de un plugin de estadisticas tengo que leer el archivo de captura
    void read(long startTime, long endTime);

    // para el caso del plugin de toma de notas, tengo que escribir el archivo de las notas
    // para el caso del plugins de estadisticas, que tengo que escribir? puedo escribir nada?
    void write(long startTime, long endTime);

}