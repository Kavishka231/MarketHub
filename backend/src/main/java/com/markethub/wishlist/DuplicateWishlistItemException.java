package com.markethub.wishlist;
public class DuplicateWishlistItemException extends RuntimeException{public DuplicateWishlistItemException(){super("Product is already saved in the wishlist");}}