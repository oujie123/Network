package com.gacrnd.gcs.customnetwork.http;

/**
 * @author Jack_Ou  created on 2020/11/18.
 */
public interface IHttpRequst {
    void setUrl(String url);
    void setParams(byte[] params);
    void execute();
    void setListener(IHttpListener listener);
}
