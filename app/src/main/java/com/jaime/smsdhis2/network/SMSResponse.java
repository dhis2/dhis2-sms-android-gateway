package com.jaime.smsdhis2.network;

public class SMSResponse {

    private String httpStatus;
    private int httpStatusCode;
    private String status;
    private String message;

    public SMSResponse(String httpStatus, int httpStatusCode, String status, String message) {
        this.httpStatus = httpStatus;
        this.httpStatusCode = httpStatusCode;
        this.status = status;
        this.message = message;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}