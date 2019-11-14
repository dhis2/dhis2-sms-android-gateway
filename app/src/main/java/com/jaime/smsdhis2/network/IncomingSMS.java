package com.jaime.smsdhis2.network;

public class IncomingSMS {

    private String text;
    private String originator;
    private String gatewayid;
    private String receiveddate;
    private String sentdate;
    private String smsenconding;

    public IncomingSMS(String text, String originator, String gatewayid, String receiveddate, String sentdate, String smsenconding) {
        this.text = text;
        this.originator = originator;
        this.gatewayid = gatewayid;
        this.receiveddate = receiveddate;
        this.sentdate = sentdate;
        this.smsenconding = smsenconding;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getGatewayid() {
        return gatewayid;
    }

    public void setGatewayid(String gatewayid) {
        this.gatewayid = gatewayid;
    }

    public String getReceiveddate() {
        return receiveddate;
    }

    public void setReceiveddate(String receiveddate) {
        this.receiveddate = receiveddate;
    }

    public String getSentdate() {
        return sentdate;
    }

    public void setSentdate(String sentdate) {
        this.sentdate = sentdate;
    }

    public String getSmsenconding() {
        return smsenconding;
    }

    public void setSmsenconding(String smsenconding) {
        this.smsenconding = smsenconding;
    }
}
