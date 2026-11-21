package com.medicalstore.pos.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueDniValidator.class)
@Documented
public @interface UniqueDni {
    String message() default "El DNI ya está registrado en el sistema";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}