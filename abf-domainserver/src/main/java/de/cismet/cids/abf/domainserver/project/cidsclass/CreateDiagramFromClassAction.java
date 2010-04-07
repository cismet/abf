package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.nodes.ViewManagement;
import org.openide.util.NbBundle;

public final class CreateDiagramFromClassAction extends DiagramAction{
    
    public void performAction() {
        ClassDiagramTopComponent diagram=ClassDiagramTopComponent.getDefault();
        diagram.setDomainserverProject(getDomainserverprojectForSelectedCidsClassNodes());
        diagram.addClasses(getSelectedCidsClassNodes());
        diagram.open();
        diagram.requestActive();
        ((ViewManagement)diagram.getDomainserverProject().getLookup().lookup(ViewManagement.class)).refreshChildren();
    }
    
    public String getName() {
        return NbBundle.getMessage(CreateDiagramFromClassAction.class, "CTL_CreateDiagramFromClassAction");
    }
    
    protected String iconResource() {
        return "de/cismet/cids/abf/abfcore/projecttypes/domainserver/nodes/classmanagement/actions/NewClassDiagram.png";
    }
    
    
}
