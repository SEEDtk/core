/**
 *
 */
package org.theseed.web;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.theseed.io.LineReader;
import org.theseed.reports.PageWriter;
import org.theseed.sequence.blast.BlastDB;
import org.theseed.sequence.blast.Source;

/**
 * @author Bruce Parrello
 *
 */
public class FormTest extends TestCase {

    public void testHtmlForm() throws IOException {
        PageWriter writer = PageWriter.Type.SEEDTK.create();
        File wsDir = new File("data", "Workspace");
        // Make sure we delete the cookie file.
        File cookieFileName = new File(wsDir, "_" + HtmlForm.formCookieName("test", "run") + ".cookie.tbl");
        if (cookieFileName.exists())
            FileUtils.forceDelete(cookieFileName);
        HtmlForm form = buildTestForm(writer, wsDir);
        String formHtml = form.output().render();
        assertThat(formHtml, equalTo("<form method=\"POST\" action=\"/SEEDtk/test.cgi/run\" class=\"web\" enctype=\"multipart/form-data\"><input type=\"hidden\" name=\"workspace\" value=\"parrello\"/><datalist id=\"_data_list_0000\"><option value=\"12022.4.gto\">12022.4.gto</option><option value=\"39803.5.gto\">39803.5.gto</option><option value=\"546.156.gto\">546.156.gto</option><option value=\"562.11147.gto\">562.11147.gto</option><option value=\"attempt_1_small.fasta\">attempt_1_small.fasta</option></datalist><datalist id=\"_data_list_0001\"><option value=\"channels.tbl\">channels.tbl</option><option value=\"ecoli.aln.tbl\">ecoli.aln.tbl</option><option value=\"ecoli.dna.tbl\">ecoli.dna.tbl</option></datalist><table><tr><th>Parameter</th><th>Value</th></tr><tr><td>Query Sequences</td><td><select name=\"qtype\"><option value=\"db\">Existing Blast Database</option><option value=\"dna\" selected>DNA FASTA file</option><option value=\"prot\">Protein FASTA file</option><option value=\"contigs\">Contigs in a GTO</option><option value=\"pegs\">Proteins in a GTO</option><option value=\"features\">Feature DNA in a GTO</option><option value=\"pegs_dna\">DNA of PEGs in a GTO</option><option value=\"rna\">RNA features in a GTO</option></select> <input type=\"checkbox\" onChange=\"configureFiles(this, 'qfile_local', 'qfile_work');\" id=\"qfile\" class=\"fileChecker\"/> Local <input type=\"file\" id=\"qfile_local\" style=\"display: none;\" class=\"file\"/> <input type=\"text\" name=\"qfile\" id=\"qfile_work\" list=\"_data_list_0000\" class=\"file\" style=\"display: inline-block;\"/></td></tr><tr><td>Show more detailed log messages</td><td><input type=\"checkbox\" name=\"debug\"/></td></tr><tr><td>Output Sort Type</td><td><select name=\"sort\"><option value=\"QUERY\" selected>Sort by Query Sequence</option><option value=\"SUBJECT\">Sort by Subject Sequence</option></select></td></tr><tr><td>Table file</td><td><input type=\"checkbox\" onChange=\"configureFiles(this, 'tabFile_local', 'tabFile_work');\" id=\"tabFile\" class=\"fileChecker\"/> Local <input type=\"file\" id=\"tabFile_local\" style=\"display: none;\" class=\"file\"/> <input type=\"text\" name=\"tabFile\" id=\"tabFile_work\" list=\"_data_list_0001\" class=\"file\" style=\"display: inline-block;\"/></td></tr><tr><td>Percent Identity</td><td><input type=\"number\" name=\"pctIdent\" value=\"0\" min=\"0\" max=\"100\"/></td></tr><tr><td>Maximum E-value</td><td><input type=\"text\" name=\"maxE\" value=\"1e-10\"/></td></tr><tr><td>Sample ID filtering</td><td><table class=\"filterBox\" id=\"sampleFilter\"><tr><th><a href=\"javascript:toggleFilter('sampleFilter','host');\">host</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','del');\">del</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','operon');\">operon</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','loc');\">loc</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','asd');\">asd</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','insert');\">insert</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','delete');\">delete</a></th></tr><tr><td><input type=\"checkbox\" name=\"f1\" value=\"7\" checked/>7</td><td><input type=\"checkbox\" name=\"f2\" value=\"0\"/>0</td><td><input type=\"checkbox\" name=\"f3\" value=\"0\"/>0</td><td><input type=\"checkbox\" name=\"f4\" value=\"0\" checked/>0</td><td><input type=\"checkbox\" name=\"f5\" value=\"asdD\"/>asdD</td><td><input type=\"checkbox\" name=\"f6\" value=\"000\" checked/>000</td><td><input type=\"checkbox\" name=\"f7\" value=\"dapA\" checked/>dapA</td></tr><tr><td><input type=\"checkbox\" name=\"f1\" value=\"M\" checked/>M</td><td><input type=\"checkbox\" name=\"f2\" value=\"D\"/>D</td><td><input type=\"checkbox\" name=\"f3\" value=\"Tasd\" checked/>Tasd</td><td><input type=\"checkbox\" name=\"f4\" value=\"A\" checked/>A</td><td><input type=\"checkbox\" name=\"f5\" value=\"asdO\"/>asdO</td><td><input type=\"checkbox\" name=\"f6\" value=\"aceBA\" checked/>aceBA</td><td><input type=\"checkbox\" name=\"f7\" value=\"dhaM\"/>dhaM</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f3\" value=\"TasdA\"/>TasdA</td><td><input type=\"checkbox\" name=\"f4\" value=\"P\" checked/>P</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"aspC\" checked/>aspC</td><td><input type=\"checkbox\" name=\"f7\" value=\"lysA\"/>lysA</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f3\" value=\"TasdA1\" checked/>TasdA1</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"pntAB\" checked/>pntAB</td><td><input type=\"checkbox\" name=\"f7\" value=\"lysC\" checked/>lysC</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"ppc\" checked/>ppc</td><td><input type=\"checkbox\" name=\"f7\" value=\"metL\"/>metL</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"pyc\" checked/>pyc</td><td><input type=\"checkbox\" name=\"f7\" value=\"ptsG\"/>ptsG</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"rhtA\" checked/>rhtA</td><td><input type=\"checkbox\" name=\"f7\" value=\"rhtA\" checked/>rhtA</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"zwf\" checked/>zwf</td><td><input type=\"checkbox\" name=\"f7\" value=\"tdh\"/>tdh</td></tr><tr><td colspan=\"7\" class=\"flag\"><input type=\"button\" onclick=\"resetFilter('sampleFilter');\" value=\"Clear Filters\"/></td></tr></table></td></tr></table><p><input type=\"submit\" class=\"submit\"/></p></form>"));
        // Store new values in the cookie file
        try (CookieFile formData = new CookieFile(wsDir, "form.test.run")) {
            formData.put("sort", BlastDB.SortType.SUBJECT.name());
            formData.put("tabFile", "frog.prince.tbl");
            formData.put("qType", Source.pegs.name());
            formData.put("debug", true);
        }
        formHtml = buildTestForm(writer, wsDir).output().render();
        // Verify that the form has the new defaults.
        assertThat(formHtml, equalTo("<form method=\"POST\" action=\"/SEEDtk/test.cgi/run\" class=\"web\" enctype=\"multipart/form-data\"><input type=\"hidden\" name=\"workspace\" value=\"parrello\"/><table><tr><th>Parameter</th><th>Value</th></tr><tr><td>Query Sequences</td><td><select name=\"qtype\"><option value=\"db\">Existing Blast Database</option><option value=\"dna\" selected>DNA FASTA file</option><option value=\"prot\">Protein FASTA file</option><option value=\"contigs\">Contigs in a GTO</option><option value=\"pegs\">Proteins in a GTO</option><option value=\"features\">Feature DNA in a GTO</option><option value=\"pegs_dna\">DNA of PEGs in a GTO</option><option value=\"rna\">RNA features in a GTO</option></select> <input type=\"checkbox\" onChange=\"configureFiles(this, 'qfile_local', 'qfile_work');\" id=\"qfile\" class=\"fileChecker\"/> Local <input type=\"file\" id=\"qfile_local\" style=\"display: none;\" class=\"file\"/> <input type=\"text\" name=\"qfile\" id=\"qfile_work\" list=\"_data_list_0000\" class=\"file\" style=\"display: inline-block;\"/></td></tr><tr><td>Show more detailed log messages</td><td><input type=\"checkbox\" name=\"debug\" checked/></td></tr><tr><td>Output Sort Type</td><td><select name=\"sort\"><option value=\"QUERY\">Sort by Query Sequence</option><option value=\"SUBJECT\" selected>Sort by Subject Sequence</option></select></td></tr><tr><td>Table file</td><td><input type=\"checkbox\" onChange=\"configureFiles(this, 'tabFile_local', 'tabFile_work');\" id=\"tabFile\" class=\"fileChecker\"/> Local <input type=\"file\" id=\"tabFile_local\" style=\"display: none;\" class=\"file\"/> <input type=\"text\" name=\"tabFile\" id=\"tabFile_work\" list=\"_data_list_0001\" class=\"file\" style=\"display: inline-block;\" value=\"frog.prince.tbl\"/></td></tr><tr><td>Percent Identity</td><td><input type=\"number\" name=\"pctIdent\" value=\"0\" min=\"0\" max=\"100\"/></td></tr><tr><td>Maximum E-value</td><td><input type=\"text\" name=\"maxE\" value=\"1e-10\"/></td></tr><tr><td>Sample ID filtering</td><td><table class=\"filterBox\" id=\"sampleFilter\"><tr><th><a href=\"javascript:toggleFilter('sampleFilter','host');\">host</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','del');\">del</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','operon');\">operon</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','loc');\">loc</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','asd');\">asd</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','insert');\">insert</a></th><th><a href=\"javascript:toggleFilter('sampleFilter','delete');\">delete</a></th></tr><tr><td><input type=\"checkbox\" name=\"f1\" value=\"7\" checked/>7</td><td><input type=\"checkbox\" name=\"f2\" value=\"0\"/>0</td><td><input type=\"checkbox\" name=\"f3\" value=\"0\"/>0</td><td><input type=\"checkbox\" name=\"f4\" value=\"0\" checked/>0</td><td><input type=\"checkbox\" name=\"f5\" value=\"asdD\"/>asdD</td><td><input type=\"checkbox\" name=\"f6\" value=\"000\" checked/>000</td><td><input type=\"checkbox\" name=\"f7\" value=\"dapA\" checked/>dapA</td></tr><tr><td><input type=\"checkbox\" name=\"f1\" value=\"M\" checked/>M</td><td><input type=\"checkbox\" name=\"f2\" value=\"D\"/>D</td><td><input type=\"checkbox\" name=\"f3\" value=\"Tasd\" checked/>Tasd</td><td><input type=\"checkbox\" name=\"f4\" value=\"A\" checked/>A</td><td><input type=\"checkbox\" name=\"f5\" value=\"asdO\"/>asdO</td><td><input type=\"checkbox\" name=\"f6\" value=\"aceBA\" checked/>aceBA</td><td><input type=\"checkbox\" name=\"f7\" value=\"dhaM\"/>dhaM</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f3\" value=\"TasdA\"/>TasdA</td><td><input type=\"checkbox\" name=\"f4\" value=\"P\" checked/>P</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"aspC\" checked/>aspC</td><td><input type=\"checkbox\" name=\"f7\" value=\"lysA\"/>lysA</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f3\" value=\"TasdA1\" checked/>TasdA1</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"pntAB\" checked/>pntAB</td><td><input type=\"checkbox\" name=\"f7\" value=\"lysC\" checked/>lysC</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"ppc\" checked/>ppc</td><td><input type=\"checkbox\" name=\"f7\" value=\"metL\"/>metL</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"pyc\" checked/>pyc</td><td><input type=\"checkbox\" name=\"f7\" value=\"ptsG\"/>ptsG</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"rhtA\" checked/>rhtA</td><td><input type=\"checkbox\" name=\"f7\" value=\"rhtA\" checked/>rhtA</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td><input type=\"checkbox\" name=\"f6\" value=\"zwf\" checked/>zwf</td><td><input type=\"checkbox\" name=\"f7\" value=\"tdh\"/>tdh</td></tr><tr><td colspan=\"7\" class=\"flag\"><input type=\"button\" onclick=\"resetFilter('sampleFilter');\" value=\"Clear Filters\"/></td></tr></table></td></tr></table><p><input type=\"submit\" class=\"submit\"/></p></form>"));
    }

    /**
     * This method builds the test form.
     *
     * @param writer	page writer
     * @param wsDir		workspace directory
     *
     * @return the built form object
     *
     * @throws IOException
     */
    private HtmlForm buildTestForm(PageWriter writer, File wsDir) throws IOException {
        HtmlForm retVal = new HtmlForm("test", "run", "parrello", wsDir, writer);
        retVal.addBlastRow("qtype", "qfile", "Query Sequences");
        retVal.addCheckBoxRow("debug", "Show more detailed log messages");
        retVal.addEnumRow("sort", "Output Sort Type", BlastDB.SortType.QUERY, BlastDB.SortType.values());
        retVal.addFileRow("tabFile", "Table file", HtmlForm.TEXT_FILE_PATTERN);
        retVal.addIntRow("pctIdent", "Percent Identity", 0, 0, 100);
        retVal.addTextRow("maxE", "Maximum E-value", "1e-10");
        // For the filter row, we need parallel arrays.
        String[] names = new String[] { "f1", "f2", "f3", "f4", "f5", "f6", "f7" };
        String[] titles = new String[] { "host", "del", "operon", "loc", "asd", "insert", "delete" };
        List<Set<String>> possibles = new ArrayList<Set<String>>(7);
        try (LineReader choices = new LineReader(new File("data", "choices.tbl"))) {
            for (String line : choices) {
                Set<String> options = new TreeSet<String>(Arrays.asList(StringUtils.split(line, ", ")));
                possibles.add(options);
            }
        }
        List<Collection<String>> selected = new ArrayList<Collection<String>>(7);
        selected.add(Arrays.asList("7", "M"));
        selected.add(Collections.emptySet());
        selected.add(Arrays.asList("Tasd", "TasdA1"));
        selected.add(possibles.get(3));
        selected.add(Collections.emptySet());
        selected.add(possibles.get(5));
        selected.add(Arrays.asList("dapA", "lysC", "rhtA"));
        retVal.addFilterBox("sampleFilter", "Sample ID filtering", names, titles, possibles, selected);
        return retVal;
    }

}
