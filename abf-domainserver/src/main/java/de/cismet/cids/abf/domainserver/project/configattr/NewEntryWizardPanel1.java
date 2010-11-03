/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.configattr;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.Component;

import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NewEntryWizardPanel1 implements WizardDescriptor.Panel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_PROJECT = "__domainserver_project__"; // NOI18N
    public static final String PROP_ENTRY_TYPE = "__entry_type__";        // NOI18N
    public static final String PROP_ENTRY_KEY = "__entry_key__";          // NOI18N
    public static final String PROP_ENTRY = "__entry__";                  // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient ChangeSupport changeSupport;

    private transient NewEntryVisualPanel1 component;
    private transient WizardDescriptor wizard;
    private transient ConfigAttrKey key;
    private transient Types type;
    private transient DomainserverProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewEntryWizardPanel1 object.
     */
    public NewEntryWizardPanel1() {
        changeSupport = new ChangeSupport(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NewEntryVisualPanel1(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        final ConfigAttrEntry entry = component.getEntry();

        // checking for a transient key first because that means that we're creating a new key, too
        if ((entry.getKey().getId() != null) && project.getCidsDataObjectBackend().contains(entry)) {
            wizard.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(
                    NewEntryWizardPanel1.class,
                    "NewEntryWizardPanel1.isValid().wizard.entryAlreadyPresent")); // NOI18N

            return false;
        } else {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);

            return true;
        }
    }

    @Override
    public final void addChangeListener(final ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(final ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /**
     * DOCUMENT ME!
     */
    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }
    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        key = (ConfigAttrKey)wizard.getProperty(PROP_ENTRY_KEY);
        type = (Types)wizard.getProperty(PROP_ENTRY_TYPE);
        project = (DomainserverProject)wizard.getProperty(PROP_PROJECT);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(PROP_ENTRY, component.getEntry());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ConfigAttrKey getKey() {
        return key;
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
    Types getType() {
        return type;
    }
}
