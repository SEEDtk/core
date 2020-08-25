/**
 *
 */
package org.theseed.web;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.theseed.reports.HtmlForm;
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
        assertThat(formHtml, equalTo("<form method=\"POST\" action=\"/SEEDtk/test.cgi/run\" class=\"web\" enctype=\"multipart/form-data\"><input type=\"hidden\" name=\"workspace\" value=\"parrello\"/><datalist id=\"_data_list_0000\"><option value=\"12022.4.gto\">12022.4.gto</option><option value=\"39803.5.gto\">39803.5.gto</option><option value=\"546.156.gto\">546.156.gto</option><option value=\"562.11147.gto\">562.11147.gto</option><option value=\"attempt_1_small.fasta\">attempt_1_small.fasta</option></datalist><datalist id=\"_data_list_0001\"><option value=\"channels.tbl\">channels.tbl</option><option value=\"ecoli.aln.tbl\">ecoli.aln.tbl</option><option value=\"ecoli.dna.tbl\">ecoli.dna.tbl</option></datalist><table><tr><th>Parameter</th><th>Value</th></tr><tr><td>Query Sequences</td><td><select name=\"qtype\"><option value=\"db\">Existing Blast Database</option><option value=\"dna\" selected>DNA FASTA file</option><option value=\"prot\">Protein FASTA file</option><option value=\"contigs\">Contigs in a GTO</option><option value=\"pegs\">Proteins in a GTO</option><option value=\"features\">Feature DNA in a GTO</option><option value=\"pegs_dna\">DNA of PEGs in a GTO</option><option value=\"rna\">RNA features in a GTO</option></select> <input type=\"checkbox\" onChange=\"configureFiles(this, 'qfile_local', 'qfile_work');\" id=\"qfile\" class=\"fileChecker\"/> Local <input type=\"file\" id=\"qfile_local\" style=\"display: none;\" class=\"file\"/> <input type=\"text\" name=\"qfile\" id=\"qfile_work\" list=\"_data_list_0000\" class=\"file\" style=\"display: inline-block;\"/></td></tr><tr><td>Show more detailed log messages</td><td><input type=\"checkbox\" name=\"debug\"/></td></tr><tr><td>Output Sort Type</td><td><select name=\"sort\"><option value=\"QUERY\" selected>Sort by Query Sequence</option><option value=\"SUBJECT\">Sort by Subject Sequence</option></select></td></tr><tr><td>Table file</td><td><input type=\"checkbox\" onChange=\"configureFiles(this, 'tabFile_local', 'tabFile_work');\" id=\"tabFile\" class=\"fileChecker\"/> Local <input type=\"file\" id=\"tabFile_local\" style=\"display: none;\" class=\"file\"/> <input type=\"text\" name=\"tabFile\" id=\"tabFile_work\" list=\"_data_list_0001\" class=\"file\" style=\"display: inline-block;\"/></td></tr><tr><td>Percent Identity</td><td><input type=\"number\" name=\"pctIdent\" value=\"0\" min=\"0\" max=\"100\"/></td></tr><tr><td>Maximum E-value</td><td><input type=\"text\" name=\"maxE\" value=\"1e-10\"/></td></tr></table><p><input type=\"submit\" class=\"submit\"/></p></form>"));
        // Store new values in the cookie file
        try (CookieFile formData = new CookieFile(wsDir, "form.test.run")) {
            formData.put("sort", BlastDB.SortType.SUBJECT.name());
            formData.put("tabFile", "frog.prince.tbl");
            formData.put("qType", Source.pegs.name());
            formData.put("debug", true);
        }
        formHtml = buildTestForm(writer, wsDir).output().render();
        // Verify that the form has the new defaults.
        assertThat(formHtml, equalTo("<form method=\"POST\" action=\"/SEEDtk/test.cgi/run\" class=\"web\" enctype=\"multipart/form-data\"><input type=\"hidden\" name=\"workspace\" value=\"parrello\"/><table><tr><th>Parameter</th><th>Value</th></tr><tr><td>Query Sequences</td><td><select name=\"qtype\"><option value=\"db\">Existing Blast Database</option><option value=\"dna\" selected>DNA FASTA file</option><option value=\"prot\">Protein FASTA file</option><option value=\"contigs\">Contigs in a GTO</option><option value=\"pegs\">Proteins in a GTO</option><option value=\"features\">Feature DNA in a GTO</option><option value=\"pegs_dna\">DNA of PEGs in a GTO</option><option value=\"rna\">RNA features in a GTO</option></select> <input type=\"checkbox\" onChange=\"configureFiles(this, 'qfile_local', 'qfile_work');\" id=\"qfile\" class=\"fileChecker\"/> Local <input type=\"file\" id=\"qfile_local\" style=\"display: none;\" class=\"file\"/> <input type=\"text\" name=\"qfile\" id=\"qfile_work\" list=\"_data_list_0000\" class=\"file\" style=\"display: inline-block;\"/></td></tr><tr><td>Show more detailed log messages</td><td><input type=\"checkbox\" name=\"debug\" checked/></td></tr><tr><td>Output Sort Type</td><td><select name=\"sort\"><option value=\"QUERY\">Sort by Query Sequence</option><option value=\"SUBJECT\" selected>Sort by Subject Sequence</option></select></td></tr><tr><td>Table file</td><td><input type=\"checkbox\" onChange=\"configureFiles(this, 'tabFile_local', 'tabFile_work');\" id=\"tabFile\" class=\"fileChecker\"/> Local <input type=\"file\" id=\"tabFile_local\" style=\"display: none;\" class=\"file\"/> <input type=\"text\" name=\"tabFile\" id=\"tabFile_work\" list=\"_data_list_0001\" class=\"file\" style=\"display: inline-block;\" value=\"frog.prince.tbl\"/></td></tr><tr><td>Percent Identity</td><td><input type=\"number\" name=\"pctIdent\" value=\"0\" min=\"0\" max=\"100\"/></td></tr><tr><td>Maximum E-value</td><td><input type=\"text\" name=\"maxE\" value=\"1e-10\"/></td></tr></table><p><input type=\"submit\" class=\"submit\"/></p></form>"));
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
        return retVal;
    }

}
