package com.chenlumina.musicweb.dto;

import java.util.List;

/** 统一分页结构：参数 page/size，返回 total（开发手册 7.1） */
public class PageDto<T> {

    private final List<T> list;
    private final long total;
    private final int page;
    private final int size;

    public PageDto(List<T> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<T> getList() { return list; }
    public long getTotal() { return total; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}