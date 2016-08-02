package mo.core.ui.frames;

import mo.core.plugin.ExtensionPoint;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
@ExtensionPoint
public interface IDockableElement {
    public DockableElement getElement();
}
