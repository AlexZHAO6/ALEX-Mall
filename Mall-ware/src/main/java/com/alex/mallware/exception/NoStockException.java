package com.alex.mallware.exception;

public class NoStockException extends RuntimeException{
    private Long skuId;
    public NoStockException(Long skuId){
        super("No stock available for: " + skuId);
    }
    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
