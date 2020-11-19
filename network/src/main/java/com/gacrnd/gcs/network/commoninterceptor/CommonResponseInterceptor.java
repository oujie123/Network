package com.gacrnd.gcs.network.commoninterceptor;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 请求耗时，请求次数  用于打点
 *
 * @author Jack_Ou  created on 2020/11/19.
 */
public class CommonResponseInterceptor implements Interceptor {

    private static final String TAG = "ResponseInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        long requestTimeStamp = System.currentTimeMillis();
        Response response = chain.proceed(chain.request());
        Log.d(TAG,"request time --> " + (System.currentTimeMillis() - requestTimeStamp));
        return response;
    }
}
