package com.alex.mallproduct.exception;

import lombok.Data;

public enum BizCodeEmum {
    UNKNOW_EXCEPTION(10000, "unknow exception"),
    VALID_EXCEPTION(10001, "parameter valid exception");

    private int code;
    private String msg;
    BizCodeEmum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
