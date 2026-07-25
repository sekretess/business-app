package io.sekretess.controller;

import io.sekretess.dto.AdsMessageDTO;
import io.sekretess.dto.MessageDTO;
import io.sekretess.service.SekretessBusinessService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/business")
public class SekretessBusinessController {

    private final SekretessBusinessService sekretessBusinessService;

    public SekretessBusinessController(SekretessBusinessService sekretessBusinessService) {
        this.sekretessBusinessService = sekretessBusinessService;
    }

    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@Valid @RequestBody MessageDTO message) {
        this.sekretessBusinessService.handleSendMessage(message);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/messages/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("consumer") String consumer) {
        this.sekretessBusinessService.handleSendFile(file, consumer);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/ads/messages")
    public ResponseEntity<String> sendAdsMessage(@Valid @RequestBody AdsMessageDTO message) {
        this.sekretessBusinessService.handleSendAdsMessage(message);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
