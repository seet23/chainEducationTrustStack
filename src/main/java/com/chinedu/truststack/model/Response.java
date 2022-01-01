package com.chinedu.truststack.model;

import java.lang.*;

public class Response {

    private int code;

    private String message = "";

    private Error error;

    public Response(int code, String message, Error error) {
        this.code = code;
        this.message = message;
        this.error = error;
    }

    public Response(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Response(int code) {
        this.code = code;
    }

    public Response(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
