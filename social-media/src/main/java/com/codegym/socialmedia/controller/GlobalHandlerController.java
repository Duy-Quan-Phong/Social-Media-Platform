package com.codegym.socialmedia.controller;
import com.codegym.socialmedia.annotation.UniqueValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class GlobalHandlerController
{
    @Autowired
    private UniqueValidator validator;

    @InitBinder
    public void initBinder(org.springframework.web.bind.WebDataBinder binder) {
        binder.addValidators(validator);
    }


}
