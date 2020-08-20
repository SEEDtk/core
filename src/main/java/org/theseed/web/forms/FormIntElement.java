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
 * This annotation represents a form integer element.  In addition to the name and label of a regular
 * FormElement, it specifies a minimum and maximum value.  Both int and double fields can use this
 * annotation.
 *
 * @author Bruce Parrello
 *
 */
public @interface FormIntElement {


    /** name of the form element */
    String name();

    /** label to put on the form element */
    String label();

    /** minimum value for the element */
    int min();

    /** maximum value for the element */
    int max();
}
