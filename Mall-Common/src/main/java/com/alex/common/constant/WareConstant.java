package com.alex.common.constant;

public class WareConstant {
    public enum PurchaseStatusEnum{
        CREATED(0, "created"),
        ASSIGNED(1, "assigned"),
        RECEIVED(2, "received"),
        FINISHED(3, "finished"),
        ERROR(4, "error");

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        private int code;
        private String msg;
        PurchaseStatusEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }
    }

    public enum PurchaseDetailStatusEnum{
        CREATED(0, "created"),
        ASSIGNED(1, "assigned"),
        BUYING(2, "buying"),
        FINISHED(3, "finished"),
        ERROR(4, "buy failed");

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        private int code;
        private String msg;
        PurchaseDetailStatusEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }
    }
}
