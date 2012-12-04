/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.WeakListeners;

import java.awt.Component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;

import java.util.HashSet;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class PasswordPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor,
    PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------

    private final transient DefaultPasswordComponentModel pwModel;

    // WTH are there two property env instances, and only one of then really does what it is supposed to
    private transient Set<PropertyEnv> env;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PasswordPropertyEditor object.
     */
    public PasswordPropertyEditor() {
        this.pwModel = new DefaultPasswordComponentModel();
        this.pwModel.addPropertyChangeListener(WeakListeners.propertyChange(this, this.pwModel));
        env = new HashSet<PropertyEnv>();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getCustomEditor() {
        return new PasswordPanel(pwModel);
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public void attachEnv(final PropertyEnv pe) {
        this.env.add(pe);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (DefaultPasswordComponentModel.PROP_PW.equals(evt.getPropertyName())
                    || DefaultPasswordComponentModel.PROP_PW_MATCH.equals(evt.getPropertyName())) {
            final Object state;
            if (pwModel.isValid()) {
                state = PropertyEnv.STATE_VALID;
            } else {
                state = PropertyEnv.STATE_INVALID;
            }

            for (final PropertyEnv e : env) {
                e.setState(state);
            }
        }
    }

    @Override
    public Object getValue() {
        if (pwModel.isValid()) {
            return String.valueOf(pwModel.getPassword());
        } else {
            return null;
        }
    }
}
