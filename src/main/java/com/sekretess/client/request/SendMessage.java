package com.sekretess.client.request;

public class SendMessage {
    private String text;
    private String sender;
    private String consumerExchange;

    public SendMessage(String text, String sender, String consumerExchange) {
        this.text = text;
        this.sender = sender;
        this.consumerExchange = consumerExchange;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getConsumerExchange() {
        return consumerExchange;
    }

    public void setConsumerExchange(String consumerExchange) {
        this.consumerExchange = consumerExchange;
    }
}
