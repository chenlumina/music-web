package com.chenlumina.musicweb.dto;

/** 标签 id + 名称，后台歌单编辑回显用 */
public class IdName {

    private final Long id;
    private final String name;

    public IdName(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}