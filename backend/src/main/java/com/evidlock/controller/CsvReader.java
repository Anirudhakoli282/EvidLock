package com.evidlock.controller;

import com.evidlock.persistence.model.User;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static List<User> csvFileToUsers(MultipartFile file, String agencyCode) throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<User> users = new ArrayList<>();
            String[] line;
            reader.skip(1); // Skip header row

            while ((line = reader.readNext()) != null) {
                User user = parseCsvLine(line, agencyCode);
                if (user != null) {
                    users.add(user);
                }
            }

            return users;
        }
    }

    private static User parseCsvLine(String[] line, String agencyCode) {
        try {
            // Expected columns: BadgeNumber, Name, Email, Role, Phone
            User user = new User();
            user.setBadgeNumber(line[0].trim());
            user.setName(line[1].trim());
            user.setEmail(line[2].trim());
            user.setRole(line.length > 3 ? line[3].trim().toUpperCase() : "INVESTIGATOR");
            user.setPhone(line.length > 4 ? line[4].trim() : "");
            user.setAgencyCode(agencyCode);
            user.setPassword("Web2324.");
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}
