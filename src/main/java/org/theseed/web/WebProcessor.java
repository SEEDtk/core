package org.theseed.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.reports.HtmlForm;
import org.theseed.reports.PageWriter;
import org.theseed.utils.BaseProcessor;
import org.theseed.utils.IDescribable;
import org.theseed.web.forms.FormElement;
import org.theseed.web.forms.FormIntElement;

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
    @Argument(index = 0, metaVar = "dataDir", usage = "SEED data directory")
    private File coreDir;

    /** workspace name */
    @Argument(index = 1, metaVar = "workspace", usage = "workspace name")
    private String workSpace;

    // TODO put back buttons on result pages

    /**
     * Set the default options.
     */
    public final void setDefaults() {
        this.outputType = PageWriter.Type.SEEDTK;
        setWebDefaults();
    }

    /**
     * Set the subclass default options.
     */
    protected abstract void setWebDefaults();

    /**
     * Set up the workspace and the page writer.
     *
     * @throws IOException
     */
    protected final boolean validateParms() throws IOException {
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
        // Call the client.
        return this.validateWebParms();
    }

    /**
     * Validate the client parameters.
     *
     * @return TRUE if it is safe to proceed
     */
    protected abstract boolean validateWebParms() throws IOException;

    /**
     * @return the cookie file name for this command
     */
    protected abstract String getCookieName();

    /**
     * @return the output page writer
     */
    public PageWriter getPageWriter() {
        return this.pageWriter;
    }

    /*
     * @return the workspace directory
     */
    public File getWorkSpaceDir() {
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
    public String getWorkSpace() {
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

    /**
     * Run the command.
     *
     * @throws Exception
     */
    protected final void runCommand() throws Exception {
        try (CookieFile cookies = new CookieFile(this.workSpaceDir, this.getCookieName())) {
            this.runWebCommand(cookies);
        }
    }

    /**
     * Execute this command.
     *
     * @param cookies	cookie file containing persistent data
     *
     * @throws Exception
     */
    protected abstract void runWebCommand(CookieFile cookies) throws Exception;

    /**
     * Build a form for this command using the Form* annotations.  This command is called from
     * the form's webprocessor to build a form for the command processor.
     *
     * @param processorType		type of command processor for which we want to build a form
     * @param program			program name for the form
     * @param command			command for the form
     *
     * @return a form designed for the command
     *
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public HtmlForm buildForm(Class<? extends WebProcessor> processorType, String program, String command)
            throws IOException, IllegalAccessException, InstantiationException {
        HtmlForm retVal = new HtmlForm(program, command, this.workSpace, this.workSpaceDir, this.pageWriter);
        WebProcessor processor = processorType.newInstance();
        processor.setDefaults();
        Field[] fields = processorType.getDeclaredFields();
        // We will track bad annotations in here.
        List<String> badFields = new ArrayList<String>();
        List<String> badTypes = new ArrayList<String>();
        for (Field field : fields) {
            Class<?> type = field.getType();
            Annotation[] annotations = field.getAnnotations();
            try {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof FormElement) {
                        FormElement formAnnotation = (FormElement) annotation;
                        String name = formAnnotation.name();
                        String label = formAnnotation.label();
                        if (type == java.lang.Boolean.TYPE) {
                            retVal.addCheckBoxRow(name, label);
                        } else if (type == java.lang.Double.TYPE) {
                            retVal.addTextRow(name, label, Double.toString(field.getDouble(processor)));
                        } else if (type.isEnum()) {
                            Enum<?> value = (Enum<?>) field.get(processor);
                            IDescribable[] constants = (IDescribable[]) type.getEnumConstants();
                            retVal.addEnumRow(name, label, value, constants);
                        } else
                            badTypes.add(field.getName());
                    } else if (annotation instanceof FormIntElement) {
                        FormIntElement formAnnotation = (FormIntElement) annotation;
                        String name = formAnnotation.name();
                        String label = formAnnotation.label();
                        int min = formAnnotation.min();
                        int max = formAnnotation.max();
                        if (type == java.lang.Integer.TYPE) {
                            int init = field.getInt(processor);
                            retVal.addIntRow(name, label, init, min, max);
                        } else if (type == java.lang.Double.TYPE) {
                            int init = (int) field.getDouble(processor);
                            retVal.addIntRow(name, label, init, min, max);
                        } else
                            badTypes.add(field.getName());
                    }
                }
            } catch (IllegalAccessException e) {
                // Remember invalid fields.
                badFields.add(field.getName());
            }
        }
        if (badFields.size() + badTypes.size() > 0) {
            String message = "Form element errors.";
            if (! badTypes.isEmpty())
                message += "  Invalid field types in " + badTypes.stream().collect(Collectors.joining(", ")) + ".";
            if (! badFields.isEmpty())
                message += "  Some fields are private instead of protected:  " +
                    badFields.parallelStream().collect(Collectors.joining(", ")) + ".";
            throw new RuntimeException(message);
        }
        return retVal;

    }

}
