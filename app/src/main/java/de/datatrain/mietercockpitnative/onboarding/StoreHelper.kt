package de.datatrain.mietercockpitnative.onboarding

import android.content.Context
import com.sap.cloud.mobile.flow.onboarding.ClientPolicy
import com.sap.cloud.mobile.flow.onboarding.OnboardingParameters
import com.sap.cloud.mobile.flow.onboarding.SecureStore
import com.sap.cloud.mobile.flow.onboarding.SecureStoreException
import com.sap.cloud.mobile.flow.onboarding.storemanager.PasscodePolicyStoreStep
import com.sap.cloud.mobile.flow.onboarding.storemanager.StoreManagerStep
import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator
import com.sap.cloud.mobile.foundation.common.EncryptionError
import com.sap.cloud.mobile.foundation.common.EncryptionUtil
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar
import com.sap.cloud.mobile.foundation.securestore.FileMissingException
import com.sap.cloud.mobile.foundation.securestore.OpenFailureException
import com.sap.cloud.mobile.foundation.securestore.SecureKeyValueStore
import com.sap.cloud.mobile.odata.OnlineODataProvider
import datatrain.mietercockpitnative.tp_srv.TP_SRV
import de.datatrain.mietercockpitnative.MyApplication
import okhttp3.OkHttpClient
import java.net.MalformedURLException
import java.util.Date
import java.util.GregorianCalendar
import org.slf4j.LoggerFactory

object StoreHelper {
    private val IS_ONBOARDED_KEY = "isOnboarded"
    private val EULA_VERSION = "EULA_VERSION"

    private var policyStore: SecureKeyValueStore? = null
    private var authStore: SecureKeyValueStore? = null

    private val LOGGER = LoggerFactory.getLogger(MyApplication::class.java)

    private val serviceURL: String = "https://mobile-a71f9a2af.hana.ondemand.com";
    private val appID: String = "de.datatrain.mietercockpitnative";
    private val connectionID: String = "DT1TPOdata";
    private var myDataProvider: OnlineODataProvider? = null

    private fun policyStoreNullCheck(context: Context) {
        if (policyStore == null) {
            policyStore = SecureKeyValueStore(context, PasscodePolicyStoreStep.DATABASE)
        }
    }

    private fun authStoreNullCheck(context: Context) {
        if (authStore == null) {
            authStore = SecureKeyValueStore(context, StoreManagerStep.DATABASE)
        }
    }

    @Throws(EncryptionError::class, OpenFailureException::class)
    private fun policyStoreOpenCheck() {
        if (!policyStore!!.isOpen) {
            policyStore!!.open(EncryptionUtil.getEncryptionKey(PasscodePolicyStoreStep.ALIAS))
        }
    }

    @Throws(EncryptionError::class, OpenFailureException::class)
    private fun authStoreOpenCheck() {
        if (!authStore!!.isOpen) {
            authStore!!.open(EncryptionUtil.getEncryptionKey(StoreManagerStep.ALIAS))
        }
    }

    fun reset() {
        if (policyStore != null) {
            policyStore!!.close()
            policyStore = null
        }
    }

    fun setIsOnboarded(context: Context, isOnboarded: Boolean) {
        // places the isOnboarded variable into the policy store
        try {
            policyStoreNullCheck(context)
            policyStoreOpenCheck()
            policyStore!!.put(IS_ONBOARDED_KEY, isOnboarded)
        } catch (encryptionError: EncryptionError) {
            encryptionError.printStackTrace()
        } catch (e: OpenFailureException) {
            e.printStackTrace()
        }

    }

    fun getService(deviceID: String) : TP_SRV? {

        var myOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(AppHeadersInterceptor(appID, deviceID, "1.0"))
            .authenticator(BasicAuthDialogAuthenticator())
            .cookieJar(WebkitCookieJar())
            .build()

        myDataProvider = OnlineODataProvider(
            "ESPMContainer",
            "$serviceURL/$connectionID", myOkHttpClient
        )

        return TP_SRV(myDataProvider!!)
    }

    fun isOnboarded(context: Context): Boolean {
        // grabs isOnboarded variable from the policy store if possible and stores it in onboardFinished
        var onboardFinished: Boolean? = false
        try {
            policyStoreNullCheck(context)
            policyStoreOpenCheck()
            onboardFinished = policyStore!!.getBoolean(IS_ONBOARDED_KEY)
            // LOGGER.debug("isOnboarded value = " + onboardFinished!!)
        } catch (encryptionError: EncryptionError) {
            encryptionError.printStackTrace()
        } catch (e: OpenFailureException) {
            e.printStackTrace()
        }

        return onboardFinished ?: false
    }

    fun logStoreValues(store: SecureStore) {
        LOGGER.debug("in logStoreValues")
        try {
            val keys = store.keys()
            for (key in keys) {
                if (store.get<Any>(key).javaClass == OnboardingParameters::class.java) {
                    val onboardingParameters = store.get<Any>(key) as OnboardingParameters
                    try {
                        onboardingParameters.build()
                        val settingsParameters = onboardingParameters.settingsParameters
                        // logs values from configuration_key
                        LOGGER.debug(key + " - applicationID: " + onboardingParameters.applicationId)
                        LOGGER.debug(key + " - OAuth2Configuration: " + onboardingParameters.oAuth2Configuration)
                        LOGGER.debug(key + " - applicationVersion: " + settingsParameters.applicationVersion)
                        LOGGER.debug(key + " - backendURL: " + settingsParameters.backendUrl)
                        LOGGER.debug(key + " - deviceID: " + settingsParameters.deviceId)
                        LOGGER.debug(key + " - pushService: " + settingsParameters.pushService)
                    } catch (e: MalformedURLException) {
                        LOGGER.debug("Unable to build the settings parameters. " + e.message)
                    }

                } else if (store.get<Any>(key).javaClass == ClientPolicy::class.java) {
                    // grabs values belonging to policies_key
                    val clientPolicy = store.get<Any>("policies_key") as ClientPolicy
                    val passcodePolicy = clientPolicy.passcodePolicy
                    LOGGER.debug(key + " - ExpirationTimeFrame: " + clientPolicy.expirationTimeFrame)
                    LOGGER.debug(key + " - LockTimeout: " + clientPolicy.lockTimeout)
                    LOGGER.debug(key + " - LogLevel: " + clientPolicy.logLevel)
                    LOGGER.debug(key + " - isLogEnabled: " + clientPolicy.isLogEnabled)
                    LOGGER.debug(key + " - isPasscodePolicyEnabled: " + clientPolicy.isPasscodePolicyEnabled)
                    if (passcodePolicy != null) {
                        LOGGER.debug("$key - PasscodePolicy: $passcodePolicy")
                    }
                } else if (key.contains("https://")) {
                    // gets credentials
                    val link = store.get<Array<String>>(key)
                    LOGGER.debug("Credentials follow, password not logged")
                    LOGGER.debug(key + " : " + link[0])
                } else if (key == "KEY_PASSCODE_TIMESTAMP") {
                    // converts date objects to readable format
                    val date = Date((store.get<Any>("KEY_PASSCODE_TIMESTAMP") as GregorianCalendar).timeInMillis)
                    LOGGER.debug("$key - $date")
                } else {
                    LOGGER.debug(key + " : " + store.get<Any>(key))
                }
            }
        } catch (sse: SecureStoreException) {
            LOGGER.debug("Secure Store Exception: " + sse.message)
        } catch (fme: FileMissingException) {
            LOGGER.debug("Store does not exist.  " + fme.message)
        }

    }
}