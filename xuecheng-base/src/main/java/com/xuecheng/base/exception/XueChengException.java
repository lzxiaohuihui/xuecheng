package com.xuecheng.base.exception;

public class XueChengException extends RuntimeException{
    private static final long serialVersionUID = 5565760508056698922L;

    private String errMessage;

    public XueChengException() {
        super();
    }

    public XueChengException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XueChengException(errMessage);
    }
}
