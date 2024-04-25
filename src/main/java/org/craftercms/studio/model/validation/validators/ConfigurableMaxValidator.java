package org.craftercms.studio.model.validation.validators;

import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.validation.annotations.ConfigurableMax;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Custom validator for the {@link ConfigurableMax} annotation.
 * It validates that the input value does not exceed the maximum length
 * Notice that null values are considered valid.
 */
public class ConfigurableMaxValidator implements ConstraintValidator<ConfigurableMax, String> {
    public static final int DEFAULT_MAX = 512 * 1024;

    private final StudioConfiguration studioConfiguration;
    private String propertyName;

    public ConfigurableMaxValidator(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    public void initialize(ConfigurableMax constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        propertyName = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        int maxLength = studioConfiguration.getProperty(propertyName, Integer.class, DEFAULT_MAX);
        if (value == null) {
            return true;
        }
        return value.length() <= maxLength;
    }
}
