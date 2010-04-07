/*
 * ClassGraphScene.java
 *
 * Created on 8. Januar 2007, 11:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cids.abf.domainserver.project.cidsclass.graph;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
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

/**
 *
 * @author hell
 */
public class ClassGraphScene  extends GraphPinScene <String, String, String> {
    private LayerWidget backgroundLayer = new LayerWidget(this);
    private LayerWidget mainLayer = new LayerWidget(this);
    private LayerWidget connectionLayer = new LayerWidget(this);
    private LayerWidget upperLayer = new LayerWidget(this);
    private WidgetAction singleSelectAction = ActionFactory.createSelectAction(new ObjectSingleSelectProvider());
    public static final String PIN_ID_DEFAULT_SUFFIX = "#default"; // NOI18N
    
    
    private Router router;
    
    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction();
    private WidgetAction moveAction = ActionFactory.createMoveAction();
    
    private SceneLayout sceneLayout;
    public  Layout absoluteLayout;
    /** Creates a new instance of ClassGraphScene */
    public ClassGraphScene() {
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);
        
        addChild(backgroundLayer);
        addChild(mainLayer);
        addChild(connectionLayer);
        addChild(upperLayer);
        
        router = RouterFactory.createOrthogonalSearchRouter(mainLayer, connectionLayer);
        //router = RouterFactory.createFreeRouter();
        
        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
        //getActions().addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));
        
        
        sceneLayout = LayoutFactory.createSceneGraphLayout(this, new GridGraphLayout<String, String> ().setChecker(true));
        absoluteLayout=LayoutFactory.createAbsoluteLayout();
//
//        sceneLayout = LayoutFactory.createSceneGraphLayout(this, new TreeGraphLayout((GraphScene)this.getScene(),100,100,64,64,true));
//
    }
    
    /**
     * Implements attaching a widget to a node. The widget is VMDNodeWidget and has object-hover, select, popup-menu and move actions.
     * @param node the node
     * @return the widget attached to the node
     */
    protected Widget attachNodeWidget(String node) {
        ClassNodeWidget widget = new ClassNodeWidget(this);
        mainLayer.addChild(widget);
        
        widget.getHeader().getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSingleSelectAction());
        widget.getActions().addAction(moveAction);
        
        return widget;
    }
    /**
     * Creates a object-oriented select action.
     * @return the object-oriented select action
     */
    public final WidgetAction createSingleSelectAction() {
        return singleSelectAction;
    }
    
    /**
     * Implements attaching a widget to a pin. The widget is VMDPinWidget and has object-hover and select action.
     * The the node id ends with "#default" then the pin is the default pin of a node and therefore it is non-visual.
     * @param node the node
     * @param pin the pin
     * @return the widget attached to the pin, null, if it is a default pin
     */
    protected Widget attachPinWidget(String node, String pin) {
        if (pin.endsWith(PIN_ID_DEFAULT_SUFFIX))
            return null;
        
        AttributeWidget widget = new AttributeWidget(this);
        ((ClassNodeWidget) findWidget(node)).attachPinWidget(widget);
        widget.getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSingleSelectAction());
        
        return widget;
    }
    
    /**
     * Implements attaching a widget to an edge. the widget is ConnectionWidget and has object-hover, select and move-control-point actions.
     * @param edge the edge
     * @return the widget attached to the edge
     */
    protected Widget attachEdgeWidget(String edge) {
        RelationWidget connectionWidget = new RelationWidget(this, router);
        connectionLayer.addChild(connectionWidget);
        
        connectionWidget.getActions().addAction(createObjectHoverAction());
        connectionWidget.getActions().addAction(createSingleSelectAction());
        connectionWidget.getActions().addAction(moveControlPointAction);
        
        return connectionWidget;
    }
    
    /**
     * Attaches an anchor of a source pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldSourcePin the old source pin
     * @param sourcePin the new source pin
     */
    protected void attachEdgeSourceAnchor(String edge, String oldSourcePin, String sourcePin) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(getPinAnchor(sourcePin));
    }
    
    /**
     * Attaches an anchor of a target pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldTargetPin the old target pin
     * @param targetPin the new target pin
     */
    protected void attachEdgeTargetAnchor(String edge, String oldTargetPin, String targetPin) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(getPinAnchor(targetPin));
    }
    
    private Anchor getPinAnchor(String pin) {
        if (pin==null ) return null;
        ClassNodeWidget nodeWidget = (ClassNodeWidget) findWidget(getPinNode(pin));
        Widget pinMainWidget = findWidget(pin);
        Anchor anchor;
        if (pinMainWidget != null) {
            anchor = AnchorFactory.createDirectionalAnchor(pinMainWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 8);
            anchor = nodeWidget.createAnchorPin(anchor);
        } else
            anchor = nodeWidget.getNodeAnchor();
        return anchor;
    }
    
    /**
     * Invokes layout of the scene.
     */
    public void layoutScene() {
        //sceneLayout.invokeLayout();
        sceneLayout.invokeLayoutImmediately();
    }
    private class ObjectSingleSelectProvider implements SelectProvider {
        
        public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }
        
        public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            //singleSelect
            invertSelection=false;
            
            Object object = findObject(widget);
            return object != null  &&  (invertSelection  ||  ! getSelectedObjects().contains(object));
        }
        
        public void select(Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject(widget);
            
            //singleSelect
            invertSelection=false;
            
            
            setFocusedObject(object);
            if (object != null) {
//                if (getSelectedObjects ().contains (object))
//                    return;
                userSelectionSuggested(Collections.singleton(object), invertSelection);
            } else
                userSelectionSuggested(Collections.emptySet(), invertSelection);
        }
    }
    
    
}



