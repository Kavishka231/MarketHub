package com.markethub.vendor;

public class DuplicateVendorApplicationException extends RuntimeException {

    public DuplicateVendorApplicationException() {
        super("Vendor application already exists");
    }
}
