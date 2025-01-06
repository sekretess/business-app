package com.sekretess.dto;

import org.springframework.stereotype.Component;

@Component
public class AdsMessageDTO {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
