package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


/**
 * 
 * 
 * @author Nathaniel Cotton
 * @version 1
 */
@Target(value = {ElementType.METHOD})
public @interface Async {

	int threads = 0;
	
}
