package com.evidlock.controller.service;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
<br>import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Lightweight health-check servlet.
 * GET /api/health → 200 OK {"status":"UP"}
 * Used by Vercel cron and Render to keep the backend alive (free tier).
 */
@WebServlet(name = "HealthService", urlPatterns = {"/api/health"})
public class HealthService extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("{\"status\":\"UP\",\"service\":\"EvidLock Backend\"}");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
