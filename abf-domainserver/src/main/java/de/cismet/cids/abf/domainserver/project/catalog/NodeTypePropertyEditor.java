/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.catalog;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

import java.awt.Component;
import java.awt.event.ActionListener;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import de.cismet.cids.abf.domainserver.project.utils.Renderers;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class NodeTypePropertyEditor extends PropertyEditorSupport implements InplaceEditor.Factory,
    ExPropertyEditor {

    //~ Instance fields --------------------------------------------------------

    private transient InplaceEditor editor;

    //~ Methods ----------------------------------------------------------------

    @Override
    public InplaceEditor getInplaceEditor() {
        if (editor == null) {
            editor = new NodeTypeInplaceEditor();
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
    private static class NodeTypeInplaceEditor implements InplaceEditor {

        //~ Instance fields ----------------------------------------------------

        private final transient JComboBox box;
        private transient PropertyEditor editor;
        private transient PropertyModel model;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NodeTypeInplaceEditor object.
         */
        public NodeTypeInplaceEditor() {
            box = new JComboBox(
                    new Object[] {
                        CatNode.Type.CLASS.getType(),
                        CatNode.Type.OBJECT.getType(),
                        CatNode.Type.ORG.getType()
                    });
            box.setRenderer(new Renderers.NodeTypeRenderer());
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void connect(final PropertyEditor propertyEditor,
                final PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return box;
        }

        @Override
        public void clear() {
            // not needed
        }

        @Override
        public Object getValue() {
            return box.getSelectedItem();
        }

        @Override
        public void setValue(final Object object) {
            box.setSelectedItem(object);
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
                    box.setSelectedItem(o);
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
            return box.equals(component) || box.isAncestorOf(component);
        }
    }
}
