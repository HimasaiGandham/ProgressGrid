package com.progressgrid.dao;

import java.util.List;

public class Habit {
    private int id;
    private String name;
    private List<Integer> completions;

    public Habit(int id, String name, List<Integer> completions) {
        this.id = id;
        this.name = name;
        this.completions = completions;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Integer> getCompletions() { return completions; }
    public void setCompletions(List<Integer> completions) { this.completions = completions; }
}
