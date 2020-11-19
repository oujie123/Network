package com.gacrnd.gcs.network.commoninterceptor;

import com.gacrnd.gcs.network.base.INetworkRequireInfo;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 将通用的请求头加到此处
 *
 * @author Jack_Ou  created on 2020/11/19.
 */
public class CommonRequestInterceptor implements Interceptor {

    private INetworkRequireInfo mRequiredInfo;

    public CommonRequestInterceptor(INetworkRequireInfo info) {
        this.mRequiredInfo = info;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("os","android");
        builder.addHeader("appVersion",mRequiredInfo.getAppVersionCode());
        return chain.proceed(builder.build());
    }
}
