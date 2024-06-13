package com.example.samplecallapp;

public class UssdResponse {
    private String ussdResponse;
    private String ussdCode;
    private int subcriptionId;

    public UssdResponse(String ussdResponse,String ussdCode,int subscriptionId) {
        this.ussdResponse = ussdResponse;
        this.ussdCode = ussdCode;
        this.subcriptionId = subscriptionId;
    }

    public String getUssdResponse() {
        return ussdResponse;
    }
    public String getUssdCode() {
        return ussdCode;
    }
    public int getSubscriptionId() {
        return subcriptionId;
    }
}
