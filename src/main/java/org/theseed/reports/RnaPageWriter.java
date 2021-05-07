/**
 *
 */
package org.theseed.reports;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.document;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.img;
import static j2html.TagCreator.link;
import static j2html.TagCreator.script;
import static j2html.TagCreator.title;

import java.util.stream.Stream;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.EmptyTag;

/**
 * This is the writer for the RNA SEQ web site.  It is similar to the SEEDtk website, but does not have the SEEDtk directory
 * prefix.
 *
 * @author Bruce Parrello
 *
 */
public class RnaPageWriter extends PageWriter {

    /** style sheet link */
    private static final EmptyTag RNA_STYLES = link().withRel("stylesheet").withHref("/css/Basic.css");
    /** logo break */
    private static final DomContent BREAK = br().withClass("logo");

    @Override
    public void writePage(String title, DomContent heading, Stream<DomContent> content) {
        System.out.println(document().render());
        ContainerTag body = body().attr("onload", "setup();")
                .with(a(img().withSrc("/css/seed-logo-blue.png").withClass("logo").withAlt("SEED logo")).withHref("/"))
                .with(h1(heading)).with(BREAK).with(content);
        ContainerTag page = html(head(title(title), RNA_STYLES,
                script().withSrc("/css/utils.js"), script().withSrc("https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"),
                link().withRel("icon").withType("image/png").withHref("/favicon.ico")), body);
        System.out.println(page.render());
        System.out.flush();
    }

    @Override
    public String local_url(String url) {
        return url;
    }

}
