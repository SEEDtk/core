/**
 *
 */
package org.theseed.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.EmptyTag;

import static j2html.TagCreator.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.theseed.io.TabbedLineReader;
import org.theseed.reports.PageWriter;
import org.theseed.sequence.blast.Source;
import org.theseed.utils.IDescribable;

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
        // Add a hidden input for the workspace parameter.
        addHidden("workspace", workspace);
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
     * Add a hidden parameter to the form.
     *
     * @param name		parameter name
     * @param value		parameter value
     */
    public void addHidden(String name, String value) {
        this.form.with(input().withType("hidden").withName(name).withValue(value));
    }

    /**
     * Specify the target window for this form's result.
     *
     * @param target	new proposed target
     */
    public void setTarget(String target) {
        this.form.attr("target", target);
    }

    /**
     * Specify an ID for this form.
     *
     * @param id	proposed form ID
     */
    public void setId(String id) {
        this.form.withId(id);
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
     * Create a row with a dropdown box initialized from a tab-delimited file.  The "description" column of the
     * file contains the description to be displayed in the box, and the "value" column contains the value to
     * return.  The first data record of the file contains the default value.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param mapFile		file containing the choices
     */
    public void addMapRow(String name, String description, File mapFile) {
        ContainerTag dropdown = this.buildMapBox(name, mapFile);
        this.newRow(description, dropdown);
    }

    /**
     * @return a dropdown box built from a file.
     *
     * @param name		parameter name
     * @param mapFile	mapping file, tab-delimited, with the description in column "description" and the value in column "value"
     */
    private ContainerTag buildMapBox(String name, File mapFile) {
        // Read the map from the file.
        List<Map.Entry<String, String>> mapList = new ArrayList<>();
        try (TabbedLineReader mapStream = new TabbedLineReader(mapFile)) {
            int descIdx = mapStream.findField("description");
            int valIdx = mapStream.findField("value");
            for (TabbedLineReader.Line line : mapStream)
                mapList.add(new AbstractMap.SimpleEntry<String, String>(line.get(valIdx), line.get(descIdx)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (mapList.size() < 1)
            throw new IllegalArgumentException("File " + mapFile + " has no data.");
        // Get the saved value of the control.
        String initVal = this.savedForm.get(name, mapList.get(0).getKey());
        // Create the select tag.
        ContainerTag retVal = select().withName(name);
        for (Map.Entry<String, String> pair : mapList) {
            String value = pair.getKey();
            ContainerTag option = option(pair.getValue()).withValue(value);
            if (value.contentEquals(initVal))
                option.attr("selected");
            retVal.with(option);
        }
        return retVal;
    }

    /**
     * Add a choice box with a numeric return.  The return value will be the list index of the chosen
     * value.  If the list is empty, the control is a hidden that always returns 0.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param defaultValue	default string value to pre-select
     * @param values		list of string values
     */
    public void addChoiceIndexedRow(String name, String description, String defaultValue, List<String> values) {
        DomContent retVal;
        if (values.size() <= 1) {
            // Here we have an empty list or a singleton that can only have the one value.
            retVal = input().attr("type", "hidden").attr("name", name).attr("value", "0");
        } else {
            // Here we have a real selection.
            ContainerTag selector = select().withName(name);
            fillSelector(defaultValue, values, selector);
            retVal = selector;
        }
        newRow(description, retVal);
    }

    /**
     * Add a choice box with a numeric return.  The return value will be the list index of the chosen
     * value, or -1 if the special value is selected.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param defaultValue	default string value to pre-select, or NULL to select the special value
     * @param values		list of string values
     * @param special		special value that returns -1
     */
    public void addChoiceIndexedRow(String name, String description, String defaultValue, List<String> values, String special) {
        DomContent retVal;
        if (values.size() <= 1) {
            // Here we have an empty list or a singleton that can only have the one value.
            retVal = input().attr("type", "hidden").attr("name", name).attr("value", "-1");
        } else {
            // Here we have a real selection.
            ContainerTag selector = select().withName(name);
            // Create the special value.
            ContainerTag option0 = option(special).withValue("-1");
            if (defaultValue == null)
                option0.attr("selected");
            selector.with(option0);
            // Fill in the list values.
            fillSelector(defaultValue, values, selector);
            retVal = selector;
        }
        newRow(description, retVal);
    }

    /**
     * Fill in a selector with the option values from a string list.
     *
     * @param defaultValue	default string to pre-select
     * @param values		list of values to put in for selection
     * @param selector		target container tag
     */
    private void fillSelector(String defaultValue, List<String> values, ContainerTag selector) {
        for (int i = 0; i < values.size(); i++) {
            String optVal = values.get(i);
            ContainerTag option = option(optVal).withValue(Integer.toString(i));
            if (optVal.equals(defaultValue))
                option.attr("selected");
            selector.with(option);
        }
    }

    /**
     * Add a choice box with a string return.  The return value will be the text of the chosen value.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param defaultValue	default string value to pre-select
     * @param values		list of string values
     */
    public void addChoiceRow(String name, String description, String defaultValue, List<String> values) {
        ContainerTag retVal = select().withName(name);
        for (String optVal : values) {
            ContainerTag option = option(optVal).withValue(optVal);
            if (optVal.contentEquals(defaultValue))
                option.attr("selected");
            retVal.with(option);
        }
        newRow(description, retVal);
    }

    /**
     * Add a search box backed by a data list.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param defaultValue	default string value to pre-select
     * @param listName		name of data list
     */
    public void addSearchRow(String name, String description, String defaultValue, String listName) {
        EmptyTag retVal = input().withType("text").withName(name)
                .withId(name).attr("list", listName).withClass("file");
        newRow(description, retVal);
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
    protected void newRow(String description, DomContent inputObject) {
        new Row<Key.Null>(this.inputTable, Key.NONE).add(description).add(inputObject);
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
     * Build a data list for a string set.
     *
     * @param strings		a list of strings to put in the data list
     * @param listName		the name to give to the list
     */
    public void createDataList(List<String> strings, String listName) {
        ContainerTag dataList = datalist().withId(listName);
        for (String string : strings)
            dataList.with(option(string).withValue(string));
        this.form.with(dataList);
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
     * Add a message row.  This crosses the whole table and is generally used for messaging.
     *
     * @param content	HTML content for the row
     */
    public void addMessageRow(DomContent content) {
        MessageRow<Key.Null> row = new MessageRow<Key.Null>(this.inputTable, Key.NONE);
        row.store(content);
    }

    /**
     * Add a checkbox input.  The default is always OFF.
     *
     * @param name			parameter name
     * @param description	parameter description
     */
    public void addCheckBoxRow(String name, String description) {
        boolean defaultVal = this.savedForm.get(name, false);
        addCheckBoxWithDefault(name, description, defaultVal);
    }

    /**
     * Add a checkbox input with a preselected default.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param defaultVal	initial value
     */
    public void addCheckBoxWithDefault(String name, String description, boolean defaultVal) {
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
     * Build a table of filter boxes.  This is done via an special table that only has interior vertical borders
     * and is described by parallel arrays.  Each column in the filter has its own array position.
     *
     * @param id			ID to give to the table
     * @param description	description of what is being filtered
     * @param names			array of parameter names
     * @param titles		descriptions of the parameter
     * @param possibles		list of selections for each parameter
     * @param selections	list of values selected for each parameter
     */
    public void addFilterBox(String id, String description, String[] names, String[] titles, List<? extends Collection<String>> possibles,
            List<? extends Collection<String>> selections) {
        ContainerTag filterTable = table().withClass("filterBox").withId(id);
        // Get iterators for all of the possibilities.
        List<Iterator<String>> iters = possibles.stream().map(x -> x.iterator()).collect(Collectors.toList());
        // Create the first row of the table, containing the titles.
        ContainerTag row = tr().with(Arrays.stream(titles).map(x -> th(a(x).withHref("javascript:toggleFilter('" + id + "','" + x + "');"))));
        // Loop through the data rows.
        boolean found = true;
        while (found) {
            // Add the previous row.
            filterTable.with(row);
            // Start the new row, and denote we have not found any active iterators.
            row = tr();
            found = false;
            // Loop through the columns of this row.
            for (int i = 0; i < names.length; i++) {
                Iterator<String> iter = iters.get(i);
                if (! iter.hasNext())
                    row.with(td(rawHtml("&nbsp;")));
                else {
                    // Here we have an active iterator, so there's a checkbox in this row.
                    String option = iter.next();
                    EmptyTag checkbox = input().withType("checkbox").withName(names[i]).withValue(option);
                    if (selections.get(i).contains(option)) checkbox.attr("checked");
                    row.with(td().with(checkbox).withText(option));
                    found = true;
                }
            }
        }
        // Put in a button to reset all the checkboxes.
        row = tr(td(input().withType("button").attr("onclick", "resetFilter('" + id + "');").attr("value", "Clear Filters"))
                .attr("colspan", names.length).withClass("flag"));
        filterTable.with(row);
        // Now add the form row for this filter box.
        this.newRow(description, filterTable);
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
