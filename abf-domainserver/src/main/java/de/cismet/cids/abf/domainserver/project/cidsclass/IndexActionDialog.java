/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.MessageFormat;

import javax.swing.Timer;

import de.cismet.cids.util.Cancelable;
import de.cismet.cids.util.ProgressListener;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class IndexActionDialog extends javax.swing.JDialog implements ProgressListener {

    //~ Instance fields --------------------------------------------------------

    private final transient ActionListener aL;
    private final transient Cancelable cancelable;
    private final transient String statusString;
    private final transient Timer timer;
    private transient ProgressState currentState;
    private transient int currentStep;
    private transient int currentClass;
    private transient long startTime;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton cmdCancel = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdOK = new javax.swing.JButton();
    private javax.swing.JLabel lblError;
    private final transient javax.swing.JLabel lblOverallStatus = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblStatus = new javax.swing.JLabel();
    private final transient javax.swing.JPanel pnlProgress = new javax.swing.JPanel();
    private final transient javax.swing.JProgressBar prgClass = new javax.swing.JProgressBar();
    private final transient javax.swing.JProgressBar prgOverall = new javax.swing.JProgressBar();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IndexActionDialog object.
     *
     * @param  parent       DOCUMENT ME!
     * @param  modal        DOCUMENT ME!
     * @param  noOfClasses  DOCUMENT ME!
     * @param  cancelable   DOCUMENT ME!
     */
    public IndexActionDialog(final Frame parent,
            final boolean modal,
            final int noOfClasses,
            final Cancelable cancelable) {
        super(parent, modal);
        this.cancelable = cancelable;
        initComponents();
        setLocationRelativeTo(parent);
        prgClass.setStringPainted(true);
        prgOverall.setMinimum(0);
        prgOverall.setMaximum(noOfClasses);
        prgOverall.setValue(0);
        prgOverall.setIndeterminate(false);
        prgOverall.setStringPainted(true);
        currentStep = 0;
        currentClass = 0;
        currentState = new ProgressState("", 0);                                                                          // NOI18N
        final String noOfClassesString = org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                (noOfClasses == 1)
                    ? "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).noOfClassesString.oneClass"
                    : "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).noOfClassesString.multClasses"); // NOI18N
        statusString = org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).statusString",                         // NOI18N
                new Object[] {
                    "{0}",                                                                                                // NOI18N
                    noOfClasses,
                    noOfClassesString,
                    "{1}"                                                                                                 // NOI18N
                });
        lblOverallStatus.setText(
            MessageFormat.format(statusString, 0, "0s"));                                                                 // NOI18N
        aL = new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (currentClass == prgOverall.getMaximum()) {
                                    timer.stop();
                                    cmdCancel.setEnabled(false);
                                    cmdOK.setEnabled(true);
                                    prgClass.setIndeterminate(false);
                                    prgClass.setMinimum(0);
                                    prgClass.setMaximum(1);
                                    prgClass.setValue(1);
                                }
                                final long now = System.currentTimeMillis();
                                final long seconds = ((now - startTime) / 1000) % 60;
                                final long minutes = ((now - startTime) / 60000) % 60;
                                final long hours = (now - startTime) / 3600000;
                                final StringBuffer text = new StringBuffer(""); // NOI18N
                                if (hours > 0) {
                                    text.append(hours).append("h ");            // NOI18N
                                }
                                if (minutes > 0) {
                                    text.append(minutes).append("m ");          // NOI18N
                                }
                                text.append(seconds).append('s');               // NOI18N
                                lblOverallStatus.setText(MessageFormat.format(
                                        statusString,
                                        currentClass,
                                        text));
                                prgOverall.setValue(currentClass);
                            }
                        });
                }
            };
        timer = new Timer(1000, aL);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void progress(final int steps) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    currentStep += steps;
                    prgClass.setValue(currentStep);
                }
            });
    }

    @Override
    public void processingStateChanged(final ProgressState state) {
        if (!timer.isRunning()) {
            startTime = System.currentTimeMillis();
            timer.start();
        }
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    lblStatus.setText(state.getState());
                    if (state.isIndeterminate()) {
                        prgClass.setIndeterminate(true);
                    } else {
                        prgClass.setMinimum(0);
                        prgClass.setMaximum(state.getMaxSteps());
                        if (currentState.isIndeterminate()) {
                            prgClass.setIndeterminate(false);
                            prgClass.setValue(0);
                            currentStep = 0;
                        }
                    }
                    currentState = state;
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    void nextClass() {
        currentClass++;
        aL.actionPerformed(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  error  DOCUMENT ME!
     * @param  e      DOCUMENT ME!
     */
    void setError(final String error, final Throwable e) {
        lblError.setText(error);
        cmdCancelActionPerformed(null);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(IndexActionDialog.class, "IndexActionDialog.title")); // NOI18N

        cmdOK.setText(org.openide.util.NbBundle.getMessage(IndexActionDialog.class, "IndexActionDialog.cmdOK.text")); // NOI18N
        cmdOK.setEnabled(false);
        cmdOK.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOKActionPerformed(evt);
                }
            });

        cmdCancel.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.cmdCancel.text")); // NOI18N
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdCancelActionPerformed(evt);
                }
            });

        lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblStatus.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblStatus.text")); // NOI18N

        lblOverallStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblOverallStatus.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblOverallStatus.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout pnlProgressLayout = new org.jdesktop.layout.GroupLayout(pnlProgress);
        pnlProgress.setLayout(pnlProgressLayout);
        pnlProgressLayout.setHorizontalGroup(
            pnlProgressLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                pnlProgressLayout.createSequentialGroup().addContainerGap().add(
                    pnlProgressLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        lblStatus,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        508,
                        Short.MAX_VALUE).add(
                        prgClass,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        508,
                        Short.MAX_VALUE).add(
                        lblOverallStatus,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        508,
                        Short.MAX_VALUE).add(
                        prgOverall,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        508,
                        Short.MAX_VALUE)).addContainerGap()));
        pnlProgressLayout.setVerticalGroup(
            pnlProgressLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                pnlProgressLayout.createSequentialGroup().add(15, 15, 15).add(lblStatus).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    prgClass,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(32, 32, 32).add(lblOverallStatus)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    prgOverall,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)));

        lblError.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblError.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        layout.createSequentialGroup().add(24, 24, 24).add(lblError)).add(
                        pnlProgress,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        layout.createSequentialGroup().add(cmdCancel).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(cmdOK))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                layout.createSequentialGroup().addContainerGap().add(
                    pnlProgress,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(lblError).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED,
                    70,
                    Short.MAX_VALUE).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(cmdCancel).add(cmdOK))
                            .addContainerGap()));

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_cmdCancelActionPerformed
    {                                                                           //GEN-HEADEREND:event_cmdCancelActionPerformed
        cancelable.cancel();
        cmdCancel.setEnabled(false);
        cmdOK.setEnabled(true);
        timer.stop();
    }                                                                           //GEN-LAST:event_cmdCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOKActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_cmdOKActionPerformed
    {                                                                       //GEN-HEADEREND:event_cmdOKActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setVisible(false);
                    dispose();
                }
            });
    } //GEN-LAST:event_cmdOKActionPerformed
}
