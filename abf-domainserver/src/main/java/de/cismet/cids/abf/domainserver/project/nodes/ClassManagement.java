/***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
package de.cismet.cids.abf.domainserver.project.nodes;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.cidsclass.CidsClassNode;
import de.cismet.cids.abf.domainserver.project.cidsclass.ClassManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.cidsclass.ImportClassesAction;
import de.cismet.cids.abf.domainserver.project.cidsclass.NewCidsClassWizardAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.cidsclass.CidsClass;

import java.awt.EventQueue;
import java.awt.Image;

import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import org.apache.log4j.Logger;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class ClassManagement extends ProjectNode implements Refreshable,
        ConnectionListener,
        ClassManagementContextCookie
{

    //~ Instance fields --------------------------------------------------------
    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------
    /**
     * Creates a new instance of ClassManagement.
     *
     * @param  project  DOCUMENT ME!
     */
    public ClassManagement(final DomainserverProject project)
    {
        super(Children.LEAF, project);
        getCookieSet().add(this);
        project.addConnectionListener(this);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "class_management.png"); // NOI18N
        setDisplayName(NbBundle.getMessage(ClassManagement.class, "Dsc_classes")); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @param   i  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Image getIcon(final int i)
    {
        return nodeImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   i  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Image getOpenedIcon(final int i)
    {
        return nodeImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isConnected  DOCUMENT ME!
     */
    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        if(project.isConnected())
        {
            setChildren(new ClassManagementChildren(project));
        }else
        {
            setChildren(Children.LEAF);
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void connectionStatusIndeterminate()
    {
        // not needed
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Action[] getActions(final boolean context)
    {
        return new Action[]
                {
                    CallableSystemAction.get(NewCidsClassWizardAction.class),
                    null,
                    CallableSystemAction.get(ImportClassesAction.class)
                };
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void refresh()
    {
        final Children ch = getChildren();
        if(ch instanceof ClassManagementChildren)
        {
            ((ClassManagementChildren) ch).refreshAll();
        }
    }

}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class ClassManagementChildren extends Children.Keys
{

    //~ Static fields/initializers ---------------------------------------------
    private static final transient Logger LOG = Logger.getLogger(ClassManagementChildren.class);
    //~ Instance fields --------------------------------------------------------
    private final transient DomainserverProject project;
    private transient LoadingNode loadingNode;

    //~ Constructors -----------------------------------------------------------
    /**
     * Creates a new ClassManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ClassManagementChildren(final DomainserverProject project)
    {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @param   object  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof LoadingNode)
        {
            return new Node[]
                    {
                        (LoadingNode) object
                    };
        }
        if(object instanceof CidsClass)
        {
            return new Node[]
                    {
                        new CidsClassNode((CidsClass) object, project)
                    };
        }
        return new Node[] {};
    }

    /**
     * DOCUMENT ME!
     */
    void refreshAll()
    {
        addNotify();
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    protected void addNotify()
    {
        loadingNode = new LoadingNode();
        setKeys(new Object[]
                {
                    loadingNode
                });
        refresh();
        final Thread t = new Thread(
                new Runnable()
                {

                    @Override
                    public void run()
                    {
                        try
                        {
                            final List<CidsClass> allClasses =
                                    project.getCidsDataObjectBackend().getAllEntities(CidsClass.class);
                            Collections.sort(allClasses, new Comparators.CidsClasses());
                            EventQueue.invokeLater(new Runnable()
                            {

                                @Override
                                public void run()
                                {
                                    setKeys(allClasses);
                                }

                            });
                            if(loadingNode != null)
                            {
                                loadingNode.dispose();
                                loadingNode = null;
                            }
                        }catch(final Exception ex)
                        {
                            LOG.error("could not fetch all classes from backend", ex); // NOI18N
                            ErrorUtils.showErrorMessage(
                                    NbBundle.getMessage(ClassManagementChildren.class, "Err_requestingClasses"),//NOI18N
                                    ex);
                        }
                    }

                },
                getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
        t.start();
    }

}
