package com.chenlumina.musicweb.entity;

/** 标签实体：标签名唯一，排序值用于前台筛选条展示顺序 */
public class Tag {

    private Long id;
    private String name;
    private int sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}