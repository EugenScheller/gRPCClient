package com.eugen.sandbox.jpa.h2.dto;

import java.io.Serializable;

public class ResponseDTO implements Serializable {
    private int processedTutorials;
    private int notProcessedTutorials;
    private int duration;
    private String response;

    public ResponseDTO() {
    }

    public ResponseDTO(int processedTutorials, int notProcessedTutorials, int duration, String response) {
        this.processedTutorials = processedTutorials;
        this.notProcessedTutorials = notProcessedTutorials;
        this.duration = duration;
        this.response = response;
    }



    public int getProcessedTutorials() {
        return processedTutorials;
    }

    public void setProcessedTutorials(int processedTutorials) {
        this.processedTutorials = processedTutorials;
    }

    public int getNotProcessedTutorials() {
        return notProcessedTutorials;
    }

    public void setNotProcessedTutorials(int notProcessedTutorials) {
        this.notProcessedTutorials = notProcessedTutorials;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
