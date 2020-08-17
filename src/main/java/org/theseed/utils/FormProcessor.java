/**
 *
 */
package org.theseed.utils;

import java.io.IOException;

import org.theseed.reports.HtmlForm;

import j2html.tags.DomContent;
import static j2html.TagCreator.*;

/**
 * This is a special web processor that generates a form page.
 *
 * @author Bruce Parrello
 *
 */
public abstract class FormProcessor extends WebProcessor {

    // FIELDS
    /** HTML form creator */
    private HtmlForm pageForm;

    @Override
    protected void setDefaults() {
        this.setWebDefaults();
    }

    @Override
    protected boolean validateParms() throws IOException {
        this.validateWebParms();
        return true;
    }

    @Override
    protected void runCommand() throws Exception {
        log.info("Workspace directory is {}.", this.getWorkSpaceDir());
        // Get the key definition strings.
        String pageTitle = this.getTitle();
        String program = this.getProgram();
        String command = this.getCommand();
        // Create the HTML form.
        this.pageForm = new HtmlForm(program, command, this.getWorkSpace(), this.getWorkSpaceDir());
        // Ask the user for the table rows.
        this.buildForm(this.pageForm);
        // Format the web page.
        DomContent formBlock = this.getPageWriter().highlightBlock(this.pageForm.output());
        this.getPageWriter().writePage(pageTitle, h1(pageTitle).withClass("form"), formBlock);
    }

    /**
     * @return the command name for the application run by this form
     */
    protected abstract String getCommand();

    /**
     * @return the program name for the application run by this form
     */
    protected abstract String getProgram();

    /**
     * @return the title of this page
     */
    protected abstract String getTitle();

    /**
     * Add the parameter rows to this form.
     *
     * @param form	form object to which rows can be added
     */
    protected abstract void buildForm(HtmlForm form);

}
