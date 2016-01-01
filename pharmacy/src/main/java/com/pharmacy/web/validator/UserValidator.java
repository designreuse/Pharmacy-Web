package com.pharmacy.web.validator;

import com.pharmacy.domain.User;
import com.pharmacy.service.api.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alexander on 28.12.2015.
 */
@Component
public class UserValidator implements Validator {

    private static final Logger LOG = LoggerFactory.getLogger(UserValidator.class);

    @Inject
    private UserService userService;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    @Override
    public void validate(Object target, Errors errors) {
        LOG.trace("Enter validate: target={}, errors={}", target, errors);
        User user = (User) target;

        if (StringUtils.isBlank(user.getLogin())) {
            errors.rejectValue("login", "message.EmptyFirstname");
        }
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            errors.rejectValue("firstName", "message.EmptyFirstname");
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            errors.rejectValue("lastName", "message.EmptyLastname");
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            errors.rejectValue("password", "message.EmptyPassword");
        }

        LOG.debug("exit");
    }

    @Override
    public boolean supports(Class type) {
        return User.class.equals(type);
    }
}
