package com.alex.mallware.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseDoneVO {
    @NotNull
    private Long purchaseId;

    private List<ItemDoneVO> items;
}
