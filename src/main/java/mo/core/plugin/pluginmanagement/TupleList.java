/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mo.core.plugin.pluginmanagement;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 *
 * @author felo
 */
public class TupleList extends JPanel {
    
    private int row;
        
    private GridBagConstraints grid;
    
    private static final String EMPTY = "---";
    
    private static final int MAX_ROWS_TABLE = 5;
    
    TupleList(){        
        row = 0;
        
        setLayout(new GridBagLayout());
	grid = new GridBagConstraints();
        
        grid.gridy = 0;
        grid.insets = new Insets(10, 10, 10, 10);
        grid.weightx = 0.5;
        grid.fill = GridBagConstraints.HORIZONTAL;
    }
    
    
    private void addTuple(String key, JComponent value){
        
        grid.gridx = 0;
	add(new JLabel(key, SwingConstants.RIGHT), grid);

	grid.gridx = 1;	
	add(value, grid);
        
        grid.gridy++;
        
    }
    

    
    public void addTuple(String key, Object value){
        
        JComponent comp = null;        
        
        if(value instanceof JTable){
            // make the table smaller
            JTable table = (JTable) value;
            comp = new JScrollPane(table);        
            Dimension depDimension = table.getPreferredSize();
            
            // set max height
            comp.setPreferredSize(new Dimension(depDimension.width, table.getRowHeight() * (Math.min(table.getRowCount(), MAX_ROWS_TABLE) + 2)));            
            
        } else if(value instanceof JComponent){
            comp = (JComponent) value;
        } else if(value == null){
            comp = new JLabel(EMPTY);
        } else {
            
            String text = value.toString().length() == 0? EMPTY : value.toString();
            
            comp = new JLabel(text);
        }        
        
        addTuple(key, comp);
    }
    
    public void addScrollText(String key, String value){        

        JTextArea textArea = new JTextArea(5, 20);
        textArea.setOpaque(false);

        textArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(textArea); 
        textArea.setText(value);
        textArea.setEditable(false);        
        
        addTuple(key, scrollPane); 
        
    }    
    
}
