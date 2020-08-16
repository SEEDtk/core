/**
 *
 */
package org.theseed.reports;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import j2html.tags.DomContent;

import static j2html.TagCreator.*;


/**
 * Html testing class
 *
 * @author Bruce Parrello
 *
 */
public class TestPageWriter extends TestCase {

    public void testConstructs() {
        PageWriter writer = PageWriter.Type.INTERNAL.create();
        assertThat(writer.highlightBlock(p("p1"), p("p2")).render(), equalTo("<div id=\"Pod\"><p>p1</p><p>p2</p></div>"));
        assertThat(writer.scrollBlock(h2("header"), p("body")).render(), equalTo("<div class=\"wide\"><h2>header</h2><p>body</p></div>"));
        List<DomContent> items = new ArrayList<DomContent>(3);
        items.add(p("p1"));
        items.add(code("c1"));
        assertThat(writer.highlightBlock(items).render(), equalTo("<div id=\"Pod\"><p>p1</p><code>c1</code></div>"));
        assertThat(writer.subSection("name", "My Title", p("body text"), p("foot text")).render(), equalTo("<div><h2><a name=\"name\">My Title</a></h2><p>body text</p><p>foot text</p></div>"));
    }
}
