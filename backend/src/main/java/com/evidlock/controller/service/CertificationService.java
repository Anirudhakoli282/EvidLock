package com.evidlock.controller.service;

import com.evidlock.controller.CsvReader;
import com.evidlock.controller.ExcelReader;
import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.CertificationTraining;
import com.evidlock.persistence.model.User;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://*.vercel.app", "https://*.onrender.com"}, allowCredentials = "true")
public class CertificationService {

    @GetMapping(value = {"/trainings", "/corsi"})
    public ResponseEntity<List<CertificationTraining>> getTrainings(HttpServletRequest req) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(DBManager.getInstance().getCertificationTrainingDao().getAll(), HttpStatus.OK);
    }

    @GetMapping(value = {"/training/{id}", "/corso"})
    public ResponseEntity<CertificationTraining> getTrainingById(HttpServletRequest req, @PathVariable(required = false) Long id, @RequestParam(required = false) Long idParam) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Long searchId = id != null ? id : idParam;
        CertificationTraining t = DBManager.getInstance().getCertificationTrainingDao().findById(searchId);
        return t != null ? new ResponseEntity<>(t, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = {"/personnel", "/dipendenti"})
    public ResponseEntity<List<User>> getPersonnel(HttpServletRequest req, @RequestParam(required = false) String agencyCode) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        String agency = (agencyCode != null && !agencyCode.isBlank()) ? agencyCode : user.getAgencyCode();
        return new ResponseEntity<>(DBManager.getInstance().getUserDao().findByAgency(agency), HttpStatus.OK);
    }

    @GetMapping(value = {"/personnel/{id}", "/dipendente"})
    public ResponseEntity<User> getPersonnelById(HttpServletRequest req, @PathVariable(required = false) Long id, @RequestParam(required = false) Long idParam) {
        User user = Utility.getRequestUser(req);
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Long searchId = id != null ? id : idParam;
        User u = DBManager.getInstance().getUserDao().findById(searchId);
        return u != null ? new ResponseEntity<>(u, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/personnel/import-csv")
    public ResponseEntity<String> importCsvPersonnel(
            HttpServletRequest req,
            @RequestParam("file") MultipartFile file,
            @RequestParam("agencyCode") String agencyCode) {
        User user = Utility.getRequestUser(req);
        if (user == null || !"SUPERVISOR".equalsIgnoreCase(user.getRole())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            List<User> list = CsvReader.csvFileToUsers(file, agencyCode);
            for (User u : list) {
                DBManager.getInstance().getUserDao().insert(u);
            }
            return new ResponseEntity<>("Imported " + list.size() + " personnel members", HttpStatus.OK);
        } catch (IOException | CsvValidationException e) {
            return new ResponseEntity<>("CSV parsing error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/personnel/import-excel")
    public ResponseEntity<String> importExcelPersonnel(
            HttpServletRequest req,
            @RequestParam("file") MultipartFile file,
            @RequestParam("agencyCode") String agencyCode) {
        User user = Utility.getRequestUser(req);
        if (user == null || !"SUPERVISOR".equalsIgnoreCase(user.getRole())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            List<User> list = ExcelReader.excelFileToUsers(file, agencyCode);
            for (User u : list) {
                DBManager.getInstance().getUserDao().insert(u);
            }
            return new ResponseEntity<>("Imported " + list.size() + " personnel members", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Excel parsing error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
