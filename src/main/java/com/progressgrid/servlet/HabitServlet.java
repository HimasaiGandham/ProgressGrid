package com.progressgrid.servlet;

import com.google.gson.Gson;
import com.progressgrid.dao.Habit;
import com.progressgrid.dao.HabitDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/habits")
public class HabitServlet extends HttpServlet {
    
    private HabitDAO habitDAO = new HabitDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        // CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");

        List<Habit> habits = habitDAO.getAllHabits();
        String json = gson.toJson(habits);

        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");
        
        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ToggleRequest request = gson.fromJson(body, ToggleRequest.class);
        
        habitDAO.toggleCompletion(request.habitId, request.day, request.isCompleted);
        
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private static class ToggleRequest {
        int habitId;
        int day;
        boolean isCompleted;
    }
}
