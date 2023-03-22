package com.ubiqube.juju.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ubiqube.juju.dto.ProblemDetails;

@ControllerAdvice
public class JujuControllerAdvice {
	private static final Logger LOG = LoggerFactory.getLogger(JujuControllerAdvice.class);

	@SuppressWarnings("static-method")
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetails> handleValidationExceptions(final MethodArgumentNotValidException ex) {
		final StringBuilder errors = new StringBuilder();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			LOG.warn("{}", error);
			final String fieldName = ((FieldError) error).getField();
			final String objectName = ((FieldError) error).getObjectName();
			final String errorMessage = error.getDefaultMessage();
			errors.append(objectName).append(".").append(fieldName).append(": ").append(errorMessage).append("\n");
		});
		final ProblemDetails problemDetail = new ProblemDetails(400, errors.toString());
		return ResponseEntity.status(400)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problemDetail);
	}

}
