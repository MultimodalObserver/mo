package mo.pruebas;

import mo.core.Utils;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Celso
 */
public class Preferences3 {

    public void load() {
        Configurations configs = new Configurations();
        File pref = new File(Utils.getBaseFolder()
                + "/preferences.xml");

        if (!pref.isFile()) {
            createPreferencesFile(pref);
        } else {
            System.out.println(":)");
            try {
                Configuration config = configs.xml(pref);
                config.addProperty("projects.p", "asd");
                //config.setReloadingStrategy(new FileChangedReloadingStrategy());
                //config.setAutoSave(true);
                //config.s
                // access configuration properties
            } catch (org.apache.commons.configuration2.ex.ConfigurationException ex) {
                Logger.getLogger(Preferences3.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        Preferences3 p = new Preferences3();
        p.load();
    }

    private void createPreferencesFile(File pref) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder buider;

            buider = factory.newDocumentBuilder();
            Document doc = buider.newDocument();
            Element mainRootElement = doc.createElement("configuration");

            doc.appendChild(mainRootElement);

            // output DOM XML to console 
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult file = new StreamResult(pref);
            transformer.transform(source, file);
        } catch (TransformerException | ParserConfigurationException ex) {
            Logger.getLogger(Preferences3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
