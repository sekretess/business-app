package io.sekretess.controller;

import io.sekretess.dto.AdsMessageDTO;
import io.sekretess.dto.MessageDTO;
import io.sekretess.service.SekretessBusinessService;
import io.sekretess.util.MessageType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business")
public class SekretessBusinessController {

    private SekretessBusinessService sekretessBusinessService;

    public SekretessBusinessController(SekretessBusinessService sekretessBusinessService) {
        this.sekretessBusinessService = sekretessBusinessService;
    }

    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO message) {
        message.setType(MessageType.PRIVATE.name());
        this.sekretessBusinessService.handleSendMessage(message);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/ads/messages")
    public ResponseEntity<String> sendAdsMessage(@RequestBody AdsMessageDTO message) {
        this.sekretessBusinessService.handleSendAdsMessage(message);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @DeleteMapping("/sessions/users/{userName}")
    public ResponseEntity<String> deleteSession(@PathVariable("userName") String user) {
        this.sekretessBusinessService.deleteSession(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
