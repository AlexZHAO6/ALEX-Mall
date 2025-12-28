package com.alex.mallorder.enume;

public enum OrderStatusEnum {

    CREATE_NEW(0, "New Order, waiting for payment"),
    PAYED(1, "Paid"),
    SHIPPED(2, "Shipped"),
    RECEIVED(3, "Completed"),
    CANCELED(4, "Canceled"),
    CLOSED(5, "Closed");

    private final Integer code;
    private final String description;

    OrderStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
