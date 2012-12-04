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

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.event.ChangeListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;

import de.cismet.tools.Equals;

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
    public static final String PROP_ENTRIES = "__entries__";              // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient ChangeSupport changeSupport;

    private transient NewEntryVisualPanel1 component;
    private transient WizardDescriptor wizard;
    private transient ConfigAttrKey key;
    private transient Types type;
    private transient DomainserverProject project;
    private transient List<ConfigAttrEntry> caes;
    private transient ConfigAttrEntry currentEntry;
    private transient EntryListModel entryListModel;
    private transient int entryAddAnswer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewEntryWizardPanel1 object.
     */
    public NewEntryWizardPanel1() {
        changeSupport = new ChangeSupport(this);
        entryListModel = new EntryListModel();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new NewEntryVisualPanel1(this);
        }

        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ListModel getEntryListModel() {
        return entryListModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<ConfigAttrEntry> getEntries() {
        return caes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cae  DOCUMENT ME!
     */
    public void addEntry(final ConfigAttrEntry cae) {
        entryListModel.addEntry(cae);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cae  DOCUMENT ME!
     */
    public void removeEntry(final ConfigAttrEntry cae) {
        entryListModel.removeEntry(cae);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ConfigAttrEntry getCurrentEntry() {
        return currentEntry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cae  DOCUMENT ME!
     */
    public void setCurrentEntry(final ConfigAttrEntry cae) {
        currentEntry = cae;

        changeSupport.fireChange();
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        if (entryContained(currentEntry, caes)) {
            wizard.putProperty(
                WizardDescriptor.PROP_INFO_MESSAGE,
                NbBundle.getMessage(
                    NewEntryWizardPanel1.class,
                    "NewEntryWizardPanel1.isValid().wizard.entryAlreadyPresent")); // NOI18N
        } else {
            wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cae   DOCUMENT ME!
     * @param   caes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean entryContained(final ConfigAttrEntry cae, final List<ConfigAttrEntry> caes) {
        for (final ConfigAttrEntry entry : caes) {
            if (Equals.beanDeepEqual(cae, entry, "getId", "getValue")) { // NOI18N
                return true;
            }
        }

        return false;
    }

    @Override
    public final void addChangeListener(final ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(final ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;
        key = (ConfigAttrKey)wizard.getProperty(PROP_ENTRY_KEY);
        type = (Types)wizard.getProperty(PROP_ENTRY_TYPE);
        project = (DomainserverProject)wizard.getProperty(PROP_PROJECT);
        caes = (List)wizard.getProperty(PROP_ENTRIES);
        // initial case
        if (caes == null) {
            if (key.getId() == null) {
                caes = new ArrayList<ConfigAttrEntry>();
            } else {
                caes = project.getCidsDataObjectBackend().getEntries(key);
            }
        }
        entryAddAnswer = -1;

        component.init();
    }

    @Override
    public void storeSettings(final Object settings) {
        wizard = (WizardDescriptor)settings;

        // we have to store the answer since this operation is invoked multiple times on finish
        if (WizardDescriptor.FINISH_OPTION.equals(wizard.getValue()) && (entryAddAnswer < 0)
                    && !entryContained(currentEntry, caes)) {
            entryAddAnswer = JOptionPane.showConfirmDialog(
                    component,
                    "The currently chosen entry is not contained in the entry list, yet. Do you want to add it?",
                    "Add entry",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (JOptionPane.YES_OPTION == entryAddAnswer) {
                caes.add(currentEntry);
            }
        }

        wizard.putProperty(PROP_ENTRIES, caes);
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class EntryListModel extends AbstractListModel {

        //~ Methods ------------------------------------------------------------

        @Override
        public int getSize() {
            return caes.size();
        }

        @Override
        public Object getElementAt(final int index) {
            return caes.get(index);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  cae  DOCUMENT ME!
         */
        public void addEntry(final ConfigAttrEntry cae) {
            caes.add(cae);

            fireIntervalAdded(this, caes.size() - 1, caes.size() - 1);
            changeSupport.fireChange();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  cae  DOCUMENT ME!
         */
        public void removeEntry(final ConfigAttrEntry cae) {
            final int index = caes.indexOf(cae);
            if (index >= 0) {
                caes.remove(cae);

                fireIntervalRemoved(this, index, index);
                changeSupport.fireChange();
            }
        }
    }
}
