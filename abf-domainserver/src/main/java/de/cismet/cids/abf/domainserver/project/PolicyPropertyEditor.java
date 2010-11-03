/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

import java.awt.Component;
import java.awt.event.ActionListener;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import de.cismet.cids.abf.utilities.Comparators;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.permission.Policy;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class PolicyPropertyEditor extends PropertyEditorSupport implements InplaceEditor.Factory,
    ExPropertyEditor {

    //~ Instance fields --------------------------------------------------------

    private transient InplaceEditor editor;
    private final transient DomainserverProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PolicyPropertyEditor object.
     *
     * @param  project  DOCUMENT ME!
     */
    public PolicyPropertyEditor(final DomainserverProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public InplaceEditor getInplaceEditor() {
        if (editor == null) {
            editor = new PolicyInplaceEditor(
                    project.getCidsDataObjectBackend());
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
    private static class PolicyInplaceEditor implements InplaceEditor {

        //~ Instance fields ----------------------------------------------------

        private final transient JComboBox box;
        private transient PropertyEditor editor;
        private transient PropertyModel model;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PolicyInplaceEditor object.
         *
         * @param  backend  DOCUMENT ME!
         */
        public PolicyInplaceEditor(final Backend backend) {
            final LinkedList<Policy> policies = new LinkedList<Policy>(
                    backend.getAllEntities(Policy.class));
            Collections.sort(policies, new Comparators.Policies());
            policies.addFirst(Policy.NO_POLICY);
            box = new JComboBox(policies.toArray());
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void connect(final PropertyEditor propertyEditor, final PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return box;
        }

        @Override
        public void clear() {
            // do nothing
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
