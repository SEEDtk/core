/**
 *
 */
package org.theseed.reports;

import static j2html.TagCreator.*;

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

    /** style sheet link */
    public static final EmptyTag SEEDTK_STYLES = link().withRel("stylesheet").withHref("https://bioseed.mcs.anl.gov/~parrello/SEEDtk/css/Basic.css");

    @Override
    protected void writePage(String title, DomContent heading, Stream<DomContent> content) {
        System.out.println(document().render());
        ContainerTag body = body().attr("onload", "setup();")
                .with(h1(heading)).with(content);
        ContainerTag page = html(head(title(title), SEEDTK_STYLES,
                script().withSrc("/SEEDtk/css/utils.js"), script().withSrc("https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"),
                link().withRel("icon").withType("image/png").withHref("https://bioseed.mcs.anl.gov/~parrello/SEEDtk/favicon.ico")), body);
        System.out.println(page.render());
        System.out.flush();
    }

    @Override
    public String local_url(String url) {
        return url;
    }

}
