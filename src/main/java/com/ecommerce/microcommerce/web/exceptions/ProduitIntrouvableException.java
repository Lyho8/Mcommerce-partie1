package com.ecommerce.microcommerce.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProduitIntrouvableException extends RuntimeException {

	public ProduitIntrouvableException(String s) {
		super(s);
	}
}
