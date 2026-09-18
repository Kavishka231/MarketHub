package com.markethub.review;
public class DuplicateReviewException extends RuntimeException{public DuplicateReviewException(){super("Customer has already reviewed this product");}}