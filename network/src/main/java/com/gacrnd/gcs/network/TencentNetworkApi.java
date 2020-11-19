package com.gacrnd.gcs.network;

import com.gacrnd.gcs.network.base.NetworkApi;
import com.gacrnd.gcs.network.beans.TecentBaseResponse;
import com.gacrnd.gcs.network.errorhandler.ExceptionHandle;
import com.gacrnd.gcs.network.utils.TecentUtil;

import java.io.IOException;

import io.reactivex.functions.Function;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 腾讯的域名和接口
 * 也可以是讯飞的域名和接口
 *
 * @author Jack_Ou  created on 2020/11/19.
 */
public class TencentNetworkApi extends NetworkApi {

    public static TencentNetworkApi mInstance = new TencentNetworkApi();

    private TencentNetworkApi(){}

    public static TencentNetworkApi getInstance() {
        return mInstance;
    }

    public <T> T getService(Class<T> service) {
        return getInstance().getRetrofit(service).create(service);
    }

    // 腾讯系api专门的请求头
    @Override
    protected Interceptor getInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                String timeStr = TecentUtil.getTimeStr();
                Request.Builder builder = chain.request().newBuilder();
                builder.addHeader("Source", "source");
                builder.addHeader("Authorization", TecentUtil.getAuthorization(timeStr));
                builder.addHeader("Date", timeStr);
                return chain.proceed(builder.build());
            }
        };
    }

    @Override
    protected <T> Function<T, T> getAppErrorHandler() {
        return new Function<T, T>() {
            @Override
            public T apply(T response) throws Exception {
                //response中code码不会0 出现错误
                if (response instanceof TecentBaseResponse && ((TecentBaseResponse) response).showapiResCode != 0) {
                    ExceptionHandle.ServerException exception = new ExceptionHandle.ServerException();
                    exception.code = ((TecentBaseResponse) response).showapiResCode;
                    exception.message = ((TecentBaseResponse) response).showapiResError != null ? ((TecentBaseResponse) response).showapiResError : "";
                    throw exception;
                }
                return response;
            }
        };
    }

    @Override
    public String getFormal() {
        return "http://service-o5ikp40z-1255468759.ap-shanghai.apigateway.myqcloud.com/";
    }

    @Override
    public String getTest() {
        return "http://service-o5ikp40z-1255468759.ap-shanghai.apigateway.myqcloud.com/";
    }
}
