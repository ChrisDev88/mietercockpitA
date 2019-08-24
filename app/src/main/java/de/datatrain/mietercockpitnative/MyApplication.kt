package de.datatrain.mietercockpitnative

import android.app.Application
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler
import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar
import com.sap.cloud.mobile.odata.OnlineODataProvider
import datatrain.mietercockpitnative.tp_srv.TP_SRV
import okhttp3.OkHttpClient


class MyApplication : Application() {

    private val serviceURL: String = "https://mobile-a71f9a2af.hana.ondemand.com";
    private val appID: String = "de.datatrain.mietercockpitnative";
    private val connectionID: String = "DT1TPOdata";
    private var deviceID: String = "";
    var tp_srv: TP_SRV? = null
    private var myDataProvider: OnlineODataProvider? = null

    override fun onCreate() {
        super.onCreate()

        deviceID = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        var myOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(AppHeadersInterceptor(appID, deviceID, "1.0"))
            .authenticator(BasicAuthDialogAuthenticator())
            .cookieJar(WebkitCookieJar())
            .build()

        myDataProvider = OnlineODataProvider(
            "ESPMContainer",
            "$serviceURL/$connectionID", myOkHttpClient
        )
        tp_srv = TP_SRV(myDataProvider!!)


        registerActivityLifecycleCallbacks(AppLifecycleCallbackHandler.getInstance())
    }
}
