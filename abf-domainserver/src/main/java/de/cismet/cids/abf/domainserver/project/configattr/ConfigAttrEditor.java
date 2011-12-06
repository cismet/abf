/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.configattr;

import org.apache.log4j.Logger;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import java.awt.EventQueue;
import java.awt.Image;

import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.abf.utilities.nodes.ModifyCookie;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class ConfigAttrEditor extends TopComponent implements ExplorerManager.Provider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ConfigAttrEditor.class);

    //~ Instance fields --------------------------------------------------------

    protected final transient DocumentListener saveListener;
    protected final transient UndoRedo.Manager undoRedo;

    protected transient Node dataObject;

    private final transient ExplorerManager manager;
    private final transient PropertyChangeListener propL;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigAttrEditor object.
     *
     * @param   dataObject  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     * @throws  IllegalStateException     DOCUMENT ME!
     */
    public ConfigAttrEditor(final Node dataObject) {
        final ConfigAttrEntryCookie entryCookie = dataObject.getCookie(ConfigAttrEntryCookie.class);
        if (entryCookie == null) {
            throw new IllegalArgumentException("dataobject must contain ConfigAttrEntryCookie");         // NOI18N
        } else if (entryCookie.getEntry() == null) {
            throw new IllegalStateException("ConfigAttrEntryCookie does not deliver valid entry: null"); // NOI18N
        }

        if (entryCookie.getEntry().getKey() != null) {
            setName(entryCookie.getEntry().getKey().getKey() + ": " + dataObject.getName()); // NOI18N
        } else {
            setName(dataObject.getName());
        }

        setActivatedNodes(new Node[] { dataObject });
        this.dataObject = dataObject;
        propL = new PropertyChangeListenerImpl();
        saveListener = new DocumentListenerImpl();
        manager = new ExplorerManager();
        undoRedo = new UndoRedo.Manager();
        associateLookup(dataObject.getLookup());

        dataObject.addPropertyChangeListener(WeakListeners.propertyChange(propL, dataObject));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon() {
        return dataObject.getIcon(BeanInfo.ICON_COLOR_16x16);
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager.clone();
    }

    @Override
    public boolean canClose() {
        boolean canClose = true;

        if (isTransient()) {
            final Confirmation dialog = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(
                        ConfigAttrEditor.class,
                        "ConfigAttrEditor.canClose().saveDialog.message", // NOI18N
                        getName()),
                    NbBundle.getMessage(ConfigAttrEditor.class, "ConfigAttrEditor.canClose().saveDialog.title"), // NOI18N
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            final Object result = DialogDisplayer.getDefault().notify(dialog);
            if (NotifyDescriptor.CANCEL_OPTION.equals(result)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("user cancelled save process"); // NOI18N
                }
                canClose = false;
            } else {
                if (NotifyDescriptor.YES_OPTION.equals(result)) {
                    try {
                        getLookup().lookup(SaveCookie.class).save();
                    } catch (final IOException ex) {
                        LOG.error("could not save editor value", ex); // NOI18N
                    }
                }
            }
        }

        return canClose;
    }

    /**
     * DOCUMENT ME!
     */
    abstract void notifyDataObjectChanged();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    abstract ConfigAttrEntry getEditorValue();

    /**
     * DOCUMENT ME!
     *
     * @param  modified  DOCUMENT ME!
     */
    protected void setModified(final boolean modified) {
        final boolean before = isTransient();

        dataObject.getCookie(ModifyCookie.class).setModified(modified);

        if (before != modified) {
            updateDisplayName();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void updateDisplayName() {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (isTransient()) {
                        setDisplayName(getName() + " *");                            // NOI18N
                        setHtmlDisplayName("<html><b>" + getName() + "</b></html>"); // NOI18N
                    } else {
                        setDisplayName(getName());
                        setHtmlDisplayName(getName());
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isTransient() {
        return dataObject.getCookie(SaveCookie.class) != null;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return undoRedo;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class PropertyChangeListenerImpl implements PropertyChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            notifyDataObjectChanged();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DocumentListenerImpl implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertUpdate(final DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            setModified(true);
        }
    }
}
