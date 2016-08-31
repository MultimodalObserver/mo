package mo.organization;

import javax.swing.JPopupMenu;

public interface StageNode {
    JPopupMenu getPopupMenu();
    String getStageName();
    boolean mustBeUnique();
}
