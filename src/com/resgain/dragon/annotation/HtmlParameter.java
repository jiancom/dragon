package com.resgain.dragon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HtmlParameter
{
	public LEVEL value() default LEVEL.Normal;

	public enum LEVEL {
		Simple, Normal, Free; //Simple是最简单的html代码,normal是允许部分html代码存在, Free则允许所有html代码存在
	}
}
