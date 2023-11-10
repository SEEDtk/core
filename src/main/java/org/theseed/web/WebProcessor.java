package org.theseed.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.basic.BaseProcessor;
import org.theseed.basic.ParseFailureException;
import org.theseed.reports.PageWriter;
import org.theseed.sequence.blast.Source;
import org.theseed.utils.IDescribable;
import org.theseed.web.forms.FormBlastElement;
import org.theseed.web.forms.FormElement;
import org.theseed.web.forms.FormFileElement;
import org.theseed.web.forms.FormIntElement;
import org.theseed.web.forms.FormMapElement;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

import static j2html.TagCreator.*;

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
    /** class ID for preselected cursor focus */
    public static final String FOCUS_CLASS = "_focus";
    /** set to FALSE to allow no-workspace mode */
    protected boolean needsWorkspace;

    // COMMAND-LINE OPTIONS

    /** web page output environment */
    @Option(name = "--env", usage = "output environment")
    private PageWriter.Type outputType;

    /** seed data directory */
    @Argument(index = 0, metaVar = "dataDir", usage = "SEED data directory", required = true)
    private File coreDir;

    /** workspace name */
    @Argument(index = 1, metaVar = "workspace", usage = "workspace name")
    private String workSpace;

    /**
     * Set the default options.
     */
    public final void setDefaults() {
        this.outputType = PageWriter.Type.SEEDTK;
        this.needsWorkspace = true;
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
     * @throws ParseFailureException
     */
    protected final boolean validateParms() throws IOException, ParseFailureException {
        if (this.needsWorkspace) {
            this.workSpaceDir = new File(this.coreDir, "Workspaces/" + this.workSpace);
            if (! this.workSpaceDir.isDirectory())
                throw new FileNotFoundException("Invalid or unauthorized workspace \"" + workSpace + "\" specified.");
            // Insure we have a Temp directory.
            this.workDir = new File(this.workSpaceDir, "Temp");
            if (! this.workDir.isDirectory()) {
                log.info("Creating temporary work directory {}.", this.workDir);
                FileUtils.forceMkdir(this.workDir);
            }
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
     *
     * @throws ParseFailureException
     */
    protected abstract boolean validateWebParms() throws IOException, ParseFailureException;

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
     * @return a local link using this object's page writer
     *
     * @param text		text of the link
     * @param href		URL of the link
     */
    protected ContainerTag localLink(String text, String href) {
        String url = this.pageWriter.local_url(href);
        ContainerTag retVal = a(text).withHref(url);
        return retVal;
    }

    /**
     * @return a local link to the specified command
     *
     * @param text		text of link
     * @param program	program to invoke
     * @param command	command within the program
     * @param parms		additional parameters (key=value)
     */
    public ContainerTag commandLink(String text, String program, String command, String... parms) {
        String url = commandUrl(program, command, parms);
        return a(text).withHref(url);

    }

    /**
     * @return a local URL for the specified command
     *
     * @param program	program to invoke
     * @param command	command within the program
     * @param parms		additional parameters (key=value)
     */
    public String commandUrl(String program, String command, String... parms) {
        String url = "/" + program + ".cgi/" + command + "?workspace=" + this.getWorkSpace();
        if (parms.length > 0)
            url += ";" + StringUtils.join(parms, ';');
        return this.pageWriter.local_url(url);
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
        if (this.needsWorkspace)
            try (CookieFile cookies = new CookieFile(this.workSpaceDir, this.getCookieName())) {
                this.saveForm(cookies);
                this.runWebCommand(cookies);
            }
        else
            this.runWebCommand(null);
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
     * This is a utility structure for extracting useful annotations.
     *
     * @author Bruce Parrello
     */
    private static class FormThing {

        private String name;
        private String label;
        private String fieldName;
        private Annotation formAnnotation;

        /**
         * Constructor to set up for processing form annotations.
         *
         * @param field		field to analyze
         */
        public FormThing(Field field) {
            Annotation[] annotations = field.getAnnotations();
            findOption(field, annotations);
            // Find the form annotation.  This is so very ugly.
            for (Annotation annotation : annotations) {
                if (annotation instanceof FormElement || annotation instanceof FormIntElement ||
                        annotation instanceof FormBlastElement || annotation instanceof FormFileElement ||
                        annotation instanceof FormMapElement
                        )
                    formAnnotation = annotation;
            }
            // Error out if we have a form annotation without a valid option element.
            if (formAnnotation != null && name == null)
                throw new IllegalArgumentException("Field " + fieldName + " needs a double-dashed @Option.");
        }

        /**
         * Default constructor for internal use.
         */
        public FormThing() { };
        /**
         * @return TRUE if the field has the specified option name.
         *
         * @param field		field to analyze
         */
        public static boolean hasOptionName(Field field, String name) {
            FormThing testInstance = new FormThing();
            Annotation[] annotations = field.getAnnotations();
            testInstance.findOption(field, annotations);
            return name.equals(testInstance.getName());
        }

        /**
         * Try to find the Option annotation and extract its data.
         *
         * @param field			field being processed
         * @param annotations	annotations on the field
         */
        private void findOption(Field field, Annotation[] annotations) {
            name = null;
            label = null;
            formAnnotation = null;
            fieldName = field.getName();
            Optional<Annotation> optionElement = Arrays.stream(annotations).filter(x -> x instanceof Option).findFirst();
            if (optionElement.isPresent()) {
                Option option = (Option) optionElement.get();
                if (option.name().startsWith("--"))
                    name = option.name().substring(2);
                label = option.usage();
            }
        }

        /**
         * @return the name from the Option annotation
         */
        protected String getName() {
            return this.name;
        }

        /**
         * @return the usage description from the Option annotation
         */
        protected String getLabel() {
            return this.label;
        }

        /**
         * @return the form annotation
         */
        protected Annotation getFormAnnotation() {
            return formAnnotation;
        }

    }

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
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public HtmlForm buildForm(Class<? extends WebProcessor> processorType, String program, String command)
            throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        HtmlForm retVal = new HtmlForm(program, command, this.workSpace, this.workSpaceDir, this.pageWriter);
        WebProcessor processor = processorType.getDeclaredConstructor().newInstance();
        processor.setDefaults();
        Field[] fields = processorType.getDeclaredFields();
        // We will track bad annotations in here.
        List<String> badFields = new ArrayList<String>();
        List<String> badTypes = new ArrayList<String>();
        for (Field field : fields) {
            Class<?> type = field.getType();
            FormThing formThing = new FormThing(field);
            try {
                Annotation annotation = formThing.getFormAnnotation();
                if (annotation != null) {
                    String name = formThing.getName();
                    String label = formThing.getLabel();
                    // Here we have a real form element.
                    if (annotation instanceof FormElement) {
                        if (type == java.lang.Boolean.TYPE) {
                            retVal.addCheckBoxRow(name, label);
                        } else if (type == java.lang.Double.TYPE) {
                            retVal.addTextRow(name, label, Double.toString(field.getDouble(processor)));
                        } else if (type.isEnum()) {
                            Enum<?> value = (Enum<?>) field.get(processor);
                            IDescribable[] constants = (IDescribable[]) type.getEnumConstants();
                            retVal.addEnumRow(name, label, value, constants);
                        } else if (type == String.class) {
                            retVal.addTextRow(name, label, (String) field.get(processor));
                        } else
                            badTypes.add(field.getName());
                    } else if (annotation instanceof FormIntElement) {
                        FormIntElement formAnnotation = (FormIntElement) annotation;
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
                    } else if (annotation instanceof FormMapElement) {
                        FormMapElement formAnnotation = (FormMapElement) annotation;
                        File mapFile = new File(this.coreDir, formAnnotation.file());
                        retVal.addMapRow(name, label, mapFile);
                    } else if (annotation instanceof FormBlastElement) {
                        FormBlastElement formAnnotation = (FormBlastElement) annotation;
                        if (type == String.class) {
                            String typeId = formAnnotation.id();
                            retVal.addBlastRow(typeId, name, label);
                        } else
                            badTypes.add(field.getName());
                    } else if (annotation instanceof FormFileElement) {
                        FormFileElement formAnnotation = (FormFileElement) annotation;
                        Pattern filePattern = Pattern.compile(formAnnotation.pattern());
                        retVal.addFileRow(name, label, filePattern);
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

    /**
     * Save the form data to the cookie file.
     *
     * @param cookies	cookie file for saving form data
     * @throws IllegalAccessException
     */
    public void saveForm(CookieFile cookies) throws IllegalAccessException {
        Class<? extends WebProcessor> processorType = this.getClass();
        Field[] fields = processorType.getDeclaredFields();
        // We will track the blast elements in here.
        for (Field field : fields) {
            Class<?> type = field.getType();
            FormThing formThing = new FormThing(field);
            Annotation annotation = formThing.getFormAnnotation();
            if (annotation != null) {
                if (annotation instanceof FormBlastElement) {
                    // BLAST fields are tricky, because we have to store two values.
                    String typeOptionName = ((FormBlastElement) annotation).id();
                    // Search for the matching type field.
                    Optional<Field> possibleMatch = Arrays.stream(fields).filter(x -> FormThing.hasOptionName(x, typeOptionName)).findFirst();
                    if (possibleMatch.isPresent()) {
                        // Store the file field.
                        cookies.put(formThing.getName(), (String) field.get(this));
                        // Store the type field.
                        Source dbType = (Source) possibleMatch.get().get(this);
                        cookies.put(typeOptionName, dbType.name());
                    }
                } else {
                    if (type == java.lang.Boolean.TYPE)
                        cookies.put(formThing.getName(), field.getBoolean(this));
                    else if (type == java.lang.Integer.TYPE)
                        cookies.put(formThing.getName(), field.getInt(this));
                    else if (type == java.lang.Double.TYPE)
                        cookies.put(formThing.getName(), field.getDouble(this));
                    else if (type == String.class)
                        cookies.put(formThing.getName(), (String) field.get(this));
                    else if (type.isEnum()) {
                        Enum<?> value = (Enum<?>) field.get(this);
                        cookies.put(formThing.getName(), value.name());
                    }
                }
            }
        }
    }

    /**
     * @return a link to search for the specified function/role in the CoreSEED
     *
     * @param coreFunDesc	function/role to search for
     *
     * @throws UnsupportedEncodingException
     */
    protected DomContent roleSearchLink(String coreFunDesc) throws UnsupportedEncodingException {
        String pattern = URLEncoder.encode(Pattern.quote(coreFunDesc), StandardCharsets.UTF_8.toString());
        String coreFunUrl = this.getPageWriter().local_url("/blast.cgi/search?workspace=" + this.getWorkSpace() + ";regex=" + pattern);
        DomContent roleSearch = a(coreFunDesc).withHref(coreFunUrl).withTarget("_blank");
        return roleSearch;
    }

    /**
     * Throw an error if the current field did not have a proper option attached.
     *
     * @param field		field of interest
     * @param name		name value taken from the field's option annotation, or NULL if no valid one was found
     */
    public void verifyOption(Field field, String name) {
        if (name == null)
            throw new IllegalArgumentException("Field " + field.getName() + " needs a double-dashed @Option.");
    }

}
