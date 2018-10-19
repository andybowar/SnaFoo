package snafoo.controllers;

import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import snafoo.exceptions.TooManyVotesException;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler({TooManyVotesException.class})
    public ModelAndView tooManyVotes(TooManyVotesException t) {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("tooManyVotes", t);
        modelAndView.setViewName("tooManyVotesException");

        return modelAndView;
    }

    // Handle missing parameters exceptions
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ModelAndView handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        System.out.println(name + " parameter is missing");

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("exception", ex);

        /* There are only two methods expecting parameters - one
          param is `int[]` and the other is `int`. As such, we can
          always expect the following to work properly. There is
          probably a more pliable way to write this. */
        if(ex.getParameterType().equals("int[]")) {
            modelAndView.setViewName("noVotesException");
        } else if(ex.getParameterType().equals("int")) {
            modelAndView.setViewName("noSuggestionException");
        }

        return modelAndView;
    }

}
