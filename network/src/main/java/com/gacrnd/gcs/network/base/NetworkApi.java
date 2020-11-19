package com.gacrnd.gcs.network.base;

import com.gacrnd.gcs.network.commoninterceptor.CommonRequestInterceptor;
import com.gacrnd.gcs.network.commoninterceptor.CommonResponseInterceptor;
import com.gacrnd.gcs.network.environment.EnvironmentActivity;
import com.gacrnd.gcs.network.environment.IEnvironment;
import com.gacrnd.gcs.network.errorhandler.HttpErrorHandler;


import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Jack_Ou  created on 2020/11/19.
 */
public abstract class NetworkApi implements IEnvironment {

    private String mBaseUrl;
    private OkHttpClient mOkHttpClient;
    private static boolean mIsFormal = true;
    private HashMap<String, Retrofit> retrofitHashMap = new HashMap<>();

    private static INetworkRequireInfo mNetworkRequireInfo;

    public NetworkApi() {
        if (!mIsFormal) {
            mBaseUrl = getTest();
        }
        mBaseUrl = getFormal();
    }

    public static void init(INetworkRequireInfo info) {
        mNetworkRequireInfo = info;
        mIsFormal = EnvironmentActivity.isOfficialEnvironment(mNetworkRequireInfo.getApplication());
    }

    protected Retrofit getRetrofit(Class service) {
        Retrofit retrofit = retrofitHashMap.get(mBaseUrl + service.getName());
        if (retrofit != null) {
            return retrofit;
        }
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(mBaseUrl);
        builder.client(getOkHttpClient());
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        builder.addConverterFactory(GsonConverterFactory.create());
        retrofit = builder.build();
        retrofitHashMap.put(mBaseUrl + service.getName(), retrofit);
        return retrofit;
    }

    private OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
            if (getInterceptor() != null) {
                okHttpClient.addInterceptor(getInterceptor());
            }
            int cacheSize = 10 * 1024 * 1024;
            okHttpClient.cache(new Cache(mNetworkRequireInfo.getApplication().getCacheDir(), cacheSize));
            okHttpClient.addInterceptor(new CommonRequestInterceptor(mNetworkRequireInfo));
            okHttpClient.addInterceptor(new CommonResponseInterceptor());
            if (mNetworkRequireInfo != null && mNetworkRequireInfo.isDebug()) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                okHttpClient.addInterceptor(httpLoggingInterceptor);
            }
            mOkHttpClient = okHttpClient.build();
        }
        return mOkHttpClient;
    }

    public <T> ObservableTransformer<T, T> applySchedulers(final Observer<T> observer) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                Observable<T> observable = (Observable<T>)upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(getAppErrorHandler())
                        .onErrorResumeNext(new HttpErrorHandler<T>());
                observable.subscribe(observer);
                return observable;
            }
        };
    }

    protected abstract Interceptor getInterceptor();

    protected abstract <T> Function<T, T> getAppErrorHandler();
}
