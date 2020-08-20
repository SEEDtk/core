/**
 *
 */
package org.theseed.web.forms;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
/**
 * This form element represents part of a BLAST source specification.  Each consists of a type selection box and a file box.
 * The blast element must be on the file name option, and it must specify the name of the source type option, as shown
 * below.
 *
 *  // type of query file
 *  @Option(name = "--qtype", usage = "type of query input file", required = true)
 *  private Source queryType;
 *
 *  @FormBlastElement(id = "qtype")
 *  /** name of query file
 *  @Option(name = "--qfile", usage = "Query sequences", required = true)
 *  private String queryName;
 *
 * @author Bruce Parrello
 *
 */
public @interface FormBlastElement {

    String id();
}
