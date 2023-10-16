package com.ubiqube.juju.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class  JujuErrorResponse {


    private LocalDateTime timeStamp;
    private String statusCode ;
    private String errorMessage;
    private String path;

    public JujuErrorResponse(LocalDateTime timeStamp, String statusCode, String errorMessage, String path) {
        this.timeStamp = timeStamp;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.path = path;
    }
}
