/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ClassGraphScene.java
 *
 * Created on 8. Januar 2007, 11:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cids.abf.domainserver.project.cidsclass.graph;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collections;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class ClassGraphScene extends GraphPinScene<String, String, String> {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PIN_ID_DEFAULT_SUFFIX = "#default"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    public Layout absoluteLayout;
    private LayerWidget backgroundLayer = new LayerWidget(this);
    private LayerWidget mainLayer = new LayerWidget(this);
    private LayerWidget connectionLayer = new LayerWidget(this);
    private LayerWidget upperLayer = new LayerWidget(this);
    private WidgetAction singleSelectAction = ActionFactory.createSelectAction(new ObjectSingleSelectProvider());

    private Router router;

    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction();
    private WidgetAction moveAction = ActionFactory.createMoveAction();

    private SceneLayout sceneLayout;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ClassGraphScene.
     */
    public ClassGraphScene() {
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        addChild(backgroundLayer);
        addChild(mainLayer);
        addChild(connectionLayer);
        addChild(upperLayer);

        router = RouterFactory.createOrthogonalSearchRouter(mainLayer, connectionLayer);
        // router = RouterFactory.createFreeRouter();

        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
        // getActions().addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));

        sceneLayout = LayoutFactory.createSceneGraphLayout(
                this,
                new GridGraphLayout<String, String>().setChecker(true));
        absoluteLayout = LayoutFactory.createAbsoluteLayout();
//
//        sceneLayout = LayoutFactory.createSceneGraphLayout(this, new TreeGraphLayout((GraphScene)this.getScene(),100,100,64,64,true));
//
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Implements attaching a widget to a node. The widget is VMDNodeWidget and has object-hover, select, popup-menu and
     * move actions.
     *
     * @param   node  the node
     *
     * @return  the widget attached to the node
     */
    @Override
    protected Widget attachNodeWidget(final String node) {
        final ClassNodeWidget widget = new ClassNodeWidget(this);
        mainLayer.addChild(widget);

        widget.getHeader().getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSingleSelectAction());
        widget.getActions().addAction(moveAction);

        return widget;
    }
    /**
     * Creates a object-oriented select action.
     *
     * @return  the object-oriented select action
     */
    public final WidgetAction createSingleSelectAction() {
        return singleSelectAction;
    }

    /**
     * Implements attaching a widget to a pin. The widget is VMDPinWidget and has object-hover and select action. The
     * the node id ends with "#default" then the pin is the default pin of a node and therefore it is non-visual.
     *
     * @param   node  the node
     * @param   pin   the pin
     *
     * @return  the widget attached to the pin, null, if it is a default pin
     */
    @Override
    protected Widget attachPinWidget(final String node, final String pin) {
        if (pin.endsWith(PIN_ID_DEFAULT_SUFFIX)) {
            return null;
        }

        final AttributeWidget widget = new AttributeWidget(this);
        ((ClassNodeWidget)findWidget(node)).attachPinWidget(widget);
        widget.getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSingleSelectAction());

        return widget;
    }

    /**
     * Implements attaching a widget to an edge. the widget is ConnectionWidget and has object-hover, select and
     * move-control-point actions.
     *
     * @param   edge  the edge
     *
     * @return  the widget attached to the edge
     */
    @Override
    protected Widget attachEdgeWidget(final String edge) {
        final RelationWidget connectionWidget = new RelationWidget(this, router);
        connectionLayer.addChild(connectionWidget);

        connectionWidget.getActions().addAction(createObjectHoverAction());
        connectionWidget.getActions().addAction(createSingleSelectAction());
        connectionWidget.getActions().addAction(moveControlPointAction);

        return connectionWidget;
    }

    /**
     * Attaches an anchor of a source pin an edge. The anchor is a ProxyAnchor that switches between the anchor attached
     * to the pin widget directly and the anchor attached to the pin node widget based on the minimize-state of the
     * node.
     *
     * @param  edge          the edge
     * @param  oldSourcePin  the old source pin
     * @param  sourcePin     the new source pin
     */
    @Override
    protected void attachEdgeSourceAnchor(final String edge, final String oldSourcePin, final String sourcePin) {
        ((ConnectionWidget)findWidget(edge)).setSourceAnchor(getPinAnchor(sourcePin));
    }

    /**
     * Attaches an anchor of a target pin an edge. The anchor is a ProxyAnchor that switches between the anchor attached
     * to the pin widget directly and the anchor attached to the pin node widget based on the minimize-state of the
     * node.
     *
     * @param  edge          the edge
     * @param  oldTargetPin  the old target pin
     * @param  targetPin     the new target pin
     */
    @Override
    protected void attachEdgeTargetAnchor(final String edge, final String oldTargetPin, final String targetPin) {
        ((ConnectionWidget)findWidget(edge)).setTargetAnchor(getPinAnchor(targetPin));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pin  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Anchor getPinAnchor(final String pin) {
        if (pin == null) {
            return null;
        }
        final ClassNodeWidget nodeWidget = (ClassNodeWidget)findWidget(getPinNode(pin));
        final Widget pinMainWidget = findWidget(pin);
        Anchor anchor;
        if (pinMainWidget != null) {
            anchor = AnchorFactory.createDirectionalAnchor(
                    pinMainWidget,
                    AnchorFactory.DirectionalAnchorKind.HORIZONTAL,
                    8);
            anchor = nodeWidget.createAnchorPin(anchor);
        } else {
            anchor = nodeWidget.getNodeAnchor();
        }
        return anchor;
    }

    /**
     * Invokes layout of the scene.
     */
    public void layoutScene() {
        // sceneLayout.invokeLayout();
        sceneLayout.invokeLayoutImmediately();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ObjectSingleSelectProvider implements SelectProvider {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isAimingAllowed(final Widget widget, final Point localLocation, final boolean invertSelection) {
            return false;
        }

        @Override
        public boolean isSelectionAllowed(final Widget widget, final Point localLocation, boolean invertSelection) {
            // singleSelect
            invertSelection = false;

            final Object object = findObject(widget);
            return (object != null) && (invertSelection || !getSelectedObjects().contains(object));
        }

        @Override
        public void select(final Widget widget, final Point localLocation, boolean invertSelection) {
            final Object object = findObject(widget);

            // singleSelect
            invertSelection = false;

            setFocusedObject(object);
            if (object != null) {
//                if (getSelectedObjects ().contains (object))
//                    return;
                userSelectionSuggested(Collections.singleton(object), invertSelection);
            } else {
                userSelectionSuggested(Collections.emptySet(), invertSelection);
            }
        }
    }
}
