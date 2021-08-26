package org.sunbit.addressbook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceNotFoundException extends ResponseStatusException {

    public ResourceNotFoundException(String resourceName) {
        super(HttpStatus.NOT_FOUND, resourceName);
    }
}
