/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.icons;

import org.apache.log4j.Logger;

import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.domainserver.project.utils.Renderers;

import de.cismet.cids.jpa.entity.cidsclass.Icon;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class IconPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory {

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;
    private final transient JLabel renderer;
    private transient InplaceEditor ed;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IconPropertyEditor object.
     *
     * @param  project  DOCUMENT ME!
     */
    public IconPropertyEditor(final DomainserverProject project) {
        this.project = project;
        renderer = new JLabel();
        renderer.setText(org.openide.util.NbBundle.getMessage(
                IconPropertyEditor.class,
                "IconPropertyEditor.renderer.text.noIconChosen")); // NOI18N
        renderer.setIcon(null);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void attachEnv(final PropertyEnv propertyEnv) {
        propertyEnv.registerInplaceEditorFactory(this);
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (ed == null) {
            ed = new Inplace(project);
        }
        return ed;
    }

    @Override
    public void setValue(final Object value) {
        if (value instanceof Icon) {
            renderer.setText(((Icon)value).getName());
            final Image image = ProjectUtils.getImageForIconAndProject(((Icon)value), project);
            renderer.setIcon((image == null) ? null : new ImageIcon(image));
        } else {
            renderer.setText(org.openide.util.NbBundle.getMessage(
                    IconPropertyEditor.class,
                    "IconPropertyEditor.renderer.text.noIconChosen")); // NOI18N
            renderer.setIcon(null);
        }
        super.setValue(value);
    }

    /**
     * Paint a representation of the value into a given area of screen real estate. Note that the propertyEditor is
     * responsible for doing its own clipping so that it fits into the given rectangle.
     *
     * <p>If the PropertyEditor doesn't honor paint requests (see isPaintable) this method should be a silent noop.</p>
     *
     * @param  gfx  Graphics object to paint into.
     * @param  box  Rectangle within graphics object into which we should paint.
     */
    @Override
    public void paintValue(final Graphics gfx, final Rectangle box) {
        renderer.setSize(box.width, box.height);
        renderer.paint(gfx);
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class Inplace implements InplaceEditor, ActionListener {

        //~ Static fields/initializers -----------------------------------------

        private static final transient Logger LOG = Logger.getLogger(
                Inplace.class);

        //~ Instance fields ----------------------------------------------------

        private final transient JComboBox cboPicker = new JComboBox();
        private final transient Set<ActionListener> listeners;
        private final transient DomainserverProject project;
        private final transient List<Icon> allIcons;
        private transient PropertyEditor editor;
        private transient PropertyModel model;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Inplace object.
         *
         * @param  project  DOCUMENT ME!
         */
        public Inplace(final DomainserverProject project) {
            this.project = project;
            listeners = new HashSet<ActionListener>();
            allIcons = new ArrayList<Icon>();
            cboPicker.addActionListener(this);
            cboPicker.setRenderer(new Renderers.IconCellRenderer(project));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void connect(final PropertyEditor propertyEditor, final PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return cboPicker;
        }

        @Override
        public void clear() {
            // avoid memory leaks:
            editor = null;
            model = null;
        }

        @Override
        public Object getValue() {
            return cboPicker.getSelectedItem();
        }

        @Override
        public void setValue(final Object object) {
            cboPicker.setSelectedItem(object);
        }

        @Override
        public boolean supportsTextEntry() {
            return true;
        }

        @Override
        public void reset() {
            try {
                final Icon value = ((Icon)editor.getValue());
                allIcons.clear();
                allIcons.addAll(project.getCidsDataObjectBackend().getAllEntities(Icon.class));
                // at this time cs_class has not null constraint on icon fields
                // allIcons.insertElementAt(null, 0);
                cboPicker.setModel(new DefaultComboBoxModel(
                        allIcons.toArray()));
                cboPicker.setSelectedItem(value);
            } catch (final Exception ex) {
                LOG.error("error while resetting editor", ex); // NOI18N
                ErrorManager.getDefault().notify(ex);
            }
        }

        @Override
        public KeyStroke[] getKeyStrokes() {
            return new KeyStroke[0];
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        @Override
        public PropertyModel getPropertyModel() {
            return model;
        }

        @Override
        public void setPropertyModel(final PropertyModel propertyModel) {
            this.model = propertyModel;
        }

        @Override
        public boolean isKnownComponent(final Component component) {
            return component.equals(cboPicker)
                        || cboPicker.isAncestorOf(component);
        }

        @Override
        public void addActionListener(final ActionListener actionListener) {
            synchronized (listeners) {
                listeners.add(actionListener);
            }
        }

        @Override
        public void removeActionListener(final ActionListener actionListener) {
            synchronized (listeners) {
                listeners.remove(actionListener);
            }
        }

        /**
         * Invoked when an action occurs.
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            final Iterator<ActionListener> it;
            synchronized (listeners) {
                it = new HashSet<ActionListener>(listeners).iterator();
            }
            final ActionEvent ae = new ActionEvent(this, 0, InplaceEditor.COMMAND_SUCCESS);
            while (it.hasNext()) {
                it.next().actionPerformed(ae);
            }
        }
    }
}
