/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.catalog;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

import java.awt.Component;

import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class DynamicChildrenPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {

    //~ Instance fields --------------------------------------------------------

    private transient boolean editable;
    private transient PropertyEnv env;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setAsText(final String s) {
        if ("null".equals(s) && (getValue() == null)) // NOI18N
        {
            return;
        }
        setValue(s);
    }

    @Override
    public void attachEnv(final PropertyEnv env) {
        this.env = env;
        final FeatureDescriptor desc = env.getFeatureDescriptor();
        if (desc instanceof Node.Property) {
            final Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        final Object value = getValue();
        final String dc = (value == null) ? "" : value.toString();
        return new DynamicChildrenSimpleEditor(dc, editable);
    }
}
