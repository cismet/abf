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
    private final transient PasswordPanel pwPanel;

    // WTH are there two property env instances, and only one of then really does what it is supposed to
    private transient Set<PropertyEnv> env;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PasswordPropertyEditor object.
     */
    public PasswordPropertyEditor() {
        pwModel = new DefaultPasswordComponentModel();
        pwPanel = new PasswordPanel(pwModel);

        pwModel.addPropertyChangeListener(WeakListeners.propertyChange(this, pwModel));
        env = new HashSet<PropertyEnv>();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getCustomEditor() {
        return pwPanel;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public void attachEnv(final PropertyEnv pe) {
        this.env.add(pe);
        pe.addPropertyChangeListener(WeakListeners.propertyChange(this, pe));
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (DefaultPasswordComponentModel.PROP_PW.equals(evt.getPropertyName())
                    || DefaultPasswordComponentModel.PROP_PW_MATCH.equals(evt.getPropertyName())) {
            final Object state;
            if (pwModel.isValid()) {
                // never set to valid state, valid state will be set if ok is pressed and no veto was raised
                state = PropertyEnv.STATE_NEEDS_VALIDATION;
            } else {
                state = PropertyEnv.STATE_INVALID;
            }

            for (final PropertyEnv e : env) {
                e.setState(state);
            }
        } else if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
                    && (evt.getNewValue() == PropertyEnv.STATE_VALID)) {
            // We clear the environment at this point as the OK button is pressed and no veto was raised. Thus the
            // editor is not visualised anymore and the new value is taken over (after set has been called).
            env.clear();
            setValue(String.valueOf(pwModel.getPassword()));
        }
    }

    @Override
    public Object getValue() {
        // Clear the editor. We have to use this as we don't have any 'editingFinished' hook regardless of actual state
        // (essentially the information whether OK or Cancel has been pressed). This operation is called before the
        // editor is visualised and afterwards, too. In case of a 'OK-Button-Pressed' this operation is called right
        // AFTER set and thus it is safe to clear the panel content. However, as said before it is also called before
        // and hence it is not a good idea to clean the PropertyEnv list as we will loose the PCL and will never be
        // informed of the current validity state.
        pwPanel.clear();

        return super.getValue();
    }
}
