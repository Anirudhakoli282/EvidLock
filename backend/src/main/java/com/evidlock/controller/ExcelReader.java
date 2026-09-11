package com.evidlock.controller;

import com.evidlock.persistence.model.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    public static List<User> excelFileToUsers(MultipartFile multipartFile, String agencyCode) throws IOException {
        List<User> users = new ArrayList<>();

        try (InputStream inputStream = multipartFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Skip header
                }

                User user = createUserFromRow(row, agencyCode);
                if (user != null) {
                    users.add(user);
                }
            }
        }

        return users;
    }

    private static User createUserFromRow(Row row, String agencyCode) {
        try {
            User user = new User();
            user.setBadgeNumber(getStringCellValue(row.getCell(0)));
            user.setName(getStringCellValue(row.getCell(1)));
            user.setEmail(getStringCellValue(row.getCell(2)));
            String role = getStringCellValue(row.getCell(3));
            user.setRole(role != null ? role.toUpperCase() : "INVESTIGATOR");
            user.setPhone(getStringCellValue(row.getCell(4)));
            user.setAgencyCode(agencyCode);
            user.setPassword("Web2324.");
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getStringCellValue(Cell cell) {
        if (cell != null) {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
                default -> null;
            };
        }
        return null;
    }
}
