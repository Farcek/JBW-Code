/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.validation;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import mn.le.farcek.jbw.api.validation.ValidationManager;

@Singleton
public class IValidationManagerImpl implements ValidationManager {

    private final Injector injector;

    @Inject
    public IValidationManagerImpl(Injector injector) {
        this.injector = injector;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T bean, Class<?>... groups) {
        return getValidator().validate(bean, groups);
    }

    @Override
    public boolean valid(Object bean, Class<?>... groups) {
        return validate(bean, groups).isEmpty();
    }

    private Validator validator;

    private Validator getValidator() {
        if (validator == null) {
            Configuration<?> configure = Validation.byDefaultProvider().configure();
            configure.constraintValidatorFactory(new GuiceConstraintValidatorFactory(injector));
            ValidatorFactory factory = configure.buildValidatorFactory();

            validator = factory.getValidator();

        }
        return validator;
    }
}
