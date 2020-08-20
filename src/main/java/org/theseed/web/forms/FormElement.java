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
 * This annotation specifies a basic form element.  The name and display label are taken from the
 * associated @Option annotation.
 *
 * This is a very crude mechanism.  The annotated field must be protected, and the processor class must
 * be in "org.theseed.web".  At some future point we may fix this.  In the meantime, it simplifies a lot.
 *
 * @author Bruce Parrello
 *
 */
public @interface FormElement {

}
