package com.evidlock.controller.service;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.model.CustodyTransferRequest;
import com.evidlock.persistence.model.TransferStepUpDto;
import com.evidlock.persistence.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:8081", "http://127.0.0.1:8081"}, allowCredentials = "true")
public class CustodyTransferService {

    @GetMapping("/transfers/pending")
    public ResponseEntity<List<CustodyTransferRequest>> getPendingTransfers(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.getPendingTransfers();
    }

    @PostMapping("/transfers/initiate")
    public ResponseEntity<CustodyTransferRequest> initiateTransfer(
            HttpServletRequest req,
            @RequestBody TransferStepUpDto dto) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.initiateTransfer(dto);
    }

    @PostMapping("/transfers/accept")
    public ResponseEntity<CustodyTransferRequest> acceptTransfer(
            HttpServletRequest req,
            @RequestBody TransferStepUpDto dto) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.acceptTransfer(dto);
    }

    @PostMapping("/transfers/reject")
    public ResponseEntity<String> rejectTransfer(
            HttpServletRequest req,
            @RequestParam Long requestId,
            @RequestParam(required = false) String reason) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return user.rejectTransfer(requestId, reason);
    }
}
