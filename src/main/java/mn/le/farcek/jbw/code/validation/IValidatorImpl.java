/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.validation;

import com.google.inject.Inject;
import java.util.Set;
import javax.validation.ConstraintViolation;
import mn.le.farcek.jbw.api.IValidator;
import mn.le.farcek.jbw.api.validation.ValidationManager;
import mn.le.farcek.jbw.api.validation.ValidatorResult;

public class IValidatorImpl implements IValidator {

    @Inject
    ValidationManager validationManager;

    @Override
    public <T> ValidatorResult<T> validate(T bean, Class<?>... groups) {

        Set<ConstraintViolation<T>> messages = validationManager.validate(bean, groups);

        return new ValidatorResult<>(messages);
    }

}
