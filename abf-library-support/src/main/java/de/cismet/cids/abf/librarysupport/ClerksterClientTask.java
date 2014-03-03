/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import de.cismet.clerkster.client.ClerksterClient;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public class ClerksterClientTask extends Task {

    //~ Instance fields --------------------------------------------------------

    private String url;
    private String username;
    private String password;
    private String infile;
    private String outfile;
    private boolean failonerror;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void execute() throws BuildException {
        super.execute();

        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);

        if ((url == null) || url.isEmpty()) {
            throw new BuildException("url must be set");
        } else if ((username == null) || username.isEmpty()) {
            throw new BuildException("username must be set");
        } else if ((password == null) || password.isEmpty()) {
            throw new BuildException("password must be set");
        } else if ((infile == null) || infile.isEmpty()) {
            throw new BuildException("infile must be set");
        } else if ((outfile == null) || outfile.isEmpty()) {
            throw new BuildException("outfile must be set");
        }

        final int respCode;
        try {
            log("uploading to clerkster service", Project.MSG_INFO);
            respCode = ClerksterClient.uploadAndReceiveJar(username, password, url, null, null, true);
        } catch (final Exception e) {
            throw new BuildException("cannot sign file: " + e.getLocalizedMessage(), e); // NOI18N
        }

        log("clerkster service returned status '" + respCode + "'", Project.MSG_INFO); // NOI18N

        if (failonerror && (respCode != 200)) {
            throw new BuildException("clerkster service communication not successful"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUrl() {
        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUsername() {
        return username;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  username  DOCUMENT ME!
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPassword() {
        return password;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  password  DOCUMENT ME!
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getInfile() {
        return infile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  infile  DOCUMENT ME!
     */
    public void setInfile(final String infile) {
        this.infile = infile;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOutfile() {
        return outfile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  outfile  DOCUMENT ME!
     */
    public void setOutfile(final String outfile) {
        this.outfile = outfile;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFailonerror() {
        return failonerror;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  failonerror  DOCUMENT ME!
     */
    public void setFailonerror(final boolean failonerror) {
        this.failonerror = failonerror;
    }
}
