package com.chenlumina.musicweb.dto;

/** 标签新增/编辑请求体：名称唯一，排序值非负 */
public class TagUpsertReq {

    private String name;
    private Integer sortOrder;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}