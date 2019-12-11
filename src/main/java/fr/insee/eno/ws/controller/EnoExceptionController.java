package fr.insee.eno.ws.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import fr.insee.eno.params.exception.EnoParametersException;

@ControllerAdvice
public class EnoExceptionController {
   @ExceptionHandler(value = EnoParametersException.class)
   public ResponseEntity<Object> exception(EnoParametersException exception) {
      return new ResponseEntity<>("EnoParameters : "+exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
   }
}