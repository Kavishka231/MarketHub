package com.markethub.vendor;

public class InvalidVendorStatusException extends RuntimeException {

    public InvalidVendorStatusException(VendorStatus status) {
        super("Vendor application cannot be updated from status " + status);
    }
}
