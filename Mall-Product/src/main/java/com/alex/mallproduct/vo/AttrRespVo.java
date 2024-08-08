package com.alex.mallproduct.vo;

import lombok.Data;
import reactor.core.Scannable;

@Data
public class AttrRespVo extends AttrVo {
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;
}
