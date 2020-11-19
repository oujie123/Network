package com.gacrnd.gcs.customnetwork.bean;

/**
 * @author Jack_Ou  created on 2020/11/18.
 */
public class ResponseBean {

    private int error_code;
    private String result_msg;

    public int getErrorCode() {
        return error_code;
    }

    public void setErrorCode(int errorCode) {
        this.error_code = errorCode;
    }

    public String getResultMsg() {
        return result_msg;
    }

    public void setResultMsg(String msg) {
        this.result_msg = msg;
    }
}
