package com.ubiqube.juju.controller;

import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ubiqube.juju.dto.ProblemDetails;

@RestController
public class ErrorController extends AbstractErrorController {

	private static final Logger LOG = LoggerFactory.getLogger(ErrorController.class);

	public ErrorController(final ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	@RequestMapping("/error")
	public ResponseEntity<ProblemDetails> getError(final HttpServletRequest request) {
		final Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		final Map<String, Object> ea = getErrorAttributes(request, ErrorAttributeOptions.of(Include.EXCEPTION, Include.MESSAGE, Include.BINDING_ERRORS));
		final String message = buildMessage(ea);
		LOG.trace("Error controller: {} => {}", status, ea);
		if (status != null) {
			final ProblemDetails problemDetail = new ProblemDetails(status, message);
			return ResponseEntity.status(status)
					.contentType(MediaType.APPLICATION_PROBLEM_JSON)
					.body(problemDetail);
		}
		final ProblemDetails problemDetail = new ProblemDetails(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Some thing bad happend.");
		return ResponseEntity
				.status(500)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problemDetail);
	}

	private static String buildMessage(final Map<String, Object> ea) {
		final StringBuilder message = new StringBuilder().append((String) ea.get("message"));
		final String ex = (String) ea.get("exception");
		if (MethodArgumentNotValidException.class.getName().equals(ex)) {
			final Object c = ea.get("errors");
			message.append(" ").append(c);
		}
		return message.toString();
	}

}
