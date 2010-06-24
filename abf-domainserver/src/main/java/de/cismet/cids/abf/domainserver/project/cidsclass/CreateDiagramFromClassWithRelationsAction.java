package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.ViewManagement;
import org.openide.util.NbBundle;

public final class CreateDiagramFromClassWithRelationsAction extends DiagramAction{
    
    public void performAction() {
       ClassDiagramTopComponent diagram=ClassDiagramTopComponent.getDefault();
       diagram.setDomainserverProject(getDomainserverprojectForSelectedCidsClassNodes());
       diagram.addClassesWithRelations(getSelectedCidsClassNodes());
       diagram.open();
       diagram.requestActive();
       ((ViewManagement)diagram.getDomainserverProject().getLookup().lookup(ViewManagement.class)).refreshChildren();
    }
    
    public String getName() {
        return NbBundle.getMessage(CreateDiagramFromClassWithRelationsAction.class,
                "CreateDiagramFromClassWithRelationsAction.getName().returnvalue");
    }
    
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "new_class_diagram_with_relations.png";
    }
    
    
    
    
}
