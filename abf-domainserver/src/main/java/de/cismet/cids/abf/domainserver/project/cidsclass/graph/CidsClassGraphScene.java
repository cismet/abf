/*
 * CidsClassGraphScene.java
 *
 * Created on 15. Januar 2007, 11:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cids.abf.domainserver.project.cidsclass.graph;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.cidsclass.CidsClassNode;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import java.awt.Image;
import java.awt.Point;
import java.util.Arrays;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author hell
 */
public class CidsClassGraphScene extends ClassGraphScene{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private DomainserverProject project;
    /** Creates a new instance of CidsClassGraphScene */
    public CidsClassGraphScene(DomainserverProject project) {
        this.project=project;
    }
    
    public void addClassToScene(CidsClassNode cidsClassNode) {
        CidsClass cidsClass=cidsClassNode.getCidsClass();
        String widgetName=cidsClass.getId()+"@"+cidsClassNode.getDomainserverProject().getDomainserverProjectNode().getName();
        ClassNodeWidget w1=(ClassNodeWidget)addNode(widgetName);
        w1.setPreferredLocation (new Point (0, 0));
        Image classIcon=ProjectUtils.getImageForIconAndProject(cidsClass.getClassIcon(),project);
        Image objectIcon=ProjectUtils.getImageForIconAndProject(cidsClass.getObjectIcon(),project);
        w1.setNodeProperties(cidsClassNode.getIcon(0),cidsClass.getTableName(),cidsClass.getTableName(),Arrays.asList(classIcon,objectIcon));

        
        Widget p=addPin(widgetName,widgetName+ClassGraphScene.PIN_ID_DEFAULT_SUFFIX);
        
        Children c=cidsClassNode.getChildren();
        Node[] childs=c.getNodes();
        for (Node n:childs) {
            log.debug(n);
        }
//        Widget pin=addPin(widgetName,widgetName+attrName);
//        if (pin != null) {
//            ((AttributeWidget)pin).setPinName("Terror");
//            ((AttributeWidget)pin).setGlyphs(Arrays.asList(TESTER,TESTER));
//        }
    }
    
    
}
