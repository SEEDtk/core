package org.theseed.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.reports.PageWriter;
import org.theseed.utils.BaseProcessor;

/**
 * This is the base class for web page service commands.  The first two positional parameters are the coreSEED directory
 * and the workspace ID.  An environment option specifies the page format.
 *
 * @author Bruce Parrello
 *
 */
public abstract class WebProcessor extends BaseProcessor {

    // FIELDS
    /** logging facility */
    protected static Logger log = LoggerFactory.getLogger(WebProcessor.class);
    /** page writer */
    private PageWriter pageWriter;
    /** workspace directory */
    private File workSpaceDir;
    /** working directory for temp files */
    private File workDir;

    // COMMAND-LINE OPTIONS

    /** web page output environment */
    @Option(name = "--env", usage = "output environment")
    private PageWriter.Type outputType;

    /** seed data directory */
    @Argument(index = 0, metaVar = "/vol/core-seed/FIGdisk/FIG/Data", usage = "SEED data directory")
    private File coreDir;

    /** workspace name */
    @Argument(index = 1, metaVar = "parrello", usage = "workspace name")
    private String workSpace;

    /**
     * Set the default options.
     */
    protected void setWebDefaults() {
        this.outputType = PageWriter.Type.SEEDTK;
    }

    /**
     * Set up the workspace and the page writer.
     *
     * @throws IOException
     */
    protected void validateWebParms() throws IOException {
        this.workSpaceDir = new File(this.coreDir, "Workspaces/" + this.workSpace);
        if (! this.workSpaceDir.isDirectory())
            throw new FileNotFoundException("Invalid or unauthorized workspace \"" + workSpace + "\" specified.");
        // Insure we have a Temp directory.
        this.workDir = new File(this.workSpaceDir, "Temp");
        if (! this.workDir.isDirectory()) {
            log.info("Creating temporary work directory {}.", this.workDir);
            FileUtils.forceMkdir(this.workDir);
        }
        // Create the page writer.
        this.pageWriter = this.outputType.create();
    }

    /**
     * @return the output page writer
     */
    protected PageWriter getPageWriter() {
        return this.pageWriter;
    }

    /*
     * @return the workspace directory
     */
    protected File getWorkSpaceDir() {
        return this.workSpaceDir;
    }

    /**
     * @return the temporary work directory
     */
    protected File getWorkDir() {
        return this.workDir;
    }

    /**
     * @return the coreSEED data directory
     */
    protected File getCoreDir() {
        return this.coreDir;
    }

    /**
     * @return the workspace name
     */
    protected String getWorkSpace() {
        return this.workSpace;
    }

    /**
     * @return the named file in the workspace
     *
     * @param name		name of desired file
     *
     * @throws IOException
     */
    protected File computeWorkFile(String name) throws IOException {
        if (name.contains(".."))
            throw new FileNotFoundException("Invalid workspace file name \"" + name + "\".");
        File retVal = new File(this.workSpaceDir, name);
        return retVal;

    }

}
