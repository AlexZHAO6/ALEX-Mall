package com.alex.mallware.vo;

import lombok.Data;

@Data
public class ItemDoneVO {
    private Long itemId;
    private Integer status;
    private String reason;
}
