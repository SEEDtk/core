/**
 *
 */
package org.theseed.web.graph;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import j2html.tags.ContainerTag;

/**
 * @author Bruce Parrello
 *
 */
public class ScatterGraphTest {

    @Test
    public void testGraph() throws IOException {
        ScatterGraph graph = new ScatterGraph(1000, 800, "graph", 20, 20, 100, 100);
        ContainerTag html = graph.getHtml();
        assertThat(html.getTagName(), equalTo("svg"));
        assertThat(html.getNumChildren(), greaterThanOrEqualTo(2));
        File inFile = new File("data", "scatter.tbl");
        graph.readPoints(inFile, "sample_id", "production", "predicted");
        graph.plot();
        graph.drawXBound(1.0);
        graph.drawYBound(0.8);
        System.out.println(html.render());
    }

}
