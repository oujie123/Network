package com.gacrnd.gcs.network.observer;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author Jack_Ou  created on 2020/11/19.
 */
public abstract class BaseObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        onFailure(e);
    }

    @Override
    public void onComplete() {

    }

    /**
     *  请求成功，回调子类
     */
    public abstract void onSuccess(T t);
    public abstract void onFailure(Throwable e);
}
