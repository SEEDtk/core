/**
 *
 */
package org.theseed.subsystems;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.theseed.io.LineReader;
import org.theseed.io.MarkerFile;

/**
 * Test subsystem classes
 *
 * @author Bruce Parrello
 *
 */
public class SubsystemTest {

    @Test
    public void testCell() {
        RowData row = new RowData("123.4", "fake genome", "likely");
        assertThat(row.getGenomeId(), equalTo("123.4"));
        assertThat(row.isActive(), equalTo(true));
        String rowHtml = row.displayGenome().render();
        assertThat(rowHtml, stringContainsInOrder(Arrays.asList("<a", "123.4", "fake genome</a>")));
        assertThat(row.getVariant(), equalTo("likely"));
        CellData cell = new CellData(row, "");
        assertThat(cell.isEmpty(), equalTo(true));
        assertThat(cell.display().render(), equalTo("&nbsp;"));
        assertThat(cell.contains("fig|123.4.peg.6"), equalTo(false));
        assertThat(cell.contains("fig|123.4.rna.6"), equalTo(false));
        cell = new CellData(row, "6,rna.7");
        assertThat(cell.isEmpty(), equalTo(false));
        assertThat(cell.display().render(), stringContainsInOrder(Arrays.asList("fig|123.4.peg.6", "fig|123.4.rna.7")));
        assertThat(cell.contains("fig|123.4.peg.6"), equalTo(true));
        assertThat(cell.contains("fig|123.4.rna.6"), equalTo(false));
        assertThat(cell.size(), equalTo(2));
        cell = new CellData(row, "9");
        assertThat(cell.isEmpty(), equalTo(false));
        assertThat(cell.display().render(), stringContainsInOrder(Arrays.asList("fig|123.4.peg.9")));
        assertThat(cell.contains("fig|123.4.peg.9"), equalTo(true));
        assertThat(cell.contains("fig|123.4.peg.6"), equalTo(false));
        assertThat(cell.size(), equalTo(1));
        assertThat(row.getTypes(), containsInAnyOrder("rna", "peg"));
    }

    @Test
    public void testRow() throws IOException {
        File coreDir = new File("data");
        RowData row = RowData.load(coreDir, "83333.1\t-1\t\t\t", 6);
        assertThat(row, nullValue());
        row = RowData.load(coreDir, "83333.1\tactive\trna.4,5\t\t30\t100", 6);
        RowData row2 = RowData.load(coreDir, "83333.1\t0\t101\t102", 6);
        RowData row3 = RowData.load(coreDir, "100226.1\tactive\t104\t106\t225\t\t\t981", 6);
        assertThat(row2.getGenomeId(), equalTo("83333.1"));
        assertThat(row3.getGenomeId(), equalTo("100226.1"));
        assertThat(row3.isMissing(), equalTo(true));
        assertThat(row2.isActive(), equalTo(false));
        assertThat(row2.isActive(), equalTo(false));
        assertThat(row, equalTo(row2));
        assertThat(row.compareTo(row2), equalTo(0));
        assertThat(row, not(equalTo(row3)));
        assertThat(row3.compareTo(row2), lessThan(0));
        assertThat(row.displayGenome().render(), stringContainsInOrder(Arrays.asList("<a", "83333.1", "Escherichia coli", "</a>")));
        CellData cell0 = row.getCell(0);
        assertThat(cell0.contains("fig|83333.1.rna.4"), equalTo(true));
        assertThat(cell0.contains("fig|83333.1.peg.5"), equalTo(true));
        assertThat(cell0.getStatus("fig|83333.1.rna.4").getState(), equalTo(PegState.MISSING));
        FeatureStatus status30 = row.getCell(2).getStatus("fig|83333.1.peg.30");
        assertThat(status30, notNullValue());
        status30.setState(PegState.BAD_ROLE, "improper function");
        FeatureStatus status30a = row.getCell(2).getStatus("fig|83333.1.peg.30");
        assertThat(status30a, sameInstance(status30));
        assertThat(status30a.getState(), equalTo(PegState.BAD_ROLE));
        assertThat(status30a.getFunction(), equalTo("improper function"));
        Map<String, String> funs = row2.getFunctions();
        assertThat(funs.size(), equalTo(4322));
        assertThat(funs.get("fig|83333.1.peg.4135"), equalTo("Programmed cell death toxin PemK"));
        assertThat(funs.get("fig|83333.1.peg.4427"), equalTo("Purine nucleoside phosphoramidase HinT"));
        assertThat(funs.get("fig|83333.1.peg.4428"), equalTo("Transcriptional regulator YjdC, AcrR family"));
        // Verify no deleted features are in the function map.
        Set<String> deleted = new HashSet<String>(1000);
        try (LineReader delFile = new LineReader(new File("data/Organisms/83333.1/Features/peg", "deleted.features"))) {
            for (String fid : delFile)
                deleted.add(fid);
        }
        for (String fid : funs.keySet()) {
            assertThat(fid, containsString("peg"));
            assertThat(fid, deleted.contains(fid), equalTo(false));
        }
    }

    /**
     * test column data
     */
    @Test
    public void testColumns() {
        File coreDir = new File("data");
        ColumnData col0 = new ColumnData(0, "R1", "fake role / other role");
        assertThat(col0.getColIdx(), equalTo(0));
        assertThat(col0.getFunction(), equalTo("fake role / other role"));
        assertThat(col0.matches("fake role / other role # comment"), equalTo(true));
        assertThat(col0.matches("bonus role @ fake role / other role"), equalTo(true));
        assertThat(col0.matches("fake role / other role"), equalTo(true));
        assertThat(col0.matches("fake role"), equalTo(false));
        assertThat(col0.matches("other role"), equalTo(false));
        assertThat(col0.matches("bonus role ! comment"), equalTo(false));
        ColumnData col1 = new ColumnData(1,"R2", "bonus role");
        assertThat(col1.getColIdx(), equalTo(1));
        assertThat(col1.matches("bonus role"), equalTo(true));
        assertThat(col1.matches("bonus role @ fake role / other role # comment"), equalTo(true));
        assertThat(col1.matches("fake role / other role"), equalTo(false));
        assertThat(col1.matches("fake role"), equalTo(false));
        assertThat(col1.getAbbr(), equalTo("R2"));
        ColumnData col2 = new ColumnData(2, "R3", "new role");
        RowData row = RowData.load(coreDir, "83333.1\t0\t101\t102,103\t104", 6);
        assertThat(row.isActive(), equalTo(false));
        row.getCell(0).setState("fig|83333.1.peg.100", "bonus role", col0);
        assertThat(row.getCell(0).contains("fig|83333.1.peg.100"), equalTo(false));
        row.getCell(0).setState("fig|83333.1.peg.101", "fake role / other role # comment", col0);
        assertThat(row.getCell(0).getStatus("fig|83333.1.peg.101").getState(), equalTo(PegState.GOOD));
        row.getCell(1).setState("fig|83333.1.peg.99", "bonus role / other role", col1);
        assertThat(row.getCell(1).getStatus("fig|83333.1.peg.99").getState(), equalTo(PegState.DISCONNECTED));
        row.getCell(1).setState("fig|83333.1.peg.102", "fake role", col1);
        FeatureStatus status = row.getCell(1).getStatus("fig|83333.1.peg.102");
        assertThat(status.getState(), equalTo(PegState.BAD_ROLE));
        assertThat(status.getFunction(), equalTo("fake role"));
        col0.countCell(row.getCell(0));
        assertThat(col0.getCount(PegState.GOOD), equalTo(1));
        assertThat(col0.getCount(PegState.MISSING), equalTo(0));
        assertThat(col0.getCount(PegState.DISCONNECTED), equalTo(0));
        assertThat(col0.getCount(PegState.BAD_ROLE), equalTo(0));
        assertThat(col0.hasErrors(), equalTo(false));
        col1.countCell(row.getCell(1));
        assertThat(col1.getCount(PegState.GOOD), equalTo(0));
        assertThat(col1.getCount(PegState.MISSING), equalTo(1));
        assertThat(col1.getCount(PegState.DISCONNECTED), equalTo(1));
        assertThat(col1.getCount(PegState.BAD_ROLE), equalTo(1));
        assertThat(col1.hasErrors(), equalTo(true));
        col2.countCell(row.getCell(2));
        assertThat(col2.getCount(PegState.GOOD), equalTo(0));
        assertThat(col2.getCount(PegState.MISSING), equalTo(1));
        assertThat(col2.getCount(PegState.DISCONNECTED), equalTo(0));
        assertThat(col2.getCount(PegState.BAD_ROLE), equalTo(0));
        assertThat(col2.hasErrors(), equalTo(true));
        row = RowData.load(coreDir, "99287.1\tactive\t\t1,2", 6);
        assertThat(row.isActive(), equalTo(true));
        row.getCell(1).setState("fig|99287.1.peg.1", "wrong role", col1);
        col1.countCell(row.getCell(1));
        row = RowData.load(coreDir, "209261.1\tactive\t\t4", 6);
        assertThat(row.isActive(), equalTo(true));
        row.getCell(1).setState("fig|209261.1.peg.4", "wrong role", col1);
        col1.countCell(row.getCell(1));
        assertThat(col1.getCount(PegState.GOOD), equalTo(0));
        assertThat(col1.getCount(PegState.MISSING), equalTo(2));
        assertThat(col1.getCount(PegState.DISCONNECTED), equalTo(1));
        assertThat(col1.getCount(PegState.BAD_ROLE), equalTo(3));
        SortedSet<ColumnData.BadRole> badRoles = col1.getBadRoles();
        assertThat(badRoles.size(), equalTo(2));
        assertThat(badRoles.first().getFunction(), equalTo("wrong role"));
        assertThat(badRoles.first().getFids(), contains("fig|209261.1.peg.4", "fig|99287.1.peg.1"));
        assertThat(badRoles.last().getFunction(), equalTo("fake role"));
        assertThat(badRoles.last().getFids(), contains("fig|83333.1.peg.102"));
    }

    @Test
    public void testSubsystem() throws IOException {
        File coreDir = new File("data");
        SubsystemData subsystem = SubsystemData.load(coreDir, "Threonine_synthesis");
        assertThat(subsystem, nullValue());
        subsystem = SubsystemData.load(coreDir, "2-nitroimidazole_resistance");
        assertThat(subsystem.getName(), equalTo("2-nitroimidazole resistance"));
        assertThat(subsystem.getId(), equalTo("2-nitroimidazole_resistance"));
        assertThat(subsystem.numGenomesMissing(), equalTo(0));
        assertThat(subsystem.getWidth(), equalTo(3));
        assertThat(subsystem.size(), equalTo(4));
        assertThat(subsystem.isSuspectErrorCount(), equalTo(true));
        List<RowData> rows = new ArrayList<RowData>(subsystem.getRows());
        rows.sort(null);
        RowData row = rows.get(0);
        assertThat(row.isActive(), equalTo(false));
        assertThat(row.getGenomeId(), equalTo("83333.1"));
        assertThat(row.getVariant(), equalTo("inactive"));
        assertThat(row.getCell(0).contains("fig|83333.1.peg.4"), equalTo(true));
        assertThat(row.getCell(1).contains("fig|83333.1.peg.5"), equalTo(true));
        assertThat(row.getCell(2).contains("fig|83333.1.rna.6"), equalTo(true));
        row = rows.get(3);
        assertThat(row.isActive(), equalTo(true));
        assertThat(row.getGenomeId(), equalTo("99287.1"));
        assertThat(row.getVariant(), equalTo("active.1.1"));
        assertThat(row.getCell(0).contains("fig|99287.1.peg.1237"), equalTo(true));
        assertThat(row.getCell(1).contains("fig|99287.1.peg.1236"), equalTo(true));
        assertThat(row.getCell(2).contains("fig|99287.1.peg.3176"), equalTo(true));
        assertThat(row.getCell(2).contains("fig|99287.1.peg.3177"), equalTo(true));
        assertThat(row.getCell(1).contains("fig|99287.1.peg.3176"), equalTo(false));
        subsystem.validateRows();
        assertThat(subsystem.isSuspectErrorCount(), equalTo(false));
        assertThat(MarkerFile.readInt(SubsystemData.errorCountFile(coreDir, subsystem.getId())), equalTo(11));
        SubsystemData subsystem2 = SubsystemData.survey(coreDir, "2-nitroimidazole_resistance");
        assertThat(subsystem2.getId(), equalTo(subsystem.getId()));
        assertThat(subsystem2.getName(), equalTo(subsystem.getName()));
        assertThat(subsystem2.getErrorCount(), equalTo(subsystem.getErrorCount()));
        assertThat(subsystem2.isSuspectErrorCount(), equalTo(false));
        assertThat(subsystem2.isPrivate(), equalTo(false));
        assertThat(subsystem2.getCurator(), equalTo("VeronikaV"));
        ColumnData col = subsystem.getColumns()[0];
        assertThat(col.getAbbr(), equalTo("NimR"));
        assertThat(col.getFunction(), equalTo("Transcriptional regulator of NimT, AraC family"));
        assertThat(col.getCount(PegState.GOOD), equalTo(3));
        assertThat(col.getCount(PegState.BAD_ROLE), equalTo(1));
        col = subsystem.getColumns()[1];
        assertThat(col.getAbbr(), equalTo("NimT"));
        assertThat(col.getFunction(), equalTo("2-nitroimidazole transporter NimT"));
        assertThat(col.getCount(PegState.DISCONNECTED), equalTo(1));
        assertThat(col.getCount(PegState.MISSING), equalTo(1));
        col = subsystem.getColumns()[2];
        assertThat(col.getAbbr(), equalTo("YeaO"));
        assertThat(col.getFunction(), equalTo("Uncharacterized protein YeaO"));
        assertThat(col.getCount(PegState.MISSING), equalTo(1));
        assertThat(col.getCount(PegState.BAD_ROLE), equalTo(2));
        assertThat(col.getBadRoles().size(), equalTo(2));
        assertThat(subsystem.getErrorCount(), equalTo(11));
        assertThat(subsystem.getHealth(), lessThan(1.0));
        subsystem = SubsystemData.load(coreDir, "5-oxoprolinase");
        assertThat(subsystem.getName(), equalTo("5-oxoprolinase"));
        assertThat(subsystem.getMissingGenomes(), contains("100226.1"));
        assertThat(subsystem.size(), equalTo(4));
        ColumnData[] cols = subsystem.getColumns();
        assertThat(cols[0].isAux(), equalTo(false));
        assertThat(cols[1].isAux(), equalTo(false));
        assertThat(cols[2].isAux(), equalTo(false));
        assertThat(cols[3].isAux(), equalTo(false));
        assertThat(cols[4].isAux(), equalTo(true));
        assertThat(cols[5].isAux(), equalTo(true));
        assertThat(cols[6].isAux(), equalTo(true));
        assertThat(cols[7].isAux(), equalTo(true));
        assertThat(cols[8].isAux(), equalTo(false));
        assertThat(cols[9].isAux(), equalTo(true));
        assertThat(cols[10].isAux(), equalTo(true));
        subsystem = SubsystemData.load(coreDir, "Phenylalanine_and_Tyrosine_synthesis");
        subsystem.validateRows();
        assertThat(MarkerFile.readInt(SubsystemData.errorCountFile(coreDir, subsystem.getId())), equalTo(0));
        assertThat(subsystem.getErrorCount(), equalTo(0));
        assertThat(subsystem.getHealth(), equalTo(1.0));
        assertThat(subsystem.getCurator(), equalTo("gjo"));
        subsystem = SubsystemData.load(coreDir, "ZZ_gjo_need_homes");
        assertThat(subsystem.isPrivate(), equalTo(true));
    }

}
