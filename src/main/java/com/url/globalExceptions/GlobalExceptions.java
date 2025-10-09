package com.url.globalExceptions;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.url.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptions {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptions.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<HashMap<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
		HashMap<String, String> errors = new HashMap<String, String>();

		ex.getBindingResult().getFieldErrors().forEach(err -> {
			errors.put(err.getField(), err.getDefaultMessage());
		});

		ApiResponse<HashMap<String, String>> response = new ApiResponse<>("Invalid input", false, errors);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<HashMap<String, String>>> validationError(
			jakarta.validation.ConstraintViolationException ex) {
		HashMap<String, String> errors = new HashMap<String, String>();

		ex.getConstraintViolations().forEach(err -> {
			errors.put(err.getPropertyPath().toString(), err.getMessage());
		});
		ApiResponse<HashMap<String, String>> response = new ApiResponse<>("Invalid input", false, errors);

		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

	}

//	Handling DAO related Error
	@ExceptionHandler(exception = DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<HashMap<String, String>>> handleDataIntegrityViolationExceptionErrors(
			DataIntegrityViolationException ex) {

		HashMap<String, String> errors = new HashMap<String, String>();

		errors.put("error", ex.getMostSpecificCause().getMessage());

		logger.error("ERROR - " + errors.toString());
		ApiResponse<HashMap<String, String>> response = new ApiResponse<>("Invalid input", false, errors);

		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

//	Handling RunTime Exceptions related Error
	@ExceptionHandler(exception = RuntimeException.class)
	public ResponseEntity<ApiResponse<String>> handleRuntimeExceptionErrors(RuntimeException ex) {

		HashMap<String, String> errors = new HashMap<String, String>();

		errors.put("error", ex.getMessage());
		logger.error("ERROR - " + errors.toString());
		ApiResponse<String> response = new ApiResponse<>("Something went wrong", false, null);

		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
