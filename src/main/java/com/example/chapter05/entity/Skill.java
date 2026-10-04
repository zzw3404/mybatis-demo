package com.example.chapter05.entity;

import java.io.Serializable;
import java.util.List;

/** 技能实体，对应 skill 表。 */
public class Skill implements Serializable {
    private Integer id;
    private String name;
    private String description;
    private List<Emp> emps;

    public Skill() { }
    public Skill(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Emp> getEmps() { return emps; }
    public void setEmps(List<Emp> emps) { this.emps = emps; }
    @Override public String toString() {
        return "Skill{id=" + id + ", name='" + name + '\'' + ", description='" + description + '\'' + '}';
    }
}
