/**
 *
 */
package org.theseed.reports;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.EmptyTag;

import static j2html.TagCreator.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    /** pattern for BLAST-related files */
    public static final Pattern BLAST_FILE_PATTERN = Pattern.compile(".+\\.(?:gto|fasta|fa|faa|fna)");
    /** pattern for text files */
    public static final Pattern TEXT_FILE_PATTERN = Pattern.compile(".+\\.(?:tbl|txt|tsv)");
    /** pattern for genome files */
    public static final Pattern GTO_FILE_PATTERN = Pattern.compile(".+\\.gto");
    /** pattern for read files */
    public static final Pattern READ_FILE_PATTERN = Pattern.compile(".+\\.(?:fq|fastq)");
    /** maximum number of files to display in a datalist */
    private static final int MAX_FILES = 40;

    /**
     * Construct a new HTML form.
     *
     * @param program	program identifier (the program name will be "web." + identifier + ".jar")
     * @param command	the program command to use
     * @param workspace	name of the workspace
     * @param wsDir		relevant workspace directory
     */
    public HtmlForm(String program, String command, String workspace, File wsDir) {
        this.form = form().withMethod("Get").withAction("/" + program + ".cgi").withClass("web");
        this.inputTable = new HtmlTable<Key.Null>(new ColSpec.Normal("Parameter"), new ColSpec.Normal("Value"));
        // Add a hidden input for the workspace parameter.  The keyword for this parameter is the command name.
        this.form.with(input().withType("hidden").withName(command).withValue(workspace));
        // Now load the file list.  We ignore directories.
        File[] workDirFiles = wsDir.listFiles();
        this.workFiles = Arrays.stream(workDirFiles).filter(x -> x.isFile() && x.canRead()).map(x -> x.getName()).collect(Collectors.toList());
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
        // Create the select tag.
        ContainerTag retVal = select().withName(name);
        for (IDescribable enumVal : values) {
            ContainerTag option = option(enumVal.getDescription()).withValue(enumVal.toString());
            if (init == enumVal)
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
     * @param name			parameter name for the box
     * @param namePattern	a pattern the file name must match
     */
    private DomContent buildFileBox(String name, Pattern namePattern) {
        // We will use a data list.  Only the first N matching file names will be kept.
        String listName = name + "_files";
        ContainerTag dataList = datalist().withId(listName);
        int used = 0;
        for (int i = 0; i < this.workFiles.size() && used < MAX_FILES; i++) {
            String fileName = this.workFiles.get(i);
            Matcher m = namePattern.matcher(fileName);
            if (m.matches()) {
                dataList.with(option(fileName).withValue(fileName));
                used++;
            }
        }
        // Now create an input with the data list.
        DomContent retVal = join(dataList, input().withType("text").withName(name).attr("list", listName).withClass("file"));
        return retVal;
    }

    /**
     * Add a text input.
     *
     * @param name			parameter name
     * @param description	parameter description
     * @param init			default value
     */
    public void addTextRow(String name, String description, String init) {
        EmptyTag inputBox = input().withType("text").withName(name);
        if (init != null && ! init.isEmpty())
            inputBox.withValue(init);
        this.newRow(description, inputBox);
    }

    /**
     * Add a checkbox input.  The default is always OFF.
     *
     * @param name			parameter name
     * @param description	parameter description
     */
    public void addCheckBoxRow(String name, String description) {
        EmptyTag checkBox = input().withType("checkbox").withName(name);
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
        EmptyTag inputBox = input().withType("number").withName(name).withValue(Integer.toString(init))
                .attr("min", Integer.toString(min)).attr("max", Integer.toString(max));
        this.newRow(description, inputBox);
    }

    /**
     * Add a BLAST source input.  This is two boxes, a dropdown and a file box.
     *
     * @param type			source type (will be prefixed to "type" and "file")
     * @param description	parameter description
     */
    public void addBlastRow(String type, String description) {
        DomContent boxes = join(this.buildEnumBox(type + "type", Source.dna, Source.values()),
                this.buildFileBox(type + "file", BLAST_FILE_PATTERN));
        this.newRow(description, boxes);
    }

    /**
     * @return the completed form
     *
     * NOTE that after this the form cannot be modified.
     */
    public ContainerTag output() {
        this.form.with(this.inputTable.output())
            .with(p().with(input().withType("submit").withClass("submit")));
        return this.form;
    }


}
