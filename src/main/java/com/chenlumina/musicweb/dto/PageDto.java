package com.chenlumina.musicweb.dto;

import java.util.List;

/** 统一分页结构：参数 page/size，返回 total（开发手册 7.1） */
public record PageDto<T>(List<T> list, long total, int page, int size) {
}
