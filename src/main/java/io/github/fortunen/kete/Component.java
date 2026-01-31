package io.github.fortunen.kete;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

	public static final String SINGLETON = "singleton";
	public static final String TRANSIENT = "transient";

	String name() default "";
	String scope() default TRANSIENT;
}
