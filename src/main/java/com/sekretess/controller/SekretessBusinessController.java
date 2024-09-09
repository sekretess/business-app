package com.sekretess.controller;

import com.sekretess.dto.MessageDTO;
import com.sekretess.service.SekretessBusinessService;
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
        this.sekretessBusinessService.handleSendMessage(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @DeleteMapping("/sessions/users/{userName}")
    public ResponseEntity<String> deleteSession(@PathVariable("userName") String user) {
        this.sekretessBusinessService.deleteSession(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
