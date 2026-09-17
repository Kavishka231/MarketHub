package com.markethub.address;

public class AddressOwnershipException extends RuntimeException {
    public AddressOwnershipException() { super("Address belongs to another customer"); }
}
