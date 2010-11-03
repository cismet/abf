/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.javaclass;

import org.apache.log4j.Logger;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

import java.awt.Component;
import java.awt.event.ActionListener;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import java.lang.reflect.Field;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import de.cismet.cids.abf.domainserver.project.utils.Renderers;

import de.cismet.cids.jpa.entity.cidsclass.JavaClass;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class JavaClassTypePropertyEditor extends PropertyEditorSupport implements InplaceEditor.Factory,
    ExPropertyEditor {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            JavaClassTypePropertyEditor.class);

    //~ Instance fields --------------------------------------------------------

    private transient JavaClassTypeInplaceEditor editor;

    //~ Methods ----------------------------------------------------------------

    @Override
    public InplaceEditor getInplaceEditor() {
        if (editor == null) {
            editor = new JavaClassTypeInplaceEditor();
        }
        return editor;
    }

    @Override
    public void attachEnv(final PropertyEnv propertyEnv) {
        propertyEnv.registerInplaceEditorFactory(this);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class JavaClassTypeInplaceEditor implements InplaceEditor {

        //~ Instance fields ----------------------------------------------------

        private final transient JComboBox cboBox;
        private transient PropertyEditor editor;
        private transient PropertyModel model;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new JavaClassTypeInplaceEditor object.
         */
        JavaClassTypeInplaceEditor() {
            final Field[] fields = JavaClass.Type.class.getFields();
            final String[] types = new String[fields.length];
            try {
                for (int i = 0; i < fields.length; ++i) {
                    final Object o = fields[i].get(new Object());
                    types[i] = ((JavaClass.Type)o).getType();
                }
            } catch (final IllegalArgumentException ex) {
                LOG.error("could not add type to typearray", ex); // NOI18N
            } catch (final IllegalAccessException ex) {
                LOG.error("could not add type to typearray", ex); // NOI18N
            }
            cboBox = new JComboBox(types);
            cboBox.setRenderer(new Renderers.JavaClassTypeRenderer());
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void connect(final PropertyEditor propertyEditor, final PropertyEnv propertyEnv) {
            editor = propertyEditor;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return cboBox;
        }

        @Override
        public void clear() {
            // nothing to clear
        }

        @Override
        public Object getValue() {
            return cboBox.getSelectedItem();
        }

        @Override
        public void setValue(final Object object) {
            if (object != null) {
                cboBox.setSelectedItem(object);
            }
        }

        @Override
        public boolean supportsTextEntry() {
            return false;
        }

        @Override
        public void reset() {
            if (editor != null) {
                final Object o = editor.getValue();
                if (o != null) {
                    cboBox.setSelectedItem(o);
                }
            }
        }

        @Override
        public void addActionListener(final ActionListener actionListener) {
            // not needed
        }

        @Override
        public void removeActionListener(final ActionListener actionListener) {
            // not needed
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
            return component.equals(cboBox) || cboBox.isAncestorOf(component);
        }
    }
}
