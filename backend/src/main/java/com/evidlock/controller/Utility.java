package com.evidlock.controller;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class Utility {

    private static final String uploadDir = "backend/src/main/resources/files";

    public static String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    public static String decodeBase64(String value) {
        return new String(Base64.getDecoder().decode(value.getBytes()));
    }

    public static String getToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth == null) return "";
        if (auth.startsWith("Basic ")) {
            return auth.substring("Basic ".length()).trim();
        }
        if (auth.startsWith("Bearer ")) {
            return auth.substring("Bearer ".length()).trim();
        }
        return auth.trim();
    }

    public static String getTokenEmail(String token) {
        try {
            return decodeBase64(token).split(":tkn:")[0];
        } catch (Exception e) {
            return "";
        }
    }

    public static String getTokenPassword(String token) {
        try {
            return decodeBase64(token).split(":tkn:")[1];
        } catch (Exception e) {
            return "";
        }
    }

    public static String getTokenRole(String token) {
        try {
            return decodeBase64(token).split(":tkn:")[2];
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean checkPassword(String plainPw, User storedUser) {
        if (plainPw == null || storedUser == null || storedUser.getPassword() == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPw, storedUser.getPassword());
        } catch (Exception e) {
            return plainPw.equals(storedUser.getPassword());
        }
    }

    public static User getRequestUser(HttpServletRequest req) {
        String token = getToken(req);
        if (token.isEmpty()) return null;
        return DBManager.getInstance().getUserDao().findByToken(token);
    }

    public static String uploadFile(String prefix, MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        assert originalFileName != null;
        String extension = originalFileName.contains(".") ? originalFileName.substring(originalFileName.lastIndexOf(".")) : ".bin";
        String cleanPrefix = prefix.replaceAll("[^a-zA-Z0-9_-]", "_");
        String fileName = cleanPrefix + "_" + System.currentTimeMillis() + extension;

        File targetDir = new File(uploadDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        Path destination = Paths.get(uploadDir, fileName);
        Files.write(destination, file.getBytes());
        return uploadDir + "/" + fileName;
    }

    public static boolean deleteFile(String pathStr) {
        if (pathStr == null || pathStr.isBlank()) return false;
        try {
            Path path = Paths.get(pathStr);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    public static <T> T jsonToObject(MultipartFile file, Class<T> valueType) {
        try {
            byte[] fileBytes = file.getBytes();
            String jsonString = new String(fileBytes);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, valueType);
        } catch (IOException e) {
            return null;
        }
    }
}
