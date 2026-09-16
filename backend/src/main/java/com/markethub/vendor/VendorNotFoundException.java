package com.markethub.vendor;

public class VendorNotFoundException extends RuntimeException {

    public VendorNotFoundException() {
        super("Vendor application not found");
    }
}
