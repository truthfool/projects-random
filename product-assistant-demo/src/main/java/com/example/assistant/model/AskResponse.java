package com.example.assistant.model;

public class AskResponse {
    private boolean allowed;
    private String answer;
    private String reason;

    public AskResponse() {
    }

    public AskResponse(boolean allowed, String answer, String reason) {
        this.allowed = allowed;
        this.answer = answer;
        this.reason = reason;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
