package com.bbbyxxx.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//@Target说明了Annotation所修饰的对象范围
/*
ElementType.TYPE：说明该注解只能被声明在一个类前。
ElementType.FIELD：说明该注解只能被声明在一个类的字段前。
ElementType.METHOD：说明该注解只能被声明在一个类的方法前。
ElementType.PARAMETER：说明该注解只能被声明在一个方法参数前。
ElementType.CONSTRUCTOR：说明该注解只能声明在一个类的构造方法前。
ElementType.LOCAL_VARIABLE：说明该注解只能声明在一个局部变量前。
ElementType.ANNOTATION_TYPE：说明该注解只能声明在一个注解类型前。
ElementType.PACKAGE：说明该注解只能声明在一个包名前。
 */
@Target({METHOD,FIELD,ANNOTATION_TYPE,CONSTRUCTOR,PARAMETER})
/*
注解@Retention可以用来修饰注解，是注解的注解，称为元注解
按生命周期来划分可分为3类：
1、RetentionPolicy.SOURCE：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；
2、RetentionPolicy.CLASS：注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期；
3、RetentionPolicy.RUNTIME：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
 */
@Retention(RUNTIME)
//Documented注解表明这个注释是由 javadoc记录的，在默认情况下也有类似的记录工具。 如果一个类型声明被注释了文档化，它的注释成为公共API的一部分。
@Documented
//真正的处理逻辑
@Constraint(validatedBy = {isMobileValidator.class})
public @interface isMobile {
    boolean required() default true;

    String message() default "手机号格式错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
