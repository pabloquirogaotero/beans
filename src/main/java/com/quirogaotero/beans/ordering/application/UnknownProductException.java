package com.quirogaotero.beans.ordering.application;

public class UnknownProductException extends RuntimeException {

    public UnknownProductException(String sku) { super("Unknown product: " + sku); }

}
