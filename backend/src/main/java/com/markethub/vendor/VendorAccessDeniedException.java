package com.markethub.vendor;

public class VendorAccessDeniedException extends RuntimeException {

    public VendorAccessDeniedException(String message) {
        super(message);
    }
}
