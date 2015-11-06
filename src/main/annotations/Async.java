package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with the @Async annotation can induce the creation of 
 * threads and will also control how many threads are allowed to access
 * the method at once.
 * 
 * @author Nathaniel Cotton
 * @version 1
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {

	// The number of threads that are allowed to be within the method
	// at one time. If zero, or negative any call to the marked method
	// will allow any number of threads in. 
	int threads() default 0;
	
}
