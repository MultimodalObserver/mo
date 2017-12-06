package mo.export.visualization;

import mo.organization.ProjectOrganization;
import mo.organization.Participant;
import mo.organization.StageModule;
import mo.core.ui.WizardDialog;
import mo.core.ui.GridBConstraints;
import mo.export.plugin.ExportDataPlugin;
import mo.export.visualization.ExportProjectWizardPanel;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.JTree;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class ExportDataDialog {

	WizardDialog dialog;
	JPanel creatorsPanel;
	JPanel pluginsPanel;
	JPanel selectPluginsPanel;
	JPanel selectionDataPanel;
	ExportProjectWizardPanel setLocationPanel;
	JPanel panel;

	private ProjectOrganization organization;
	private ExportTree eTree;
	private DefaultMutableTreeNode root;
	private TreeModel model;
	private List<File> originDataLocations;
	private List<JCheckBox> checkboxes;
	private List<JComboBox> comboboxes;
	private List<String> creators;
	private List<ExportDataPlugin> plugins;
	private HashMap destinationConfiguration;

	public ExportDataDialog(ProjectOrganization organization, List<ExportDataPlugin> plugins) {
		this.organization = organization;
		this.plugins = plugins;

		creators = new ArrayList();

		checkboxes = getCheckboxs(plugins);

		creatorsPanel = new JPanel();
		creatorsPanel.setName("Seleccione los creators");
        creatorsPanel.setLayout(new BoxLayout(creatorsPanel, BoxLayout.PAGE_AXIS));
        creatorsPanel.setPreferredSize(new Dimension(500,500));
		addCheckboxes(creatorsPanel,checkboxes);

		pluginsPanel = new JPanel();
		pluginsPanel.setName("Seleccione los plugins");
		pluginsPanel.setLayout(new BoxLayout(pluginsPanel, BoxLayout.PAGE_AXIS));
        pluginsPanel.setPreferredSize(new Dimension(500,500));

    	originDataLocations = new ArrayList<>();
        eTree = new ExportTree(organization);
		eTree.addCheckChangeEventListener(new JCheckBoxTree.CheckChangeEventListener() {
            public void checkStateChanged(JCheckBoxTree.CheckChangeEvent event) {
            	originDataLocations.clear();
                TreePath[] paths = eTree.getCheckedPaths();
            	String relativePathToLeaf = "";
                for (TreePath tp : paths) {
                    for (Object pathPart : tp.getPath()) {
                    	if (((DefaultMutableTreeNode) pathPart).isLeaf()) {
                    		Object padre = ((DefaultMutableTreeNode) pathPart).getParent();
                    		relativePathToLeaf = padre.toString() + "/" + pathPart.toString() + "/";
                    		File file = new File(organization.getLocation(),relativePathToLeaf);
                    		if (Files.exists(file.toPath())) {
                    			originDataLocations.add(file);
                    		}
                    	}
                    }                   
                }
                updateState();
            }           
        });

		selectionDataPanel = new JPanel();
		selectionDataPanel.setName("Seleccionar los orígenes de datos");
        selectionDataPanel.setLayout(new BorderLayout());
        selectionDataPanel.setPreferredSize(new Dimension(500,500));
        selectionDataPanel.add(eTree);

		dialog = new WizardDialog(null, "Export Data");
		dialog.setWarningMessage("Selecciones los creators");
        dialog.addActionListener(new WizardDialog.WizardListener() {
            @Override
            public void onStepChanged() {
                stepChanged();
            }
        });

        setLocationPanel = new ExportProjectWizardPanel(dialog);

		dialog.addPanel(creatorsPanel);
		dialog.addPanel(pluginsPanel);
		dialog.addPanel(selectionDataPanel);
        dialog.addPanel(setLocationPanel);
	}

	private void updateState() {
		disableAllButtons();

        if (dialog.getCurrentStep() == 0) {
        	if (atLeastOneCheckboxSelected()) {
            	dialog.enableNext();
        		dialog.setWarningMessage("");
        	} else {
            	dialog.setWarningMessage("Seleccione los creators");
        	}
        } else if (dialog.getCurrentStep() == 1) {
        	dialog.enableBack();

        	if(allCombosSelected()) {
        		dialog.enableNext();
        		dialog.setWarningMessage("");
        	} else {
        		dialog.setWarningMessage("Seleccione los plugins de exportacion.");
        		dialog.disableNext();
        	}
        } else if(dialog.getCurrentStep() == 2) {
        	dialog.enableBack();

            if (atLeastOneDataLocationSelected()) {

            	dialog.enableNext();
            	dialog.setWarningMessage("");
            } else {
            	dialog.disableNext();
            	dialog.setWarningMessage("Seleccione las fuentes de datos");
            }
        } else if(dialog.getCurrentStep() == 3 ) {
        	dialog.enableBack();
        	setLocationPanel.updateState();
        }
    }

    private void stepChanged() {
    	updateState();
    }

    private void disableAllButtons() {
    	dialog.disableBack();
    	dialog.disableNext();
    	dialog.disableFinish();
    }

    private boolean atLeastOneDataLocationSelected() {
    	return (originDataLocations.size() > 0);
    }

	private void addCheckboxes(JPanel panel, List<JCheckBox> checkboxes) {
		for (JCheckBox c : checkboxes) {
			c.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					 JCheckBox cb = (JCheckBox) e.getItem();
					if(cb.isSelected()) {
						addCreator(cb);
					} else {
						removeCreator(cb);
					}
					updateComboboxes();
					updateState();
				}
			});
			c.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(c);
		}
	}

    private void updateComboboxes() {
		JComboBox combo;
		comboboxes = new ArrayList();
		JLabel label;
		List<ExportDataPlugin> plugins;
		pluginsPanel.removeAll();
		for (String creator : creators) {
			label = new JLabel(creator);
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			pluginsPanel.add(label);

			combo = new JComboBox() {
				@Override
	            public Dimension getMaximumSize() {
	                Dimension max = super.getMaximumSize();
	                max.height = getPreferredSize().height;
	                return max;
	            }
			};

			combo.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateState();
				}
			});
			combo.setAlignmentX(Component.LEFT_ALIGNMENT);
			combo.addItem("Select item");
			for (ExportDataPlugin plugin : getPluginsFor(creator)) {
				combo.addItem(plugin);
				combo.setRenderer(new ExportDataPluginListCellRenderer());
			}
			pluginsPanel.add(combo);
			comboboxes.add(combo);
		}
    }

    private List<ExportDataPlugin> getPluginsFor(String creator) {
    	List<ExportDataPlugin> result = new ArrayList();
    	for (ExportDataPlugin p : plugins) {
    		for (String c : p.getCompatibleCreators()) {
    			if (c.equals(creator)) {
    				result.add(p);
    			}
    		}
    	}

    	return result;
    }

	private List<JCheckBox> getCheckboxs(List<ExportDataPlugin> plugins) {
		List<JCheckBox> result = new ArrayList();
        JCheckBox checkbox;
        for (ExportDataPlugin p : plugins) {
        	for (String c : p.getCompatibleCreators()) {
        		checkbox = new JCheckBox(c);
        		result.add(checkbox);
        	}
        }
        return result;
	}

	private void addCreator(JCheckBox checkbox) {
		creators.add(checkbox.getText());
	}

	private void removeCreator(JCheckBox checkbox) {
		creators.remove(checkbox.getText());
	}

    private boolean atLeastOneCheckboxSelected() {
    	return (creators.size() > 0);
    }

    private boolean allCombosSelected() {
    	boolean allSelected = true;
    	if(comboboxes.size() == 0)	return false;

    	for (JComboBox combo : comboboxes) {
    		if (combo.getSelectedItem() instanceof String) {
    			allSelected = false;
    		}
    	}

    	return allSelected;
    }

	public boolean show() {
		destinationConfiguration = dialog.showWizard();

		if (destinationConfiguration != null && originDataLocations.size() > 0) {
        	return true;
        }

        return false;
	}

    public List<String> getCreators() {
    	return creators;
    }

    public List<ExportDataPlugin> getSelectedPlugins() {
    	List<ExportDataPlugin> selectedPlugins = new ArrayList();

    	ExportDataPlugin plugin;
    	for (JComboBox combo : comboboxes) {
    		plugin = (ExportDataPlugin) combo.getSelectedItem();
    		selectedPlugins.add(plugin);
    	}

    	return selectedPlugins;
    }

    public File getExportRootFile() {
		File rootFolder = new File((String) destinationConfiguration.get("projectFolder"));

		return rootFolder;
	}

	public List<File> getOriginDataLocations() {
		return originDataLocations;
	}

	public class ExportDataPluginListCellRenderer extends DefaultListCellRenderer {

	    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	        if (value instanceof ExportDataPlugin) {
	            value = ((ExportDataPlugin)value).getName();
	        }
	        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        return this;
	    }
	}
}