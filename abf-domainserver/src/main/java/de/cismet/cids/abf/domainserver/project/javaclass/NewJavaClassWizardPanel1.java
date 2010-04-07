package de.cismet.cids.abf.domainserver.project.javaclass;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.NoResultException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public final class NewJavaClassWizardPanel1 implements WizardDescriptor.Panel
{
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private final transient DomainserverProject project;
    private final transient Set<ChangeListener> listeners;
    private transient NewJavaClassVisualPanel1 component;
    private transient WizardDescriptor wizard;
    
    NewJavaClassWizardPanel1(final DomainserverProject project)
    {
        this.project = project;
        listeners = new HashSet<ChangeListener>(1);
        // init component
        getComponent();
    }
    
    @Override
    public Component getComponent()
    {
        if (component == null)
        {
            component = new NewJavaClassVisualPanel1(project, this);
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public boolean isValid()
    {
        final String qualifier = component.getJavaClass().getQualifier();
        try
        {
            project.getCidsDataObjectBackend().getJavaClass(qualifier);
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewJavaClassWizardPanel1.class,
                        "Err_classAlreadyPresent")); // NOI18N
            return false;
        }catch(final NoResultException nre)
        {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
            return true;
        }
    }
    
    @Override
    public void addChangeListener(final ChangeListener l)
    {
        synchronized(listeners)
        {
            listeners.add(l);
        }
    }
    
    @Override
    public void removeChangeListener(final ChangeListener l)
    {
        synchronized(listeners)
        {
            listeners.remove(l);
        }
    }
    
    protected void fireChangeEvent()
    {
        final Iterator<ChangeListener> it;
        synchronized(listeners)
        {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext())
        {
            it.next().stateChanged(ev);
        }
    }
    
    @Override
    public void readSettings(final Object settings)
    {
        wizard = (WizardDescriptor)settings;
    }
    
    @Override
    public void storeSettings(final Object settings)
    {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewJavaClassWizardAction.JAVACLASS_PROPERTY,
                component.getJavaClass());
    }
}