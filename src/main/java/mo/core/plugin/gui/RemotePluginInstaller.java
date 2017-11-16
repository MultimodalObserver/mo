package mo.core.plugin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.asynchttpclient.*;
import java.util.concurrent.Future;
import javax.swing.BoxLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import mo.core.MultimodalObserver;
import mo.core.preferences.AppPreferencesWrapper;
import mo.core.preferences.PreferencesManager;
import mo.core.ui.Utils;



class Plugin {
    
    String name, desc, homePage, shortName, repoUser, repoName;
    
    Plugin(String name, String desc, String homePage, String shortName, String repoUser, String repoName){
        this.name = name;
        this.desc = desc;
        this.homePage = homePage;
        this.shortName = shortName;
        this.repoUser = repoUser;
        this.repoName = repoName;
    
    }
    @Override
    public String toString(){
        return "Plugin: " + this.name;
    }
}

class TagNode extends DefaultMutableTreeNode{
    
    String shortName;
    
    TagNode(String shortName){
        super(shortName);
        this.shortName = shortName;
    }
}

class RemotePluginInfo extends JPanel{
    
    Plugin plugin;
    RemotePluginInstaller container;
    
    RemotePluginInfo(Plugin plugin, RemotePluginInstaller container){
        this.plugin = plugin;
        this.container = container;
        
        
        TupleList tuples = new TupleList();
        
        tuples.addTuple("Name", plugin.name);        
        tuples.addTuple("Website", plugin.homePage);
        tuples.addScrollText("Description", plugin.desc);
        
        JButton installBtn = new JButton("Install");
        
        installBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                container.installPlugin(plugin);
            }
        });
        
        tuples.addTuple("", installBtn);
        
        Title pluginTitle = new Title(plugin.name);
        
        JPanel top = new JPanel();
        top.add(pluginTitle);
        
        setLayout(new BorderLayout());
        
        add(top, BorderLayout.NORTH);
        add(tuples, BorderLayout.CENTER);

    }
}



/**
 *
 * @author felo
 */
public final class RemotePluginInstaller extends JPanel {
    
    private final String CHECK_SERVER_BUTTON_LABEL = "Use server";
    
    private final int DELAY_MILLISECONDS = 800;
    
    private final int SEARCH_LIMIT = 5;
    
    private String currentServer = null;    
    
    private JTextField serverInput = new JTextField("http://localhost:3000", 50);
    private JButton serverCheckButton = new JButton(CHECK_SERVER_BUTTON_LABEL);
    private JLabel serverNotice = new JLabel();
    private JTextField searchInput = new JTextField(50);
    
    
    //private JPanel tagSearchResultContainer = new JPanel();
    //private JPanel pluginSearchResultContainer = new JPanel();
    
    //private JTree searchResults;
    DefaultMutableTreeNode tagsNode = new DefaultMutableTreeNode("Tags");
    DefaultMutableTreeNode pluginsNode = new DefaultMutableTreeNode("Plugins");
    
    SplitPaneTriple split;
    
    Spinner searchingSpinner;
    
    AppPreferencesWrapper prefs
                    = (AppPreferencesWrapper) PreferencesManager.loadOrCreate(
                            AppPreferencesWrapper.class,
                            new File(MultimodalObserver.APP_PREFERENCES_FILE));
    
    
    private Thread searchDelay;
    
    
    private void setServerNotice(){
        
        if(currentServer == null){
            searchInput.setEnabled(false);
            serverNotice.setText("<html><p style='color: red;'>You haven't selected a server.</p></html>");
        } else {
            searchInput.setEnabled(true);
            serverNotice.setText("<html><p>You are using server <b>" + currentServer + "</b>.</p></html>");
        }                        
    }
    

    
    private void searchByTag(String tagName){
        
        System.out.println("Buscando por tag: " + tagName);
        String pluginsUrl = Utils.cleanServerUrl(currentServer) + "/tags/"+tagName+"/plugins";
        
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
                
        asyncHttpClient.prepareGet(pluginsUrl).execute(new AsyncCompletionHandler<Response>(){                
            @Override
            public Response onCompleted(Response r) throws Exception{                        
                String json = r.getResponseBody();     
                
                System.out.println(json);
                showPluginSearchResults(json, tagName);
                return r;
            }
            @Override
            public void onThrowable(Throwable t){ 
                JOptionPane.showMessageDialog(null, "There was a connection error. Did you choose a working server?", "Error", JOptionPane.ERROR_MESSAGE);
            }                
        });
        
    }
    
    
    public void installPlugin(Plugin plugin){
        installPlugin(plugin.name, plugin.shortName, plugin.desc, plugin.homePage, plugin.repoName, plugin.repoUser);
    }
    
    
    private void setDownloadPanel(Component backComponent, String zipBallUrl, JScrollPane container){
        
        JPanel downloadPanel = new JPanel();
        
        LogScroll log = new LogScroll();
        
        downloadPanel.add(log);
        
        log.addLine("Beginning download...");
        log.addLine("Downloading from " + zipBallUrl);
        
        
        for(int i=0; i<100; i++){
            log.addLine("Otra linea " + i);
        }
        
        JButton backBtn = new JButton("Cancel");
        
        backBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
               container.setViewportView(backComponent);
            }
        });
        
        downloadPanel.add(backBtn);                

        System.out.println("Descargando...");
        System.out.println(zipBallUrl);        
        
        container.setViewportView(downloadPanel);
    }

    
    public void installPlugin(String name, String shortName, String description, String homepage, String repoName, String repoUser){
        
        
        JDialog d = new JDialog();        
        d.setModal(true);
        d.setSize(500, 500);
        d.setTitle("Install " + name);
        d.setLocationRelativeTo(null);

        
        JPanel p = new JPanel();
        
        
        JPanel top = new JPanel();
        top.setBorder(new EmptyBorder(10, 10, 10, 10));
        top.setLayout(new BorderLayout());
        Title pluginTitle = new Title(name);
        
        top.add(pluginTitle, BorderLayout.NORTH);

        JLabel status = new JLabel("Getting versions...");
        top.add(status, BorderLayout.SOUTH);
        
        TupleList versions = new TupleList();
        JScrollPane scroll = new JScrollPane(versions);

        
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
                    status.setText("This plugin doesn't have releases available for download.");

                } else {
                    
                    for(int i=0; i<map.size(); i++){
                        String tagName = (String)map.get(i).get("tag_name");
                        
                        JButton installBtn = new JButton("Install");
                        String zipBallUrl = (String)map.get(i).get("zipball_url");
                        
                        installBtn.addActionListener(new ActionListener(){
                            @Override
                            public void actionPerformed(ActionEvent e) {                                
                                
                                setDownloadPanel(versions, zipBallUrl, scroll);
                            }
                        });
                        
                        versions.addTuple(tagName, installBtn);
                    }
                }

                status.setText("There are "+map.size()+" available versions.");
                return r;
            }
            @Override
            public void onThrowable(Throwable t){
                JOptionPane.showMessageDialog(null, "Plugin information couldn't be retrieved.", "Error", JOptionPane.ERROR_MESSAGE);
                d.dispose();
                status.setText("<html><span style='color: red;'>There was an error.</span></html>");
            }
        });
        
        p.setLayout(new BorderLayout());
        p.add(top, BorderLayout.NORTH);
        
        
        p.add(scroll, BorderLayout.CENTER);
        
        
        d.add(p);
        d.setVisible(true);
        
    }
    
    
    private synchronized void showTagSearchResults(String json){        
        
        ArrayList<HashMap<String, Object>> tags = Utils.parseArrayJson(json);
         
        if(tags.size() == 0){
            cleanTagResults();
            return;
        }
        
        JPanel tagsPanel = split.getLeft();
        tagsPanel.removeAll();
        
        JPanel tagContainer = new JPanel();
        tagContainer.setLayout(new BoxLayout(tagContainer, BoxLayout.Y_AXIS));
        
        tagsPanel.add(new Title("Tags"), BorderLayout.NORTH);

        for(int i=0; i<tags.size(); i++){
            String tag = (String)tags.get(i).get("short_name");          
            
            JButton tagBtn = new JButton("#"+tag);
            
            tagBtn.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchByTag(tag);
                }
            });
            
            tagContainer.add(tagBtn, Component.CENTER_ALIGNMENT);
        }
        
        
        tagsPanel.add(tagContainer);
        
        tagsPanel.revalidate();
        tagsPanel.repaint();
    }
    
    private synchronized void showPluginSearchResults(String json, String tagName){        
        
        ArrayList<HashMap<String, Object>> plugins = Utils.parseArrayJson(json);        
        
        
        JPanel pluginsPanel = split.getCenter();
        pluginsPanel.removeAll();
        
        if(tagName == null){
            pluginsPanel.add(new Title("Plugins search results"), BorderLayout.NORTH);
        } else {
            pluginsPanel.add(new Title("Plugins #"+tagName), BorderLayout.NORTH);
        }
        
        if(plugins.size() == 0){
            cleanPluginResults();            
            return;
        }

        TupleList tuples = new TupleList();
        
        RemotePluginInstaller self = this;

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
            
            JButton seeBtn = new JButton("See");
            
            tuples.addTuple(name, seeBtn);
            
            Plugin plugin = new Plugin(name, description, homepage, shortName, repoUser, repoName);

            seeBtn.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(plugin);
                    split.setRight(new RemotePluginInfo(plugin, self));
                }
            });
            
            //PluginNode node = new PluginNode(name, description, homepage, shortName, repoUser, repoName);
            //pluginsNode.add(node);
        }
        
        pluginsPanel.add(tuples);        
        pluginsPanel.revalidate();
        pluginsPanel.repaint();
         
    }
    
    
    private void querySearch(){
        
        String q = searchInput.getText().trim();
        
        if(q.length() == 0){
            cleanResults();
            searchingSpinner.completeLoad();
            return;
        }
        
        System.out.println("Searching: " + q);
        
        String tagUrl = Utils.cleanServerUrl(currentServer) + "/tags?q=" + q + "&limit=" + SEARCH_LIMIT;
        String pluginUrl = Utils.cleanServerUrl(currentServer) + "/plugins?q=" + q + "&limit=" + SEARCH_LIMIT;
        
        
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        
        
                
        Future<Response> fTags = asyncHttpClient.prepareGet(tagUrl).execute(new AsyncCompletionHandler<Response>(){                
            @Override
            public Response onCompleted(Response r) throws Exception{                        
                String json = r.getResponseBody();                
                showTagSearchResults(json);                
                searchingSpinner.completeStep();                
                return r;
            }
            @Override
            public void onThrowable(Throwable t){ 
                searchingSpinner.completeStep();
                JOptionPane.showMessageDialog(null, "There was a connection error. Did you choose a working server?", "Error", JOptionPane.ERROR_MESSAGE);
            }                
        });
        
        Future<Response> fPlugins = asyncHttpClient.prepareGet(pluginUrl).execute(new AsyncCompletionHandler<Response>(){                
        @Override
        public Response onCompleted(Response r) throws Exception{                        
            String json = r.getResponseBody();
            showPluginSearchResults(json, null);            
            searchingSpinner.completeStep();
            return r;
        }
        @Override
        public void onThrowable(Throwable t){ 
            searchingSpinner.completeStep();
            JOptionPane.showMessageDialog(null, "There was a connection error. Did you choose a working server?", "Error", JOptionPane.ERROR_MESSAGE);
        }                
        });
        
    }
    
    private void saveServerPreference(String url){        
        File prefFile = new File(MultimodalObserver.APP_PREFERENCES_FILE);
        prefs.setServer(url);
        PreferencesManager.save(prefs, prefFile);
    }
    
    private void checkServer(boolean popup){
        String url = serverInput.getText().trim();   

        if(!Utils.validateHTTP_URI(url)){
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
                    saveServerPreference(url.trim());
                    
                    if(popup)
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
                
                if(popup)
                    JOptionPane.showMessageDialog(null, "A plugin repository couldn't be found.", "Error", JOptionPane.ERROR_MESSAGE);
                setServerNotice();
            }                
        });

    }
    
    
    public JPanel createTopPanel(){
        JPanel result = new JPanel();
        
        JButton searchButton = new JButton("Search");
        
        searchButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                searchingSpinner.startLoading();
                querySearch();
            }        
        });
        
        searchInput.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
                
                searchingSpinner.startLoading();
                
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
            }

            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}        
        
        });
        
        serverCheckButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkServer(true);
            }
        });
        
        serverInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
              if (e.getKeyCode()==KeyEvent.VK_ENTER){
                 checkServer(true);
              }
            }
        });
        
        TupleList inputs = new TupleList();
        
        JPanel inputBtnServer = new JPanel();
        JPanel inputBtnSearch = new JPanel();
        
        inputBtnServer.add(serverInput);
        inputBtnServer.add(serverCheckButton);
        
        inputBtnSearch.add(searchInput);
        inputBtnSearch.add(searchButton);
        inputBtnSearch.add(searchingSpinner);
        
        inputs.addTuple("Use server", inputBtnServer);
        inputs.addTuple("Server status", serverNotice);
        inputs.addTuple("Search", inputBtnSearch);

        
        result.add(inputs);
        
        return result;
    }
    
    

    public void cleanTagResults(){
        JPanel tags = split.getLeft();
        tags.removeAll();
        tags.add(new Title("Tags"), BorderLayout.NORTH);
        tags.add(new JLabel("No results"), BorderLayout.LINE_START);
        tags.revalidate();
        tags.repaint();
        
    }
   
    public void cleanPluginResults(){
        JPanel plugins = split.getCenter();
        plugins.removeAll();
        plugins.add(new Title("Plugins"), BorderLayout.NORTH);
        plugins.add(new JLabel("No results"), BorderLayout.LINE_START);
        plugins.revalidate();
        plugins.repaint();
        
    }
    
    
    public void cleanResults(){   
        cleanTagResults();
        cleanPluginResults();
    }
    
 
    
    public RemotePluginInstaller(){

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        searchingSpinner = new Spinner(2);
        
        JPanel top = createTopPanel();
        split = new SplitPaneTriple();
        
        top.setAlignmentX(this.CENTER_ALIGNMENT);
        split.setAlignmentX(this.CENTER_ALIGNMENT);
        
        this.add(top);
        this.add(split);
        
        
        // Show initial message (related to the server status)
        setServerNotice();
        
        cleanResults();    
        
        try{
                        
            String server = prefs.getServer();
            
            if(server != null && server.length() > 0){
                
                serverInput.setText(server);
                checkServer(false);
                        
            }            
            
        } catch(Exception e){
        }        
        
    }    
    
}
