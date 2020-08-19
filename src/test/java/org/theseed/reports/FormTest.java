/**
 *
 */
package org.theseed.reports;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.theseed.sequence.blast.BlastDB;

/**
 * @author Bruce Parrello
 *
 */
public class FormTest extends TestCase {

    public void testHtmlForm() throws IOException {
        PageWriter writer = PageWriter.Type.SEEDTK.create();
        File wsDir = new File("src/test/resources", "Workspace");
        HtmlForm form = new HtmlForm("test", "run", "parrello", wsDir, writer);
        form.addBlastRow("q", "Query Sequences");
        form.addCheckBoxRow("debug", "Show more detailed log messages");
        form.addEnumRow("sort", "Output Sort Type", BlastDB.SortType.QUERY, BlastDB.SortType.values());
        form.addFileRow("tabFile", "Table file", HtmlForm.TEXT_FILE_PATTERN);
        form.addIntRow("pctIdent", "Percent Identity", 0, 0, 100);
        form.addTextRow("maxE", "Maximum E-value", "1e-10");
        String formHtml = form.output().render();
        assertThat(formHtml, equalTo("<form method=\"POST\" action=\"/test.cgi/run\" class=\"web\" enctype=\"multipart/form-data\"><input type=\"hidden\" name=\"workspace\" value=\"parrello\"><datalist id=\"_data_list_0000\"><option value=\"12022.4.gto\">12022.4.gto</option><option value=\"39803.5.gto\">39803.5.gto</option><option value=\"546.156.gto\">546.156.gto</option><option value=\"562.11147.gto\">562.11147.gto</option><option value=\"attempt_1_small.fasta\">attempt_1_small.fasta</option></datalist><datalist id=\"_data_list_0001\"><option value=\"channels.tbl\">channels.tbl</option><option value=\"ecoli.aln.tbl\">ecoli.aln.tbl</option><option value=\"ecoli.dna.tbl\">ecoli.dna.tbl</option></datalist><table><tr><th>Parameter</th><th>Value</th></tr><tr><td>Query Sequences</td><td><select name=\"qtype\"><option value=\"db\">Existing Blast Database</option><option value=\"dna\" selected>DNA FASTA file</option><option value=\"prot\">Protein FASTA file</option><option value=\"contigs\">Contigs in a GTO</option><option value=\"pegs\">Proteins in a GTO</option><option value=\"features\">Feature DNA in a GTO</option><option value=\"pegs_dna\">DNA of PEGs in a GTO</option><option value=\"rna\">RNA features in a GTO</option></select> <input type=\"checkbox\" onChange=\"configureFiles(this, &#x27;qfile_local&#x27;, &#x27;qfile_work&#x27;);\" id=\"qfile\"> Local <input type=\"file\" id=\"qfile_local\" style=\"display: none;\" class=\"file\"> <input type=\"text\" name=\"qfile\" id=\"qfile_work\" list=\"_data_list_0000\" class=\"file\" style=\"display: inline-block;\"></td></tr><tr><td>Show more detailed log messages</td><td><input type=\"checkbox\" name=\"debug\"></td></tr><tr><td>Output Sort Type</td><td><select name=\"sort\"><option value=\"QUERY\" selected>Sort by Query Sequence</option><option value=\"SUBJECT\">Sort by Subject Sequence</option></select></td></tr><tr><td>Table file</td><td><input type=\"checkbox\" onChange=\"configureFiles(this, &#x27;tabFile_local&#x27;, &#x27;tabFile_work&#x27;);\" id=\"tabFile\"> Local <input type=\"file\" id=\"tabFile_local\" style=\"display: none;\" class=\"file\"> <input type=\"text\" name=\"tabFile\" id=\"tabFile_work\" list=\"_data_list_0001\" class=\"file\" style=\"display: inline-block;\"></td></tr><tr><td>Percent Identity</td><td><input type=\"number\" name=\"pctIdent\" value=\"0\" min=\"0\" max=\"100\"></td></tr><tr><td>Maximum E-value</td><td><input type=\"text\" name=\"maxE\" value=\"1e-10\"></td></tr></table><p><input type=\"submit\" class=\"submit\"></p></form>"));
    }

}
