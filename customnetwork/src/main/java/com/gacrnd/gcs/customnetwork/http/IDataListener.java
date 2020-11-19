package com.gacrnd.gcs.customnetwork.http;

/**
 * 给上层用户用的
 *
 * @author Jack_Ou  created on 2020/11/18.
 */
public interface IDataListener<T> {

    void onSuccess(T t);
    void onFailure();
}
