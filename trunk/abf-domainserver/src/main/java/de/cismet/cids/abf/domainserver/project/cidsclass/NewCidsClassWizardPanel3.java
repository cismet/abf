/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class NewCidsClassWizardPanel3 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private final transient Set<ChangeListener> listeners;
    private transient NewCidsClassVisualPanel3 component;
    private transient WizardDescriptor wizard;
    private transient DomainserverProject project;
    private transient CidsClass cidsClass;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewCidsClassWizardPanel3 object.
     */
    public NewCidsClassWizardPanel3() {
        listeners = new HashSet<ChangeListener>(1);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NewCidsClassVisualPanel3(this);
        }
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    DomainserverProject getProject() {
        return project;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsClass getCidsClass() {
        return cidsClass;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        final Set<ClassAttribute> cas = component.getCidsClass().getClassAttributes();
        final ArrayList<String> keys = new ArrayList<String>(cas.size());
        for (final ClassAttribute ca : cas) {
            if ((ca.getAttrKey() == null) || ca.getAttrKey().trim().equals("")) {
                wizard.putProperty("WizardPanel_errorMessage", "Leere Schlüss"
                            + "el sind nicht gültig");
                return false;
            }
            if (keys.contains(ca.getAttrKey())) {
                wizard.putProperty("WizardPanel_errorMessage", "Doppelte Schl"
                            + "üssel sind nicht gültig");
                return false;
            } else {
                keys.add(ca.getAttrKey());
            }
        }
        wizard.putProperty("WizardPanel_errorMessage", null);
        // If it is always OK to press Next or Finish, then:
        return true;
            // If it depends on some condition (form filled out...), then:
            // return someCondition();
            // and when this condition changes (last form field filled in...) then:
            // fireChangeEvent();
            // and uncomment the complicated stuff below.
    }

    @Override
    public final void addChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected final void fireChangeEvent() {
        final Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        project = (DomainserverProject)wizard.getProperty(
                NewCidsClassWizardAction.PROJECT_PROP);
        cidsClass = (CidsClass)wizard.getProperty(
                NewCidsClassWizardAction.CIDS_CLASS_PROP);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewCidsClassWizardAction.CIDS_CLASS_PROP, component.getCidsClass());
    }
}
