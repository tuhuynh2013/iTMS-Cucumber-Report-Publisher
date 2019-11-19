package io.jenkins.plugins.rest;

public class StandardResponse {

    private int code;
    private String type;
    private String message;

    public StandardResponse() {

    }

    public StandardResponse(int code, String type, String message) {
        this.code = code;
        this.type = type;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return "Standard response: " + this.code + " - " + this.message;
    }

}
