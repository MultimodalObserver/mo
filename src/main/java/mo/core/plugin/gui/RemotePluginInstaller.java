package mo.core.plugin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;



class PluginNode extends DefaultMutableTreeNode{
    
    String name, desc, homePage, shortName, repoUser, repoName;
    
    PluginNode(String name, String desc, String homePage, String shortName, String repoUser, String repoName){
        super(name);
        this.name = name;
        this.desc = desc;
        this.homePage = homePage;
        this.shortName = shortName;
        this.repoUser = repoUser;
        this.repoName = repoName;
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
    
    PluginNode plugin;
    RemotePluginInstaller container;
    
    RemotePluginInfo(PluginNode plugin, RemotePluginInstaller container){
        this.plugin = plugin;
        this.container = container;
        
        
        TupleList tuples = new TupleList();
        
        tuples.addTuple("Name", plugin.name);
        tuples.addTuple("Name", plugin.name);
        tuples.addTuple("Name", plugin.name);
        tuples.addTuple("Name", plugin.name);
        tuples.addTuple("Name", plugin.name);
        
        JButton installBtn = new JButton("Install");
        
        installBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                container.installPlugin(plugin);
            }
        });
        
        tuples.addTuple("", installBtn);
        
        JLabel pluginTitle = new JLabel(plugin.name, SwingConstants.LEFT);
        pluginTitle.setFont(new Font("", Font.BOLD, 20));
        Dimension d = pluginTitle.getPreferredSize();
        d.height = 25;
        pluginTitle.setPreferredSize(d);         
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
    
    private JLabel loadingTags = new JLabel("Loading tags...");
    private JLabel loadingPlugins = new JLabel("Loading plugins...");
    
    private JPanel tagSearchResultContainer = new JPanel();
    private JPanel pluginSearchResultContainer = new JPanel();
    
    private JTree searchResults;
    DefaultMutableTreeNode tagsNode = new DefaultMutableTreeNode("Tags");
    DefaultMutableTreeNode pluginsNode = new DefaultMutableTreeNode("Plugins");
    
    
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
    
    
    public void installPlugin(PluginNode plugin){
        installPlugin(plugin.name, plugin.shortName, plugin.desc, plugin.homePage, plugin.repoName, plugin.repoUser);
    }

    
    public void installPlugin(String name, String shortName, String description, String homepage, String repoName, String repoUser){
        
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
    
    private void updateTree(){
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)searchResults.getModel().getRoot();
        root.removeAllChildren();
        root.add(tagsNode);
        root.add(pluginsNode);

        TreeModel tm = new DefaultTreeModel(root);
        searchResults.setModel(tm);  

        expandTree(searchResults);
    }
    
    private synchronized void showTagSearchResults(String json){        
        
        ArrayList<HashMap<String, Object>> tags = parseArrayJson(json);
        
        tagsNode.removeAllChildren();

        for(int i=0; i<tags.size(); i++){
            String tag = (String)tags.get(i).get("short_name");
            
            TagNode node = new TagNode(tag);
            tagsNode.add(node);
        }
        
        if(tags.size() == 0){
            DefaultMutableTreeNode noResults = new DefaultMutableTreeNode("No results");
            tagsNode.add(noResults);
        }
        
        updateTree();
    }
    
    private synchronized void showPluginSearchResults(String json){        
        
        ArrayList<HashMap<String, Object>> plugins = parseArrayJson(json);
        
        pluginsNode.removeAllChildren();

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
            
            PluginNode node = new PluginNode(name, description, homepage, shortName, repoUser, repoName);
            pluginsNode.add(node);
        }
               
        
        if(plugins.size() == 0){
            DefaultMutableTreeNode noResults = new DefaultMutableTreeNode("No results");
            pluginsNode.add(noResults);
        }
        
        updateTree();        
         
    }
    
    
    private void querySearch(){
        
        String q = searchInput.getText().trim();
        
        if(q.length() == 0){
            cleanResultsTree();
            return;
        }
        
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
    
    
    public JPanel createTopPanel(){
        JPanel result = new JPanel();
        
        
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
        
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        
        serverInput.setAlignmentX(result.CENTER_ALIGNMENT);
        serverCheckButton.setAlignmentX(result.CENTER_ALIGNMENT);
        searchInput.setAlignmentX(result.CENTER_ALIGNMENT);
        serverNotice.setAlignmentX(result.CENTER_ALIGNMENT);

        //result.add(serverNotice);
        result.add(serverInput);
        result.add(serverNotice);
        result.add(serverCheckButton);
        result.add(searchInput);       
        
        return result;
    }
    
    
    JSplitPane createSplitPane(){
        JSplitPane split = new JSplitPane();
        
        
        // Tree
        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Search results");

        
        rootNode.add(tagsNode);
        rootNode.add(pluginsNode);
        
        
        searchResults = new JTree(rootNode);
        JScrollPane leftScroll = new JScrollPane(searchResults);
        JPanel rightPluginShow = new JPanel();
        
        split.setLeftComponent(leftScroll);
        split.setRightComponent(rightPluginShow);
        
        RemotePluginInstaller self = this;
        
        searchResults.addTreeSelectionListener(new TreeSelectionListener(){
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                searchResults.getLastSelectedPathComponent();
                
                if(node instanceof PluginNode){
                
                    PluginNode plugin = (PluginNode)node;
                    System.out.println(plugin.name);
                    split.setRightComponent(new RemotePluginInfo(plugin, self));
                }
                
                if(node instanceof TagNode){
                
                    TagNode plugin = (TagNode)node;
                    
                    System.out.println("#"+plugin.shortName);
                }
                
            }
        });
                
        return split;
    }
    
    private void expandTree(JTree tree){        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }        
    }
    
    public void cleanResultsTree(){        
        DefaultMutableTreeNode empty1 = new DefaultMutableTreeNode("Nothing to show");
        DefaultMutableTreeNode empty2 = new DefaultMutableTreeNode("Nothing to show");
        
        tagsNode.removeAllChildren();
        pluginsNode.removeAllChildren();      
        
        tagsNode.add(empty1);
        pluginsNode.add(empty2);
 
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)searchResults.getModel().getRoot();
        root.removeAllChildren();
        root.add(tagsNode);
        root.add(pluginsNode);
        
        TreeModel tm = new DefaultTreeModel(root);
        searchResults.setModel(tm);  
        
        expandTree(searchResults);
        
    }
    
 
    
    public RemotePluginInstaller(){

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel top = createTopPanel();
        JSplitPane split = createSplitPane();
        
        top.setAlignmentX(this.CENTER_ALIGNMENT);
        split.setAlignmentX(this.CENTER_ALIGNMENT);
        
        this.add(top);
        this.add(split);
        
        // Show initial message (related to the server status)
        setServerNotice();
        
        cleanResultsTree();
        
    }
    
    
}
