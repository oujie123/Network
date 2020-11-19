package com.gacrnd.gcs.network.base;

import android.app.Application;

/**
 * @author Jack_Ou  created on 2020/11/19.
 */
public interface INetworkRequireInfo {
    String getAppVersionName();
    String getAppVersionCode();
    boolean isDebug();
    Application getApplication();
}
