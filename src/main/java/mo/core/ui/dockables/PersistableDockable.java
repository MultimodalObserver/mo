package mo.core.ui.dockables;

public interface PersistableDockable {
    Object getPersistableData();
    DockableElement createDockableFrom(Object data);
}
