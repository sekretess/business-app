package io.sekretess.dto;

import jakarta.validation.constraints.NotBlank;

public class MessageDTO {

    @NotBlank(message = "Message text cannot be blank")
    private String text;

    @NotBlank(message = "Consumer cannot be blank")
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
