package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Any method or field that is annotated with the @Order annotation will poll
 * the ThreadTree for when execution should continue execution. This will
 * maintain the order of execution in a multi-threaded system from a
 * single-threaded system.
 * 
 * @author Nathaniel Cotton
 * 
 */
@Target(value = { ElementType.METHOD, ElementType.FIELD })
public @interface Order {

}
