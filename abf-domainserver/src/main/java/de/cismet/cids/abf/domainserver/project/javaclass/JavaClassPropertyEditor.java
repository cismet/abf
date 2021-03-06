/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.javaclass;

import org.apache.log4j.Logger;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.util.ImageUtilities;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.cidsclass.JavaClass;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class JavaClassPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor,
    InplaceEditor.Factory {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            JavaClassPropertyEditor.class);

    private static final String NO_JAVA_CLASS;

    static {
        NO_JAVA_CLASS = org.openide.util.NbBundle.getMessage(
                JavaClassPropertyEditor.class,
                "JavaClassPropertyEditor.NO_JAVA_CLASS"); // NOI18N
    }

    //~ Instance fields --------------------------------------------------------

    public final transient Image classImage;

    /** Creates a new instance of JavaClassPropertyEditor. */
    private final transient DomainserverProject project;
    private final transient JLabel renderer;
    private final transient String[] classTypes;

    // not final for memory savings, cause I think nb platform initializes all
    // propertyeditors on startup
    private transient InplaceEditor ed;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JavaClassPropertyEditor object.
     *
     * @param  project  DOCUMENT ME!
     * @param  type     DOCUMENT ME!
     */
    public JavaClassPropertyEditor(final DomainserverProject project, final JavaClass.Type... type) {
        this.project = project;
        if (type == null) {
            classTypes = null;
        } else {
            classTypes = new String[type.length];
            for (int i = 0; i < type.length; ++i) {
                classTypes[i] = type[i].getType();
            }
        }
        renderer = new JLabel();
        renderer.setText(NO_JAVA_CLASS);
        renderer.setIcon(null);
        classImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "javaclass.png"); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   javaClass  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getClassNameForJavaClass(final JavaClass javaClass) {
        if (javaClass != null) {
            final String qualifier = javaClass.getQualifier();
            final String name;
            final int indexOfDot = qualifier.lastIndexOf('.'); // NOI18N
            if (indexOfDot == -1) {
                name = qualifier;
            } else {
                name = qualifier.substring(indexOfDot + 1, qualifier.length());
            }
            return name;
        }
        return null;
    }

    @Override
    public void attachEnv(final PropertyEnv propertyEnv) {
        propertyEnv.registerInplaceEditorFactory(this);
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (ed == null) {
            ed = new Inplace();
        }
        return ed;
    }

    @Override
    public void setValue(final Object value) {
        if (value instanceof JavaClass) {
            renderer.setIcon(new ImageIcon(classImage));
            renderer.setText(getClassNameForJavaClass((JavaClass)value));
        } else {
            renderer.setText(NO_JAVA_CLASS);
            renderer.setIcon(null);
        }
        super.setValue(value);
    }

    /**
     * Paint a representation of the value into a given area of screen real estate. Note that the propertyEditor is
     * responsible for doing its own clipping so that it fits into the given rectangle.
     *
     * <p>If the PropertyEditor doesn't honor paint requests (see isPaintable) this method should be a silent noop.</p>
     *
     * @param  gfx  Graphics object to paint into.
     * @param  box  Rectangle within graphics object into which we should paint.
     */
    @Override
    public void paintValue(final Graphics gfx, final Rectangle box) {
        renderer.setSize(box.width, box.height);
        renderer.paint(gfx);
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * maybe use static class.
     *
     * @version  $Revision$, $Date$
     */
    private final class Inplace implements InplaceEditor, ActionListener {

        //~ Instance fields ----------------------------------------------------

        private final transient JComboBox cboPicker;
        private final transient Set<ActionListener> listeners;
        private final transient DefaultListCellRenderer defaultRenderer;
        private final transient List<JavaClass> javaClasses;
        private transient PropertyEditor editor;
        private transient PropertyModel model;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Inplace object.
         */
        public Inplace() {
            listeners = new HashSet<ActionListener>();
            defaultRenderer = new DefaultListCellRenderer();
            javaClasses = new ArrayList<JavaClass>();
            cboPicker = new JComboBox();
            cboPicker.addActionListener(this);
            cboPicker.setRenderer(new ListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(final JList list,
                            final Object value,
                            final int index,
                            final boolean isSelected,
                            final boolean cellHasFocus) {
                        final JavaClass c = (JavaClass)value;
                        final JLabel l = (JLabel)defaultRenderer.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus);
                        if (c == null) {
                            l.setText(
                                org.openide.util.NbBundle.getMessage(
                                    JavaClassPropertyEditor.class,
                                    "JavaClassPropertyEditor.Inplace.cellRendererComponent.label.default")); // NOI18N
                            l.setIcon(null);
                        } else {
                            l.setText(getClassNameForJavaClass(c));
                            l.setIcon(new ImageIcon(classImage));
                        }
                        return l;
                    }
                });
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void connect(final PropertyEditor propertyEditor, final PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return cboPicker;
        }

        @Override
        public void clear() {
            // avoid memory leaks:
            editor = null;
            model = null;
        }

        @Override
        public Object getValue() {
            return cboPicker.getSelectedItem();
        }

        @Override
        public void setValue(final Object object) {
            if ((object instanceof JavaClass)
                        && javaClasses.contains((JavaClass)object)) {
                cboPicker.setSelectedItem(object);
            }
        }

        @Override
        public boolean supportsTextEntry() {
            return true;
        }

        @Override
        public void reset() {
            try {
                final JavaClass value = ((JavaClass)editor.getValue());
                final List<JavaClass> classes = project.getCidsDataObjectBackend().getAllEntities(JavaClass.class);
                javaClasses.clear();
                javaClasses.add(null);
                for (final JavaClass jc : classes) {
                    if (classTypes == null) {
                        javaClasses.add(jc);
                    } else {
                        for (final String classType : classTypes) {
                            if (jc.getType().equalsIgnoreCase(classType)) {
                                javaClasses.add(jc);
                                break;
                            }
                        }
                    }
                }
                cboPicker.setModel(new DefaultComboBoxModel(
                        javaClasses.toArray()));
                cboPicker.setSelectedItem(value);
            } catch (final Exception ex) {
                LOG.error("could not reset inplace editor", ex); // NOI18N
            }
        }

        @Override
        public KeyStroke[] getKeyStrokes() {
            return new KeyStroke[0];
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        @Override
        public PropertyModel getPropertyModel() {
            return model;
        }

        @Override
        public void setPropertyModel(final PropertyModel propertyModel) {
            this.model = propertyModel;
        }

        @Override
        public boolean isKnownComponent(final Component component) {
            return component.equals(cboPicker)
                        || cboPicker.isAncestorOf(component);
        }

        @Override
        public void addActionListener(final ActionListener actionListener) {
            synchronized (listeners) {
                listeners.add(actionListener);
            }
        }

        @Override
        public void removeActionListener(final ActionListener actionListener) {
            synchronized (listeners) {
                listeners.remove(actionListener);
            }
        }

        /**
         * Invoked when an action occurs.
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            final Iterator<ActionListener> it;
            synchronized (listeners) {
                it = new HashSet<ActionListener>(listeners).iterator();
            }
            final ActionEvent ae = new ActionEvent(
                    this,
                    0,
                    InplaceEditor.COMMAND_SUCCESS);
            while (it.hasNext()) {
                it.next().actionPerformed(ae);
            }
        }
    }
}
