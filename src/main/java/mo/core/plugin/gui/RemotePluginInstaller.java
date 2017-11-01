package mo.core.plugin.gui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;

import org.asynchttpclient.*;
import java.util.concurrent.Future;
import javax.swing.BoxLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 *
 * @author felo
 */
public class RemotePluginInstaller extends JPanel {
    
    private final String CHECK_SERVER_BUTTON_LABEL = "Use server";
    
    private String currentServer = null;    
    
    private JTextField serverInput = new JTextField(50);
    private JButton serverCheckButton = new JButton(CHECK_SERVER_BUTTON_LABEL);
    private JLabel serverNotice = new JLabel();
    
    
    
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

    
    public RemotePluginInstaller(){   
        
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

        
        setServerNotice();

    }
    
    
}
