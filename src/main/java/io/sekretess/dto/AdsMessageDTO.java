package io.sekretess.dto;

import jakarta.validation.constraints.NotBlank;

public class AdsMessageDTO {

    @NotBlank(message = "Message text cannot be blank")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
