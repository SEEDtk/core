/**
 *
 */
package org.theseed.reports;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.EmptyTag;

import static j2html.TagCreator.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.theseed.sequence.blast.Source;
import org.theseed.utils.IDescribable;
import org.theseed.web.CookieFile;
import org.theseed.web.WebProcessor;

/**
 * This object is used to design a simple HTML form for a CoreSEED web application.  Such applications have a workspace
 * name as a positional parameter and then one or more command-line options.  The client describes these command-line
 * options and they are used to build the table.  The table is then returned inside a form.
 *
 * @author Bruce Parrello
 *
 */
public class HtmlForm {

    // FIELDS
    /** form container */
    private ContainerTag form;
    /** table of input rows */
    private HtmlTable<Key.Null> inputTable;
    /** list of files in the workspace directory */
    private List<String> workFiles;
    /** TRUE if files were used, else FALSE */
    private boolean usedFiles;
    /** page writer for this form */
    private PageWriter writer;
    /** cookie file with saved form state */
    private CookieFile savedForm;
    /** pattern for BLAST-related files */
    public static final String BLAST_FILE_REGEX = ".+\\.(?:gto|fasta|fa|faa|fna)";
    public static final Pattern BLAST_FILE_PATTERN = Pattern.compile(BLAST_FILE_REGEX);
    /** pattern for text files */
    public static final String TEXT_FILE_REGEX = ".+\\.(?:tbl|txt|tsv)";
    public static final Pattern TEXT_FILE_PATTERN = Pattern.compile(TEXT_FILE_REGEX);
    /** pattern for genome files */
    public static final String GTO_FILE_REGEX = ".+\\.gto";
    public static final Pattern GTO_FILE_PATTERN = Pattern.compile(GTO_FILE_REGEX);
    /** pattern for FASTA files */
    public static final String FASTA_FILE_REGEX = ".+\\.(?:fasta|fa|faa|fna)";
    public static final Pattern FASTA_FILE_PATTERN = Pattern.compile(FASTA_FILE_REGEX);
    /** pattern for read files */
    public static final String READ_FILE_REGEX = ".+\\.(?:fq|fastq)";
    public static final Pattern READ_FILE_PATTERN = Pattern.compile(READ_FILE_REGEX);
    /** maximum number of files to display in a datalist */
    private static final int MAX_FILES = 40;
    /** filter for workspace to ignore internal files */
    private static final FilenameFilter WORK_FILE_FILTER = new WorkFiles();


    /**
     * Construct a new HTML form.
     *
     * @param program	program identifier (the program name will be "web." + identifier + ".jar")
     * @param command	the program command to use
     * @param workspace	name of the workspace
     * @param wsDir		relevant workspace directory
     * @param writer	page writer for URL decoration
     *
     * @throws IOException
     */
    public HtmlForm(String program, String command, String workspace, File wsDir, PageWriter writer) throws IOException {
        this.init(program, command, workspace, wsDir, writer);
    }

    /**
     * Initialize this form.
     *
     * @param program	program identifier (the program name will be "web." + identifier + ".jar")
     * @param command	the program command to use
     * @param workspace	name of the workspace
     * @param wsDir		relevant workspace directory
     * @param writer	page writer for URL decoration
     *
     * @throws IOException
     */
    private void init(String program, String command, String workspace, File wsDir, PageWriter writer) throws IOException {
        this.form = form().withMethod("POST")
                .withAction(writer.local_url("/" + program + ".cgi/" + command))
                .withClass("web");
        this.inputTable = new HtmlTable<Key.Null>(new ColSpec.Normal("Parameter"), new ColSpec.Normal("Value"));
        // Add a hidden input for the workspace parameter.  The keyword for this parameter is the command name.
        this.form.with(input().withType("hidden").withName("workspace").withValue(workspace));
        // Save the page writer.
        this.writer = writer;
        // Get the cookie file.
        String cookieName = formCookieName(program, command);
        this.savedForm = new CookieFile(wsDir, cookieName);
        // Now load the file list.  We ignore directories and "_" files.
        File[] workDirFiles = wsDir.listFiles(WORK_FILE_FILTER);
        this.workFiles = Arrays.stream(workDirFiles).filter(x -> x.isFile() && x.canRead()).map(x -> x.getName()).collect(Collectors.toList());
        // Denote no files were used.
        this.usedFiles = false;
    }

    /**
     * @return the cookie file for an operation's form.
     *
     * @param wsDir		workspace directory
     * @param program	program name
     * @param command	command name
     *
     */
    public static String formCookieName(String program, String command)  {
        return "form." + program + "." + command;
    }

    /** This is the filename filter used to get all the workspace files. */
    private static class WorkFiles implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.charAt(0) != '_';
        }

    }

    /**
     * Construct a new HTML form.  This is a shortcut when a WebProcessor is available.
     *
     * @throws IOException
     */
    public HtmlForm(String program, String command, WebProcessor processor) throws IOException {
        this.init(program, command, processor.getWorkSpace(), processor.getWorkSpaceDir(), processor.getPageWriter());
    }

    /**
     * Add an enumeration input.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param init			default enum value
     * @param values		array of enum values
     */
    public void addEnumRow(String name, String description, Enum<?> init, IDescribable[] values) {
        ContainerTag dropDown = buildEnumBox(name, init, values);
        // Build the table row.
        this.newRow(description, dropDown);
    }

    /**
     * @return a dropdown box for selecting an enum value.
     *
     * @param name		parameter name
     * @param init		default enum value
     * @param values	array of enum values
     */
    private ContainerTag buildEnumBox(String name, Enum<?> init, IDescribable[] values) {
        // Get the saved value of the control.  We use a string here because of the vague
        // type of the enum.
        String initVal = this.savedForm.get(name, init.name());
        // Create the select tag.
        ContainerTag retVal = select().withName(name);
        for (IDescribable enumVal : values) {
            String optVal = enumVal.name();
            ContainerTag option = option(enumVal.getDescription()).withValue(enumVal.name());
            if (optVal.contentEquals(initVal))
                option.attr("selected");
            retVal.with(option);
        }
        return retVal;
    }

    /**
     * Add a file input.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param namePattern	a pattern the file name must match
     */
    public void addFileRow(String name, String description, Pattern namePattern) {
        DomContent fileBox = this.buildFileBox(name, namePattern);
        newRow(description, fileBox);
    }

    /**
     * Add a new row to the table.
     *
     * @param description		description string for the parameter
     * @param inputObject		content to put in the input cell
     */
    private void newRow(String description, DomContent inputObject) {
        this.inputTable.new Row(Key.NONE).add(description).add(inputObject);
    }

    /**
     * @return a file name-selection box
     *
     * The file-name selection box is fairly complex.  The user can select either local or workspace files,
     * and javascript is used to toggle between the two.
     *
     * @param name			parameter name for the box
     * @param namePattern	a pattern the file name must match
     */
    private DomContent buildFileBox(String name, Pattern namePattern) {
        // We will use a data list.  Only the first N matching file names will be kept.
        String listName = buildDataList(namePattern);
        DomContent retVal = buildFileComplex(name, listName);
        return retVal;
    }

    /**
     * Build a configurable file box that allows specifying both local and global files.
     *
     * @param name		name of the file control
     * @param listName	name of the populating data list
     *
     * @return a series of controls that allows choosing a local or workspace file
     */
    private DomContent buildFileComplex(String name, String listName) {
        // We need the local checkbox, the local file control, and the workspace input box.
        String nameLocal = name + "_local";
        String nameWork = name + "_work";
        String event = "configureFiles(this, '" + nameLocal + "', '" + nameWork + "');";
        EmptyTag checkBox = input().withType("checkbox").attr("onChange", event).withId(name).withClass("fileChecker");
        EmptyTag localBox = input().withType("file").withId(nameLocal).withStyle("display: none;").withClass("file");
        EmptyTag fileBox = input().withType("text").withName(name).withId(nameWork).attr("list", listName).withClass("file")
                .withStyle("display: inline-block;");
        // If there is a saved value for this control, put it in the file box.
        String savedFile = this.savedForm.get(name, "");
        if (! savedFile.isEmpty())
            fileBox.withValue(savedFile);
        // Now create the full control.
        DomContent retVal = join(checkBox, "Local", localBox, fileBox);
        // Insure we add the multipart encoding to the form.
        this.usedFiles = true;
        return retVal;
    }

    /**
     * Build a data list for a file box.
     *
     * @param namePattern	a pattern the file name must match
     * @param listName		the name to give to the list
     *
     * @return the name of the data list.
     */
    private String buildDataList(Pattern namePattern) {
        String retVal = this.writer.checkDataList(namePattern.pattern());
        if (retVal == null) {
            // Here we have to create and name the data list.
            retVal = this.writer.getListID();
            ContainerTag dataList = datalist().withId(retVal);
            int used = 0;
            for (int i = 0; i < this.workFiles.size() && used < MAX_FILES; i++) {
                String fileName = this.workFiles.get(i);
                Matcher m = namePattern.matcher(fileName);
                if (m.matches()) {
                    dataList.with(option(fileName).withValue(fileName));
                    used++;
                }
            }
            // Store the list name in the map.
            this.writer.putDataList(namePattern.pattern(), retVal);
            // Add the data list to the top of the form.
            this.form.with(dataList);
        }
        return retVal;
    }

    /**
     * Add a text input.  Do not use NULL for the default here.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param init			default value
     */
    public void addTextRow(String name, String description, String init) {
        EmptyTag inputBox = input().withType("text").withName(name);
        String defaultVal = this.savedForm.get(name, init);
        if (! defaultVal.isEmpty())
            inputBox.withValue(defaultVal);
        this.newRow(description, inputBox);
    }

    /**
     * Add a checkbox input.  The default is always OFF.
     *
     * @param name			parameter name
     * @param description	parameter description
     */
    public void addCheckBoxRow(String name, String description) {
        boolean defaultVal = this.savedForm.get(name, false);
        EmptyTag checkBox = input().withType("checkbox").withName(name);
        if (defaultVal) checkBox.attr("checked");
        this.newRow(description, checkBox);
    }

    /**
     * Add a number input.  This only works for integer numbers.  Floats have to be input as text.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param init			default value
     * @param min			minimum value
     * @param max			maximum value
     */
    public void addIntRow(String name, String description, int init, int min, int max) {
        String defaultVal = this.savedForm.get(name, Integer.toString(init));
        EmptyTag inputBox = input().withType("number").withName(name).withValue(defaultVal)
                .attr("min", Integer.toString(min)).attr("max", Integer.toString(max));
        this.newRow(description, inputBox);
    }

    /**
     * Add a BLAST source input.  This is two boxes, a dropdown and a file box.
     *
     * @param type			source type option name
     * @param file			file name option name
     * @param description	parameter description
     */
    public void addBlastRow(String type, String file, String description) {
        DomContent boxes = join(this.buildEnumBox(type, Source.dna, Source.values()),
                this.buildFileBox(file, BLAST_FILE_PATTERN));
        this.newRow(description, boxes);
    }

    /**
     * @return the completed form
     *
     * NOTE that after this the cookie file is closed and the form cannot be modified.
     *
     * @throws IOException
     */
    public ContainerTag output() throws IOException {
        if (this.usedFiles)
            this.form.attr("enctype", "multipart/form-data");
        this.form.with(this.inputTable.output())
            .with(p().with(input().withType("submit").withClass("submit")));
        this.savedForm.close();
        return this.form;
    }


}
