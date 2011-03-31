/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.javaclass;

import org.apache.log4j.Logger;

import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.beans.PropertyEditor;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.JavaClassManagement;
import de.cismet.cids.abf.utilities.CidsDistClassLoader;

import de.cismet.cids.jpa.entity.cidsclass.JavaClass;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.18
 */
public final class JavaClassNode extends ProjectNode {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(JavaClassNode.class);

    public static final String GREEN_HEX = "00C000";  // NOI18N
    public static final String ORANGE_HEX = "FF8000"; // NOI18N
    public static final String RED_HEX = "FF0000";    // NOI18N
    public static final String BLACK_HEX = "000000";  // NOI18N

    private static final RequestProcessor PROCESSOR = new RequestProcessor(JavaClassNode.class.getName(), 5);

    //~ Instance fields --------------------------------------------------------

    private final transient Image image;
    private transient JavaClass javaClass;
    // must be set to null to be newly computed
    private transient String htmlDisplayName;
    private transient String htmlColor;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JavaClassNode object.
     *
     * @param  javaClass  DOCUMENT ME!
     * @param  project    DOCUMENT ME!
     */
    public JavaClassNode(final JavaClass javaClass, final DomainserverProject project) {
        super(Children.LEAF, project);
        this.javaClass = javaClass;
        image = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "javaclass.png"); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getDisplayName() {
        final String[] parts = javaClass.getQualifier().split("\\."); // NOI18N

        return (parts.length > 0) ? parts[parts.length - 1] : getName();
    }

    @Override
    public String getName() {
        return javaClass.getQualifier();
    }

    @Override
    public String getHtmlDisplayName() {
        if (htmlDisplayName == null) {
            PROCESSOR.post(new HtmlNameLoader());
        }

        return htmlDisplayName;
    }

    @Override
    public String getShortDescription() {
        final String tooltip;
        if (htmlColor == null) {
            tooltip = super.getShortDescription();
        } else if (htmlColor.equals(GREEN_HEX)) {
            tooltip = org.openide.util.NbBundle.getMessage(
                    JavaClassNode.class,
                    "JavaClassNode.getShortDescription().tooltip.classPresentCorrectInterface");   // NOI18N
        } else if (htmlColor.equals(ORANGE_HEX)) {
            tooltip = org.openide.util.NbBundle.getMessage(
                    JavaClassNode.class,
                    "JavaClassNode.getShortDescription().tooltip.classPresentWrongInterface");     // NOI18N
        } else if (htmlColor.equals(RED_HEX)) {
            tooltip = org.openide.util.NbBundle.getMessage(
                    JavaClassNode.class,
                    "JavaClassNode.getShortDescription().tooltip.classNotPresent");                // NOI18N
        } else if (htmlColor.equals(BLACK_HEX)) {
            tooltip = org.openide.util.NbBundle.getMessage(
                    JavaClassNode.class,
                    "JavaClassNode.getShortDescription().tooltip.classNotCheckableCanWorkAnyway"); // NOI18N
        } else {
            tooltip = super.getShortDescription();
        }
        return tooltip;
    }

    @Override
    public Image getIcon(final int i) {
        return image;
    }

    @Override
    protected Sheet createSheet() {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set main = Sheet.createPropertiesSet();
        main.setName(NbBundle.getMessage(JavaClassNode.class, "JavaClassNode.createSheet().main.name"));               // NOI18N
        main.setDisplayName(NbBundle.getMessage(JavaClassNode.class, "JavaClassNode.createSheet().main.displayName")); // NOI18N

        try {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Id ">
            final Property idProp = new PropertySupport.Reflection(javaClass,
                    Integer.class, "getId", null);               // NOI18N
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    JavaClassNode.class,
                    "JavaClassNode.createSheet().idProp.name")); // NOI18N
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Qualifier ">
            final Property qualifierProp = new PropertySupport(
                    "qualifier",                                              // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        JavaClassNode.class,
                        "JavaClassNode.createSheet().qualifierProp.classname"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        JavaClassNode.class,
                        "JavaClassNode.createSheet().qualifierProp.nameOfClass"), // NOI18N
                    true,
                    true) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return javaClass.getQualifier();
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        final JavaClass old = javaClass;
                        try {
                            javaClass.setQualifier(object.toString());
                            javaClass = project.getCidsDataObjectBackend().store(
                                    javaClass);
                            fireDisplayNameChange(null, getDisplayName());
                        } catch (final Exception e) {
                            LOG.error("could not store javaclass", e); // NOI18N
                            javaClass = old;
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                };                                                     //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: Type ">
            final Property typeProp = new PropertySupport(
                    "type",                                              // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        JavaClassNode.class,
                        "JavaClassNode.createSheet().typeProp.type"),    // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        JavaClassNode.class,
                        "JavaClassNode.createSheet().typeProp.typeOfClass"), // NOI18N
                    true,
                    true) {

                    private final PropertyEditor editor = new JavaClassTypePropertyEditor();

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        final String type = javaClass.getType();
                        if (type.equals(JavaClass.Type.TO_STRING.getType())) {
                            return org.openide.util.NbBundle.getMessage(
                                    JavaClassNode.class,
                                    "JavaClassNode.createSheet().typeProp.getValue().toStringClass"); // NOI18N
                        } else if (type.equals(JavaClass.Type.RENDERER.getType())) {
                            return org.openide.util.NbBundle.getMessage(
                                    JavaClassNode.class,
                                    "JavaClassNode.createSheet().typeProp.getValue().renderer");      // NOI18N
                        } else if (type.equals(
                                        JavaClass.Type.SIMPLE_EDITOR.getType())) {
                            return org.openide.util.NbBundle.getMessage(
                                    JavaClassNode.class,
                                    "JavaClassNode.createSheet().typeProp.getValue().simpleEditor");  // NOI18N
                        } else if (type.equals(
                                        JavaClass.Type.COMPLEX_EDITOR.getType())) {
                            return org.openide.util.NbBundle.getMessage(
                                    JavaClassNode.class,
                                    "JavaClassNode.createSheet().typeProp.getValue().complexEditor"); // NOI18N
                        } else if (type.equals(JavaClass.Type.UNKNOWN.getType())) {
                            return org.openide.util.NbBundle.getMessage(
                                    JavaClassNode.class,
                                    "JavaClassNode.createSheet().typeProp.getValue().unknownClass");  // NOI18N
                        }
                        return type;
                    }

                    @Override
                    public PropertyEditor getPropertyEditor() {
                        return editor;
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        final JavaClass old = javaClass;
                        try {
                            javaClass.setType(object.toString());
                            javaClass = project.getCidsDataObjectBackend().store(
                                    javaClass);
                            htmlDisplayName = null;
                            fireDisplayNameChange(getDisplayName(), null);
                        } catch (final Exception e) {
                            javaClass = old;
                            LOG.error("could not set notice", e); // NOI18N
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                };                                                //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: Notice ">
            final Property noticeProp = new PropertySupport(
                    "notice",                                         // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        JavaClassNode.class,
                        "JavaClassNode.createSheet().noticeProp.notice"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        JavaClassNode.class,
                        "JavaClassNode.createSheet().noticeProp.notice"), // NOI18N
                    true,
                    true) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return (javaClass.getNotice() == null)
                            ? org.openide.util.NbBundle.getMessage(
                                JavaClassNode.class,
                                "JavaClassNode.createSheet().noticeProp.getValue().noNotice") : // NOI18N
                            javaClass.getNotice();
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        final JavaClass old = javaClass;
                        try {
                            javaClass.setNotice(object.toString());
                            javaClass = project.getCidsDataObjectBackend().store(
                                    javaClass);
                        } catch (final Exception e) {
                            LOG.error("could not set notice", e); // NOI18N
                            javaClass = old;
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                };                                                //</editor-fold>
            main.put(idProp);
            main.put(qualifierProp);
            main.put(typeProp);
            main.put(noticeProp);
            sheet.put(main);
        } catch (final Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }

        return sheet;
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] { CallableSystemAction.get(DeleteAction.class) };
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        try {
            project.getCidsDataObjectBackend().deleteJavaClass(javaClass);
            project.getLookup().lookup(JavaClassManagement.class).refresh();
            project.getLookup().lookup(ClassManagement.class).refresh();
        } catch (final Exception e) {
            LOG.error("error during javaclass deletion", e); // NOI18N
            ErrorManager.getDefault().notify(e);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class HtmlNameLoader implements Runnable {

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            final StringBuilder name = new StringBuilder(getDisplayName());
            final CidsDistClassLoader cdcl;
            try {
                cdcl = CidsDistClassLoader.getInstance(project.getDistRoot());
            } catch (final Exception e) {
                LOG.error("cids distribution corrupted", e); // NOI18N
                name.insert(0, "<font color=\"#\">");        // NOI18N
                name.insert(14, BLACK_HEX);
                name.append("</font>");                      // NOI18N
                htmlDisplayName = name.toString();
                fireDisplayNameChange(getDisplayName(), htmlDisplayName);

                return;
            }
            final String qualifier = javaClass.getQualifier();
            if (cdcl.isLoadable(qualifier, null)) {
                final String type = javaClass.getType();
                if (type.equals(JavaClass.Type.UNKNOWN.getType())) {
                    htmlColor = ORANGE_HEX;
                } else {
                    try {
                        final Class c = Class.forName(qualifier, false, cdcl);
                        try {
                            final Class iface = Class.forName(type, false, cdcl);
                            final Object o = c.newInstance();
                            if (iface.isInstance(o)) {
                                htmlColor = GREEN_HEX;
                            } else {
                                htmlColor = ORANGE_HEX;
                            }
                        } catch (final ClassNotFoundException ex) {
                            LOG.warn("could not load interface  that must be  implemented", ex); // NOI18N
                            htmlColor = BLACK_HEX;
                        } catch (final Exception ex) {
                            LOG.warn("could not instantiate javaclass: " + c.getName(), ex);     // NOI18N
                            htmlColor = BLACK_HEX;
                        }
                    } catch (final ClassNotFoundException ex) {
                        LOG.error("class could not be loaded: " + qualifier, ex);                // NOI18N
                        htmlColor = RED_HEX;
                    } catch (final UnsupportedClassVersionError err) {
                        LOG.error(
                            "class could not be loaded: "
                                    + qualifier
                                    + " -- maybe class was compiled with newer jdk",             // NOI18N
                            err);
                        htmlColor = BLACK_HEX;
                    } catch (final Exception e) {
                        LOG.error("class could not be loaded: " + qualifier, e);                 // NOI18N
                        htmlColor = BLACK_HEX;
                    }
                }
            } else {
                htmlColor = RED_HEX;
            }
            name.insert(0, "<font color=\"#\">");                                                // NOI18N
            name.insert(14, htmlColor);
            name.append("</font>");                                                              // NOI18N
            htmlDisplayName = name.toString();
            fireDisplayNameChange(getDisplayName(), htmlDisplayName);
        }
    }
}
