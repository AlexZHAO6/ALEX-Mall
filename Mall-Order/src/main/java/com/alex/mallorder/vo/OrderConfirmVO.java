package com.alex.mallorder.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class OrderConfirmVO {
    @Setter
    @Getter
    private List<MemberAddressVO> address;
    @Setter
    @Getter
    private List<OrderItemVO> items;
    @Setter
    @Getter
    private Integer memberPoints;
    private BigDecimal totalAmount;
    @Setter
    @Getter
    //avoid duplicate submissions
    private String orderToken;
    @Setter
    @Getter
    private Map<Long, Boolean> stocks;

    public BigDecimal getTotalAmount() {
        if(items != null){
            for (OrderItemVO item : items) {
                if(item.getTotalPrice() != null){
                    totalAmount = (totalAmount == null ? BigDecimal.ZERO : totalAmount).add(item.getTotalPrice());
                }
            }
        }
        return totalAmount;
    }

    private BigDecimal payAmount;
    public BigDecimal getPayAmount() {
        if(items != null){
            for (OrderItemVO item : items) {
                if(item.getTotalPrice() != null){
                    payAmount = (payAmount == null ? BigDecimal.ZERO : payAmount).add(item.getTotalPrice());
                }
            }
        }

        return payAmount;
    }
}
