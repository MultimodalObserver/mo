package mo.core.plugin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.asynchttpclient.*;
import java.util.concurrent.Future;
import javax.swing.BoxLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


/**
 *
 * @author felo
 */
public class RemotePluginInstaller extends JPanel {
    
    private final String CHECK_SERVER_BUTTON_LABEL = "Use server";
    
    private final int DELAY_MILLISECONDS = 800;
    
    private final int SEARCH_LIMIT = 5;
    
    private String currentServer = null;    
    
    private JTextField serverInput = new JTextField("http://localhost:3000", 50);
    private JButton serverCheckButton = new JButton(CHECK_SERVER_BUTTON_LABEL);
    private JLabel serverNotice = new JLabel();
    private JTextField searchInput = new JTextField(50);
    
    private JLabel loadingTags = new JLabel("Loading tags...");
    private JLabel loadingPlugins = new JLabel("Loading plugins...");
    
    private JPanel tagSearchResultContainer = new JPanel();
    private JPanel pluginSearchResultContainer = new JPanel();
    
    
    private Thread searchDelay;
    
    
    private void setServerNotice(){
        
        if(currentServer == null){
            serverNotice.setText("<html><p style='color: red;'>You haven't selected a server.</p></html>");
        } else {
            serverNotice.setText("<html><p>You are using server <b>" + currentServer + "</b>.</p></html>");
        }                        
    }
    
    private static boolean validateHTTP_URI(String uri) {
        final URL url;
        try {
            url = new URL(uri);
        } catch (Exception e1) {
            return false;
        }
        return "http".equals(url.getProtocol()) || "https".equals(url.getProtocol());
    }
    
    
    public String cleanServerUrl(String url){
        
        if(url == null ) return "";
        
        String result = url.trim();
        if(result.charAt(result.length() - 1) == '/'){
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
    
    private ArrayList<HashMap<String, Object>> parseArrayJson(String json){        
        try{
            ArrayList<HashMap<String, Object>> map = new ArrayList<HashMap<String, Object>>();
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(json, new TypeReference<ArrayList<HashMap<String, Object>>>(){});
            return map;
        } catch(IOException e){
            return new ArrayList<HashMap<String, Object>>();
        }
    }
    
    private void searchByTag(String tagName){
        
        System.out.println("Buscando por tag: " + tagName);
        String pluginsUrl = cleanServerUrl(currentServer) + "/tags/"+tagName+"/plugins";
        
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
                
        asyncHttpClient.prepareGet(pluginsUrl).execute(new AsyncCompletionHandler<Response>(){                
            @Override
            public Response onCompleted(Response r) throws Exception{                        
                String json = r.getResponseBody();     
                
                /*ArrayList<HashMap<String, Object>> map = new ArrayList<HashMap<String, Object>>();
                ObjectMapper mapper = new ObjectMapper();
                map = mapper.readValue(json, new TypeReference<ArrayList<HashMap<String, Object>>>(){});*/
                
                System.out.println(json);
                return r;
            }
            @Override
            public void onThrowable(Throwable t){ 
                JOptionPane.showMessageDialog(null, "There was a connection error. Did you choose a working server?", "Error", JOptionPane.ERROR_MESSAGE);
            }                
        });
        
    }
    

    
    private void installPlugin(String name, String shortName, String description, String homepage, String repoName, String repoUser){
        
        JDialog d = new JDialog();        
        d.setModal(true);
        d.setSize(500, 500);
        d.setTitle("Install Plugin");
        
        JPanel p = new JPanel();
                
        p.add(new JLabel(name));
        p.add(new JLabel(shortName));
        p.add(new JLabel("<html>"+description+"</html>"));
        p.add(new JLabel(homepage));
        
        JLabel gettingReleases = new JLabel("Getting versions...");
        p.add(gettingReleases);     
                
        
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        
        String releasesUrl = "https://api.github.com/repos/"+repoUser+"/"+repoName+"/releases";
                
        asyncHttpClient.prepareGet(releasesUrl).execute(new AsyncCompletionHandler<Response>(){                
            @Override
            public Response onCompleted(Response r) throws Exception{                        
                String json = r.getResponseBody();     
                
                ArrayList<HashMap<String, Object>> map = new ArrayList<HashMap<String, Object>>();
                ObjectMapper mapper = new ObjectMapper();
                map = mapper.readValue(json, new TypeReference<ArrayList<HashMap<String, Object>>>(){});
                
                
                if(map.size() == 0){
                    p.add(new JLabel("This plugin doesn't have releases available for download."));
                } else {
                    JScrollPane container = new JScrollPane();
                    JPanel versionContainer = new JPanel();
                    GridLayout gridLayout = new GridLayout(0, 2);
                    versionContainer.setLayout(gridLayout);
                    
                    for(int i=0; i<map.size(); i++){
                        String tagName = (String)map.get(i).get("tag_name");                        
                        
                        JButton installBtn = new JButton("Install");
                        String zipBallUrl = (String)map.get(i).get("zipball_url");
                        
                        installBtn.addActionListener(new ActionListener(){
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                System.out.println("Descargando...");
                                System.out.println(zipBallUrl);
                            }
                        });
                        
                        versionContainer.add(installBtn);
                        versionContainer.add(new JLabel(tagName));  
                    }
                    
                    container.setViewportView(versionContainer);
                    container.setPreferredSize(new Dimension(500, 400));
                    p.add(container);
                }

                gettingReleases.setVisible(false);
                return r;
            }
            @Override
            public void onThrowable(Throwable t){ 
                JOptionPane.showMessageDialog(null, "There was a connection error. Did you choose a working server?", "Error", JOptionPane.ERROR_MESSAGE);
            }                
        });
        
        d.add(p);
        d.setVisible(true);
        
        
    }
    
    private void showTagSearchResults(String json){        
        
        ArrayList<HashMap<String, Object>> tags = parseArrayJson(json);
        
        tagSearchResultContainer.removeAll();

        for(int i=0; i<tags.size(); i++){
            String tag = (String)tags.get(i).get("short_name");
            
            JButton tagButton = new JButton("#"+tag);
            
            tagButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchByTag(tag);
                }
            });
            
            tagSearchResultContainer.add(tagButton);
        }
    }
    
    private void showPluginSearchResults(String json){        
        
        ArrayList<HashMap<String, Object>> plugins = parseArrayJson(json);
        
        pluginSearchResultContainer.removeAll();

        for(int i=0; i<plugins.size(); i++){
            
            String name = "";
            String description = "";
            String homepage = "";
            String shortName = "";
            String repoUser = "";
            String repoName = "";
            
            if(plugins.get(i).get("name") != null) name = (String)plugins.get(i).get("name");
            if(plugins.get(i).get("description") != null) description = (String)plugins.get(i).get("description");
            if(plugins.get(i).get("home_page") != null) homepage = (String)plugins.get(i).get("home_page");
            if(plugins.get(i).get("short_name") != null) shortName = (String)plugins.get(i).get("short_name");
            if(plugins.get(i).get("repo_user") != null) repoUser = (String)plugins.get(i).get("repo_user");
            if(plugins.get(i).get("repo_name") != null) repoName = (String)plugins.get(i).get("repo_name");
            
            JButton installButton = new JButton("Install");            
           
            //JPanel tagsContainerPanel = new JPanel();
            
            
            /*
            // Esto no funciona porque el servicio de busqueda no entrega
            // los tags, al parecer.
            ArrayList<HashMap<String, Object>> tagsJson = (ArrayList<HashMap<String, Object>>)plugins.get(i).get("tags");
            System.out.println("El plugin " + shortName + " tiene tags: " + tagsJson.size() );
            
            for(int j=0; j<tagsJson.size(); i++){
                String tagName = (String)tagsJson.get(i).get("short_name");
                tagsContainerPanel.add(new JLabel("#"+tagName));
            }   */         
            
            installButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {                    
                    installPlugin(name, shortName, description, homepage, repoName, repoUser);
                }
            });
            
            JPanel pluginPanel = new JPanel();
            pluginPanel.setPreferredSize(new Dimension(500, 500));
            pluginPanel.setLayout(new BoxLayout(pluginPanel, BoxLayout.Y_AXIS));
                 
            // Individual plugin
            
            pluginPanel.add(new JLabel(name));
            pluginPanel.add(new JLabel("<html>"+description+"</html>"));
            pluginPanel.add(new JLabel(homepage));
            //pluginPanel.add(tagsContainerPanel);
            pluginPanel.add(installButton);
            
            // Add individual plugin to container
            pluginSearchResultContainer.add(pluginPanel);
        }
    }
    
    
    private void querySearch(){
        
        String q = searchInput.getText().trim();
        
        System.out.println("Buscando: " + q);
        
        String tagUrl = cleanServerUrl(currentServer) + "/tags?q=" + q + "&limit=" + SEARCH_LIMIT;
        String pluginUrl = cleanServerUrl(currentServer) + "/plugins?q=" + q + "&limit=" + SEARCH_LIMIT;
        
        
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
                
        Future<Response> fTags = asyncHttpClient.prepareGet(tagUrl).execute(new AsyncCompletionHandler<Response>(){                
            @Override
            public Response onCompleted(Response r) throws Exception{                        
                String json = r.getResponseBody();                
                showTagSearchResults(json);
                loadingTags.setVisible(false);
                return r;
            }
            @Override
            public void onThrowable(Throwable t){ 
                loadingTags.setVisible(false);
                JOptionPane.showMessageDialog(null, "There was a connection error. Did you choose a working server?", "Error", JOptionPane.ERROR_MESSAGE);
            }                
        });
        
        Future<Response> fPlugins = asyncHttpClient.prepareGet(pluginUrl).execute(new AsyncCompletionHandler<Response>(){                
        @Override
        public Response onCompleted(Response r) throws Exception{                        
            String json = r.getResponseBody();
            showPluginSearchResults(json);            
            System.out.println(json);
            loadingPlugins.setVisible(false);
            return r;
        }
        @Override
        public void onThrowable(Throwable t){ 
            loadingPlugins.setVisible(false);
            JOptionPane.showMessageDialog(null, "There was a connection error. Did you choose a working server?", "Error", JOptionPane.ERROR_MESSAGE);
        }                
        });
        
    }

    
    public RemotePluginInstaller(){   
        
        searchInput.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
                if(searchDelay != null && searchDelay.isAlive()){
                    searchDelay.interrupt();
                }
                
                searchDelay = new Thread(() -> {    
                      try{
                        Thread.sleep(DELAY_MILLISECONDS);
                        querySearch();
                    } catch(InterruptedException ex){
                        // The thread was stopped
                    }                    
                });
                
                searchDelay.start();
                loadingTags.setVisible(true);
                loadingPlugins.setVisible(true);
            }

            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}        
        
        });
        
        
        
        serverCheckButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {                
                String url = serverInput.getText().trim();   
                
                if(!validateHTTP_URI(url)){
                    JOptionPane.showMessageDialog(null, "Invalid URL.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                serverCheckButton.setText("Checking...");
                serverCheckButton.setEnabled(false);
                AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
                
                Future<Response> f = asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<Response>(){                
                    @Override
                    public Response onCompleted(Response r) throws Exception{                        
                        String json = r.getResponseBody();
                        Map<String, Object> map = new HashMap<String, Object>();
                        ObjectMapper mapper = new ObjectMapper();
                        map = mapper.readValue(json, new TypeReference<Map<String, String>>(){});
                        if(map.containsKey("mo_plugin_repository")){                            
                            serverCheckButton.setText(CHECK_SERVER_BUTTON_LABEL);
                            serverCheckButton.setEnabled(true);
                            currentServer = url;
                            setServerNotice();
                            JOptionPane.showMessageDialog(null, "Server contains a plugin repository", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            throw new Exception();
                        }                        
                        return r;
                    }
                    @Override
                    public void onThrowable(Throwable t){                        
                        serverCheckButton.setText(CHECK_SERVER_BUTTON_LABEL);
                        serverCheckButton.setEnabled(true);
                        JOptionPane.showMessageDialog(null, "A plugin repository couldn't be found.", "Error", JOptionPane.ERROR_MESSAGE);
                        setServerNotice();
                    }                
                });

            }
        });
        
       
        JPanel server = new JPanel();
        server.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;        
        

        server.add(serverNotice, gbc);
        server.add(serverInput);
        server.add(serverCheckButton);        

        add(server);
        
        JPanel searchBox = new JPanel();
        searchBox.add(searchInput);
        add(searchBox, gbc);
        
        setServerNotice();        
        
        loadingTags.setVisible(false);
        loadingPlugins.setVisible(false);
        add(loadingTags);
        add(loadingPlugins);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        pluginSearchResultContainer.setLayout(new BoxLayout(pluginSearchResultContainer, BoxLayout.Y_AXIS));
        
        add(tagSearchResultContainer);
        add(pluginSearchResultContainer);
        
    }
    
    
}
