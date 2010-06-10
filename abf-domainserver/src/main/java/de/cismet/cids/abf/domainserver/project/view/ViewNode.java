/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.view;

import org.dom4j.Document;

import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.RenameAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;

import java.awt.EventQueue;
import java.awt.Image;

import java.io.File;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.cidsclass.ClassDiagramTopComponent;
import de.cismet.cids.abf.domainserver.project.nodes.ViewManagement;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.4
 */
public final class ViewNode extends ProjectNode implements Comparable<ViewNode> {

    //~ Instance fields --------------------------------------------------------

    private final transient Image viewImage;
    private final transient Image unstoredViewImage;

    private Document view;
    private ClassDiagramTopComponent topComponent = null;

    private transient boolean isTransient;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ViewNode.
     *
     * @param  view     DOCUMENT ME!
     * @param  project  DOCUMENT ME!
     */
    public ViewNode(final Document view, final DomainserverProject project) {
        super(Children.LEAF, project);
        this.view = view;
        getCookieSet().add(new OpenCookie() {

                @Override
                public void open() {
                    if (topComponent == null) {
                        setTopComponent(ClassDiagramTopComponent.getDefault());
                        topComponent.setDomainserverProject(project);
                        topComponent.setViewNode(ViewNode.this);
                        topComponent.restoreFromDocument(ViewNode.this.view);
                        topComponent.open();
                    }
                    topComponent.requestActive();
                }
            });
        viewImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "view.png");                 // NOI18N
        unstoredViewImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "unstoredView.png"); // NOI18N
        super.setName(view.getRootElement().valueOf("//View/@name"));                                        // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int i) {
        if (isTransient) {
            return unstoredViewImage;
        } else {
            return viewImage;
        }
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return getIcon(i);
    }

    @Override
    public void setName(final String string) {
        final String newName = ClassDiagramTopComponent.getFreeViewname(project, string);
        final String oldName = getName();
        final String oldFileName = ClassDiagramTopComponent.getFileNameOfView(oldName);
        final String newFileName = ClassDiagramTopComponent.getFileNameOfView(newName);
        final FileObject base = project.getProjectDirectory();
        final File old = FileUtil.toFile(base.getFileObject(oldFileName));
        old.renameTo(new File(FileUtil.toFile(base), newFileName));
        if (topComponent != null) {
            topComponent.setViewName(newName, true);
        }
        super.setName(newName);
        view.getRootElement().selectSingleNode("//View/@name").setText(newName); // NOI18N
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] {
                CallableSystemAction.get(OpenAction.class),
                CallableSystemAction.get(RenameAction.class),
                CallableSystemAction.get(DeleteAction.class)
            };
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public Action getPreferredAction() {
        return CallableSystemAction.get(OpenAction.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Document getView() {
        return view;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  view  DOCUMENT ME!
     */
    public void setView(final Document view) {
        this.view = view;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ClassDiagramTopComponent getTopComponent() {
        return topComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  topComponent  DOCUMENT ME!
     */
    public void setTopComponent(final ClassDiagramTopComponent topComponent) {
        this.topComponent = topComponent;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() {
        if (topComponent == null) {
            deleteView();
        } else {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        topComponent.setSaveChanges(false);
                        topComponent.close();
                        // to ensure the file is deleted after the TC is closed
                        RequestProcessor.getDefault().post(new Runnable() {

                                @Override
                                public void run() {
                                    deleteView();
                                }
                            });
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void deleteView() {
        final File f = FileUtil.toFile(project.getProjectDirectory().getFileObject(
                    ClassDiagramTopComponent.getFileNameOfView(getName())));
        f.delete();
        project.getLookup().lookup(ViewManagement.class).refreshChildren();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hot  DOCUMENT ME!
     */
    public void setTransient(final boolean hot) {
        this.isTransient = hot;
    }

    @Override
    public boolean equals(final Object object) {
        if (object instanceof ViewNode) {
            return getName().equals(((ViewNode)object).getName());
        }
        return false;
    }

    @Override
    public int compareTo(final ViewNode o) {
        return getName().compareTo(o.getName());
    }
}
