/**
 *
 */
package org.theseed.reports;

import static j2html.TagCreator.*;

import java.io.PrintWriter;
import java.util.stream.Stream;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.EmptyTag;

/**
 * This is a special writer for web pages that are intended to function as freestanding files.  Note that in this case
 * the local URL will not exist, so it is passed back unmodified.
 *
 * @author Bruce Parrello
 *
 */
public class FreePageWriter extends PageWriter {

    // FIELDS
    /** output stream */
    private PrintWriter writer;
    /** style sheet link */
    public static final EmptyTag SEEDTK_STYLES = link().withRel("stylesheet").withHref("https://core.theseed.org/SEEDtk/css/Basic.css");

    /** Construct a page writer for the system output */
    public FreePageWriter() {
        this.writer = new PrintWriter(System.out);
    }

    /**
     * Construct a page writer for a specific output stream.
     *
     * @oaram writer		target output stream
     */
    public FreePageWriter(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    protected void writePage(String title, DomContent heading, Stream<DomContent> content) {
        this.writer.println(document().render());
        ContainerTag body = body().attr("onload", "setup();")
                .with(h1(heading)).with(content);
        ContainerTag page = html(head(title(title), SEEDTK_STYLES,
                script().withSrc("/SEEDtk/css/utils.js"), script().withSrc("https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"),
                link().withRel("icon").withType("image/png").withHref("https://bioseed.mcs.anl.gov/~parrello/SEEDtk/favicon.ico")), body);
        this.writer.println(page.render());
        this.writer.flush();
    }

    @Override
    public String local_url(String url) {
        return url;
    }

}
