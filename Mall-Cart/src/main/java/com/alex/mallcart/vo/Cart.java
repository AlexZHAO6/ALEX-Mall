package com.alex.mallcart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class Cart {
    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        if(this.items != null && this.items.size() > 0){
            int sum = 0;
            for (CartItem item : this.items) {
                sum += item.getCount();
            }
            countNum = sum;
        }
        return countNum;
    }

    public Integer getCountType() {
        if(this.items != null && this.items.size() > 0){
            countType = this.items.size();
        }
        return countType;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal total = new BigDecimal("0.00");
        if(this.items != null && this.items.size() > 0){
            for (CartItem item : this.items) {
                if(item.getCheck()){
                    total = total.add(item.getTotalPrice());
                }
            }
        }

        totalAmount = total.subtract(this.reduce);
        return totalAmount;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }

    private List<CartItem> items;
    private Integer countNum;
    private Integer countType;
    private BigDecimal totalAmount;
    private BigDecimal reduce = new BigDecimal("0.00");
}
