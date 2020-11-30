/**
 *
 */
package org.theseed.reports;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.EmptyTag;

import static j2html.TagCreator.*;

import java.util.stream.Stream;

/**
 * Here the web page is expected to be output in a SEEDtk web environment.  We use standard styles.
 * @author Bruce Parrello
 *
 */
public class SeedTkPageWriter extends PageWriter {

    /** style sheet link */
    public static final EmptyTag SEEDTK_STYLES = link().withRel("stylesheet").withHref("/SEEDtk/css/Basic.css");
    /** logo break */
    private static final DomContent BREAK = br().withClass("logo");

    @Override
    public void writePage(String title, DomContent heading, Stream<DomContent> content) {
        System.out.println(document().render());
        ContainerTag body = body().attr("onload", "setup();")
                .with(a(img().withSrc("/SEEDtk/css/seed-logo-blue.png").withClass("logo").withAlt("SEED logo")).withHref("/SEEDtk/"))
                .with(h1(heading)).with(BREAK).with(content);
        ContainerTag page = html(head(title(title), SEEDTK_STYLES,
                script().withSrc("/SEEDtk/css/utils.js"), script().withSrc("https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"),
                link().withRel("icon").withType("image/png").withHref("/SEEDtk/favicon.ico")), body);
        System.out.println(page.render());
        System.out.flush();
    }

    @Override
    public String local_url(String url) {
        url = fixUrl(url);
        return url;
    }

    /**
     * @return a URL modified for this environment
     *
     * @param url	input URL
     */
    public static String fixUrl(String url) {
        if (url.startsWith("/"))
            url = "/SEEDtk" + url;
        return url;
    }

}
