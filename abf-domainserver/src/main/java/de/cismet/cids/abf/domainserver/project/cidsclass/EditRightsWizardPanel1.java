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

import javax.swing.event.ChangeListener;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class EditRightsWizardPanel1 implements WizardDescriptor.Panel {

    //~ Instance fields --------------------------------------------------------

    private transient EditRightsVisualPanel1 component;
    private transient CidsClass[] classes;
    private transient Backend someBackend;

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new EditRightsVisualPanel1(this);
        }
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsClass[] getCidsClasses() {
        return classes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Backend getBackend() {
        return someBackend;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        // not needed
    }

    @Override
    public void removeChangeListener(final ChangeListener l) {
        // not needed
    }

    @Override
    public void readSettings(final Object settings) {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        classes = (CidsClass[])wizard.getProperty(EditRightsWizardAction.PROP_ARRAY_CIDSCLASSES);
        someBackend = (Backend)wizard.getProperty(EditRightsWizardAction.PROP_BACKEND);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        // not needed
    }
}
