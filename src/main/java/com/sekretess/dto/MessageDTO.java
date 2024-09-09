package com.sekretess.dto;

import org.springframework.stereotype.Component;

@Component
public class MessageDTO {

    private String text;
    private String consumer;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
}
