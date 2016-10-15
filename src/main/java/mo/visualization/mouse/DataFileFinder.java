package mo.visualization.mouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataFileFinder {

    public static List<File> findFilesCreatedBy(File root, String creator) {
        ArrayList<String> creators = new ArrayList<>();
        creators.add(creator);
        return findFilesCreatedBy(root, creators);
    }

    public static List<File> findFilesCreatedBy(File root, List<String> creators) {
        ArrayList<File> result = new ArrayList<>();

        try {
            //Files.walkFileTree(root.toPath(), new )
            Properties prop = new Properties();
            FileInputStream in;
            Files
                    .walk(root.toPath())
                    .filter(new Predicate<Path>() {
                        @Override
                        public boolean test(Path t) {
                            //System.out.println(t.getFileName());

                            return t.getFileName().toString().endsWith(".desc");
                        }
                    })
                    .forEach(new Consumer<Path>() {
                        @Override
                        public void accept(Path t) {
                            try {
                                String s = new String(Files.readAllBytes(t));
                                prop.load(new StringReader(s.replace("\\", "\\\\")));
                                if (prop.containsKey("creator")) {
                                    for (String creator : creators) {
                                        if (prop.get("creator").equals(creator)) {
                                            if (prop.containsKey("file")) {
                                                File f = new File(t.toFile() + "/" + prop.getProperty("file"));
                                                if (f.exists()) {
                                                    result.add(f);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(DataFileFinder.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    );
        } catch (IOException ex) {
            Logger.getLogger(DataFileFinder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public static void main(String[] args) {
        List<File> f = findFilesCreatedBy(new File("C:\\Users\\Celso\\Desktop\\ejemplo"), "mo.capture.keyboard.KeyboardRecorder");
        f.stream().forEach(System.out::println);

    }
}
