/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * CidsClassGraphScene.java
 *
 * Created on 15. Januar 2007, 11:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cids.abf.domainserver.project.cidsclass.graph;

import org.netbeans.api.visual.widget.Widget;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.awt.Image;
import java.awt.Point;

import java.util.Arrays;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.cidsclass.CidsClassNode;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;

import de.cismet.cids.jpa.entity.cidsclass.CidsClass;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class CidsClassGraphScene extends ClassGraphScene {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private DomainserverProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CidsClassGraphScene.
     *
     * @param  project  DOCUMENT ME!
     */
    public CidsClassGraphScene(final DomainserverProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  cidsClassNode  DOCUMENT ME!
     */
    public void addClassToScene(final CidsClassNode cidsClassNode) {
        final CidsClass cidsClass = cidsClassNode.getCidsClass();
        final String widgetName = cidsClass.getId() + "@"
                    + cidsClassNode.getDomainserverProject().getDomainserverProjectNode().getName();
        final ClassNodeWidget w1 = (ClassNodeWidget)addNode(widgetName);
        w1.setPreferredLocation(new Point(0, 0));
        final Image classIcon = ProjectUtils.getImageForIconAndProject(cidsClass.getClassIcon(), project);
        final Image objectIcon = ProjectUtils.getImageForIconAndProject(cidsClass.getObjectIcon(), project);
        w1.setNodeProperties(cidsClassNode.getIcon(0),
            cidsClass.getTableName(),
            cidsClass.getTableName(),
            Arrays.asList(classIcon, objectIcon));

        final Widget p = addPin(widgetName, widgetName + ClassGraphScene.PIN_ID_DEFAULT_SUFFIX);

        final Children c = cidsClassNode.getChildren();
        final Node[] childs = c.getNodes();
        for (final Node n : childs) {
            if (log.isDebugEnabled()) {
                log.debug(n);
            }
        }
//        Widget pin=addPin(widgetName,widgetName+attrName);
//        if (pin != null) {
//            ((AttributeWidget)pin).setPinName("Terror");
//            ((AttributeWidget)pin).setGlyphs(Arrays.asList(TESTER,TESTER));
//        }
    }
}
