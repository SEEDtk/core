package org.theseed.reports;

import junit.framework.TestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;



public class TestHtmlTables extends TestCase {

    public void testSortedTables() {
        // This table is sorted by Salary, from high to low.
        HtmlTable<Key.RevFloat> table = new HtmlTable<Key.RevFloat>(new ColSpec.Normal("name"), new ColSpec.Num("Salary"), new ColSpec.ErrorCount("Violations"));
        table.new Row(new Key.RevFloat(10000.55)).add("Clark Kent").addKey().add(0);
        table.new Row(new Key.RevFloat(5672.41)).add("Barry Allen").addKey().add(0);
        table.new Row(new Key.RevFloat(671203.33)).add("Bruce Wayne").addKey().add(6);
        table.new Row(new Key.RevFloat(9000.00)).add("Lois Lane").addKey().add(1);
        assertThat(table.getWidth(), equalTo(3));
        assertThat(table.getHeight(), equalTo(4));
        String output = table.output().render();
        assertThat(output, equalTo("<table><tr><th>name</th><th class=\"num\">Salary</th><th class=\"num\">Violations</th></tr><tr><td>Bruce Wayne</td><td class=\"num\">671203.33</td><td class=\"highlight num\">6</td></tr><tr><td>Clark Kent</td><td class=\"num\">10000.55</td><td class=\"num\">0</td></tr><tr><td>Lois Lane</td><td class=\"num\">9000.00</td><td class=\"highlight num\">1</td></tr><tr><td>Barry Allen</td><td class=\"num\">5672.41</td><td class=\"num\">0</td></tr></table>"));
        // This table is sorted in natural order.
        HtmlTable<Key.Mixed> table2 = new HtmlTable<Key.Mixed>(new ColSpec.Normal("fid"), new ColSpec.Normal("function"), new ColSpec.Num("Length"), new ColSpec.Fraction("Quality"));
        table2.new Row(new Key.Mixed("fig|83333.peg.3")).addKey().add("Aludium phosphate").add(612);
        table2.new Row(new Key.Mixed("fig|10462.peg.2")).addKey().add("Arginine thingsethis").add(546);
        table2.new Row(new Key.Mixed("fig|10462.peg.10")).addKey().add("Adeline oh Adeline").add(462);
        table2.new Row(new Key.Mixed("fig|10462.peg.1")).addKey().add("Threonine stuffase").add(312).add(0.9642);
        table2.new Row(new Key.Mixed("fig|83333.peg.12")).addKey().add("Threonine stuffase").add(512).add(0.246);
        assertThat(table2.getWidth(), equalTo(4));
        assertThat(table2.getHeight(), equalTo(5));
        output = table2.output().render();
        assertThat(output, equalTo("<table><tr><th>fid</th><th>function</th><th class=\"num\">Length</th><th class=\"num\">Quality</th></tr><tr><td>fig|10462.peg.1</td><td>Threonine stuffase</td><td class=\"num\">312</td><td class=\"num\">  0.9642</td></tr><tr><td>fig|10462.peg.2</td><td>Arginine thingsethis</td><td class=\"num\">546</td><td>&nbsp;</td></tr><tr><td>fig|10462.peg.10</td><td>Adeline oh Adeline</td><td class=\"num\">462</td><td>&nbsp;</td></tr><tr><td>fig|83333.peg.3</td><td>Aludium phosphate</td><td class=\"num\">612</td><td>&nbsp;</td></tr><tr><td>fig|83333.peg.12</td><td>Threonine stuffase</td><td class=\"num\">512</td><td class=\"num\">  0.2460</td></tr></table>"));
        // This table is sorted in text order.
        HtmlTable<Key.Text> table3 = new HtmlTable<Key.Text>(new ColSpec.Normal("Subsystem"), new ColSpec.ErrorCount("Errors"));
        table3.new Row(new Key.Text("A new subsystem")).addKey().add(0);
        table3.new Row(new Key.Text("An old subsystem")).addKey().add(0);
        table3.new Row(new Key.Text("Arginine thingnitase")).addKey().add(1);
        table3.new Row(new Key.Text("Adenine synthase")).addKey().add(1);
        table3.new Row(new Key.Text("Alludium deForest")).addKey().add(0);
        table3.new Row(new Key.Text("Alludium deforest")).addKey().add(0);
        table3.new Row(new Key.Text("Frosty the snowman")).addKey().add(1);
        table3.new Row(new Key.Text("Effluent ruthlessness")).addKey().add(0);
        output = table3.output().render();
        assertThat(output, equalTo("<table><tr><th>Subsystem</th><th class=\"num\">Errors</th></tr><tr><td>A new subsystem</td><td class=\"num\">0</td></tr><tr><td>Adenine synthase</td><td class=\"highlight num\">1</td></tr><tr><td>Alludium deforest</td><td class=\"num\">0</td></tr><tr><td>Alludium deForest</td><td class=\"num\">0</td></tr><tr><td>An old subsystem</td><td class=\"num\">0</td></tr><tr><td>Arginine thingnitase</td><td class=\"highlight num\">1</td></tr><tr><td>Effluent ruthlessness</td><td class=\"num\">0</td></tr><tr><td>Frosty the snowman</td><td class=\"highlight num\">1</td></tr></table>"));
        // Finally, the null key.
        HtmlTable<Key.Null> table4 = new HtmlTable<Key.Null>(new ColSpec.Normal("Subsystem").setTip("subsystems are good"),
                new ColSpec.RequiredCount("Good"), new ColSpec.Flag("Expired"));
        table4.new Row(Key.NONE).add("A new subsystem").add(1).add(false);
        table4.new Row(Key.NONE).add("An old subsystem").add(2).add(true);
        table4.new Row(Key.NONE).add("Arginine thingnitase").add(3).add(false);
        table4.new Row(Key.NONE).add("Adenine synthase").add(0).add(true);
        table4.new Row(Key.NONE).add("Alludium deForest").add(3).add(false);
        table4.new Row(Key.NONE).add("Alludium deforest").add(2).add(false);
        table4.new Row(Key.NONE).add("Frosty the snowman").add(1).add(false);
        table4.new Row(Key.NONE).add("Effluent ruthlessness").add(0).add(true);
        output = table4.output().render();
        assertThat(output, equalTo("<table><tr><th><span class=\"tt\">Subsystem<span class=\"btip\">subsystems are good</span></span></th><th class=\"num\">Good</th><th class=\"flag\">Expired</th></tr><tr><td>A new subsystem</td><td class=\"num\">1</td><td class=\"flag\">&nbsp;</td></tr><tr><td>An old subsystem</td><td class=\"num\">2</td><td class=\"flag\">Y</td></tr><tr><td>Arginine thingnitase</td><td class=\"num\">3</td><td class=\"flag\">&nbsp;</td></tr><tr><td>Adenine synthase</td><td class=\"highlight num\">0</td><td class=\"flag\">Y</td></tr><tr><td>Alludium deForest</td><td class=\"num\">3</td><td class=\"flag\">&nbsp;</td></tr><tr><td>Alludium deforest</td><td class=\"num\">2</td><td class=\"flag\">&nbsp;</td></tr><tr><td>Frosty the snowman</td><td class=\"num\">1</td><td class=\"flag\">&nbsp;</td></tr><tr><td>Effluent ruthlessness</td><td class=\"highlight num\">0</td><td class=\"flag\">Y</td></tr></table>"));
    }

    public void testRevRatio() {
        Key.RevRatio key0_2 = new Key.RevRatio(0.0, 2.0); 	//  0.0*
        Key.RevRatio key1_2 = new Key.RevRatio(1.0, 2.0); 	//  0.5*
        Key.RevRatio key4_2 = new Key.RevRatio(4.0, 2.0); 	//  2.0*
        Key.RevRatio keyn1_2 = new Key.RevRatio(-1.0, 2.0);	// -0.5*
        Key.RevRatio key0_1 = new Key.RevRatio(0.0, 1.0);	//  0.0*
        Key.RevRatio key1_1 = new Key.RevRatio(1.0, 1.0);	//  1.0*
        Key.RevRatio key4_1 = new Key.RevRatio(4.0, 1.0);	//  4.0*
        Key.RevRatio keyn1_1 = new Key.RevRatio(-1.0, 1.0);	// -1.0*
        Key.RevRatio key0_0 = new Key.RevRatio(0.0, 0.0);	//  0/0*
        Key.RevRatio key1_0 = new Key.RevRatio(1.0, 0.0);	//  1/0*
        Key.RevRatio key4_0 = new Key.RevRatio(4.0, 0.0);	//  4/0*
        Key.RevRatio keyn1_0 = new Key.RevRatio(-1.0, 0.0);	// -1/0*
        Key.RevRatio key0_n1 = new Key.RevRatio(0.0, -1.0);	//  0.0*
        Key.RevRatio key1_n1 = new Key.RevRatio(1.0, -1.0);	// -1.0*
        Key.RevRatio key4_n1 = new Key.RevRatio(4.0, -1.0);	// -4.0*
        Key.RevRatio keyn1_n1 = new Key.RevRatio(-1.0, -1.0);// 1.0*
        Key.RevRatio[] values = new Key.RevRatio[] { key0_2, key1_2, key4_2, keyn1_2,
                key0_1, key1_1, key4_1, keyn1_1, key0_0, key1_0, key4_0, keyn1_0,
                key0_n1, key1_n1, key4_n1, keyn1_n1 };
        Arrays.sort(values);
        assertThat(values, arrayContaining(key4_1, key4_2, key1_1, keyn1_n1, key1_2, key0_2,
                key0_1, key0_n1, keyn1_2, keyn1_1, key1_n1, key4_n1,
                key4_0, key1_0, keyn1_0, key0_0));

    }

}
