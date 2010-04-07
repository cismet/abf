package de.cismet.cids.abf.cidsjavatemplate;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
public final class CidsJavaTemplateWizardPanel implements
        WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, 
        WizardDescriptor.FinishablePanel
{
    
    private transient WizardDescriptor wizardDescriptor;
    private transient CidsJavaTemplatePanelVisual component;
    private final transient Set/*<ChangeListener>*/ listeners;
    
    /** Creates a new instance of templateWizardPanel */
    public CidsJavaTemplateWizardPanel()
    {
        listeners = new HashSet(1);
    }
    
    @Override
    public Component getComponent()
    {
        if (component == null)
        {
            component = new CidsJavaTemplatePanelVisual(this);
            component.setName(NbBundle.getMessage(CidsJavaTemplateWizardPanel.
                    class, "LBL_CreateProjectStep")); // NOI18N
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp()
    {
        return new HelpCtx(CidsJavaTemplateWizardPanel.class);
    }
    
    @Override
    public boolean isValid()
    {
        getComponent();
        if(!component.valid(wizardDescriptor))
        {
            return false;
        }
        if(component.getChoosenFile() == null
                || !isDistributionDir(component.getChoosenFile()))
        {
            return false;
        }
        return true;
    }
    
    boolean isDistributionDir(final File f)
    {
        final File pp = new File(f,
                "lib/cidsLibBase/project.properties"); // NOI18N
        return pp.exists() ? true : false;
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
        final Iterator it;
        synchronized(listeners)
        {
            it = new HashSet(listeners).iterator();
        }
        final ChangeEvent ev = new ChangeEvent(this);
        while(it.hasNext())
        {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    @Override
    public void readSettings(final Object settings)
    {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
    }
    
    @Override
    public void storeSettings(final Object settings)
    {
        final WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
    }
    
    @Override
    public boolean isFinishPanel()
    {
        return true;
    }
    
    @Override
    public void validate() throws WizardValidationException
    {
        getComponent();
        component.validate(wizardDescriptor);
    }   
}