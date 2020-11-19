package com.gacrnd.gcs.customnetwork.http;

import java.io.InputStream;

/**
 * 网络中最顶层的接口
 *
 * @author Jack_Ou  created on 2020/11/18.
 */
public interface IHttpListener {

    void onSuccess(InputStream inputStream);
    void onFailure();
}
