package mo.core.filemanagement.project;

import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.export.plugin.ExportProjectPlugin;
import mo.export.plugin.ExportDataPlugin;
import mo.export.plugin.ExportPlugin;
import mo.export.ExportProjectManager;
import mo.export.ExportDataManager;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

@Extension(
    xtends = {
        @Extends(
                extensionPointId = "mo.core.filemanagement.project.ProjectOptionProvider"
        )
    }
)
public class ExportMenu implements ProjectOptionProvider {

    JMenu organizationMenu;

    public ExportMenu() {
        organizationMenu = new JMenu("Exportar");
        
        JMenuItem item;
        ExportProjectPlugin internal;
        ExportProjectManager manager = new ExportProjectManager();
        List<ExportProjectPlugin> exportProjectPlugins = manager.getPlugins();
        for (ExportProjectPlugin plugin : exportProjectPlugins) {
            item = new JMenuItem(plugin.getName());
            item.setName(plugin.getCodeName());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object source = e.getSource();
                    if (source instanceof JMenuItem) {
                        JMenuItem itemSource = (JMenuItem) source;
                        File file = (File) itemSource.getClientProperty("file");
                        manager.newExportConfiguration(plugin,file);
                    }
                }
            });
            organizationMenu.add(item);
        }

        item = new JMenuItem("Exportar datos");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source instanceof JMenuItem) {
                    JMenuItem itemSource = (JMenuItem) source;
                    File file = (File) itemSource.getClientProperty("file");
                    ExportDataManager manager = new ExportDataManager(file);
                }
            }
        });
        organizationMenu.add(item);

    }

    @Override
    public JMenu getOption() {
        return organizationMenu;
    }
}