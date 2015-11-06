package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD, ElementType.FIELD})
public @interface Sync {

}
