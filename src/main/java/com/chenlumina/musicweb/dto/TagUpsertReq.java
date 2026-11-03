package com.chenlumina.musicweb.dto;

/** 标签新增/编辑请求：sortOrder 可省略 */
public record TagUpsertReq(String name, Integer sortOrder) {
}
