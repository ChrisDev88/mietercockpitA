package de.datatrain.mietercockpitnative.onboarding

//imports for entire Flows tutorial are added here
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings

import android.util.Log
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.sap.cloud.mobile.flow.Flow
import com.sap.cloud.mobile.flow.FlowActionHandler
import com.sap.cloud.mobile.flow.FlowContext
import com.sap.cloud.mobile.flow.FlowManagerService
import com.sap.cloud.mobile.flow.ServiceConnection
import com.sap.cloud.mobile.flow.Step
import com.sap.cloud.mobile.flow.onboarding.OnboardingContext
import com.sap.cloud.mobile.flow.onboarding.basicauth.BasicAuthStep
import com.sap.cloud.mobile.flow.onboarding.basicauth.BasicAuthStoreStep
import com.sap.cloud.mobile.flow.onboarding.eulascreen.EulaScreenStep
import com.sap.cloud.mobile.flow.onboarding.logging.LoggingStep
import com.sap.cloud.mobile.flow.onboarding.presenter.FlowPresentationActionHandlerImpl
import com.sap.cloud.mobile.flow.onboarding.storemanager.ChangePasscodeStep
import com.sap.cloud.mobile.flow.onboarding.storemanager.PasscodePolicyStoreStep
import com.sap.cloud.mobile.flow.onboarding.storemanager.SettingsDownloadStep
import com.sap.cloud.mobile.flow.onboarding.storemanager.SettingsStoreStep
import com.sap.cloud.mobile.flow.onboarding.storemanager.StoreManagerStep
import com.sap.cloud.mobile.flow.onboarding.welcomescreen.WelcomeScreenStep
import com.sap.cloud.mobile.flow.onboarding.welcomescreen.WelcomeScreenStoreStep
import com.sap.cloud.mobile.foundation.common.EncryptionError
import com.sap.cloud.mobile.foundation.common.SettingsParameters
import com.sap.cloud.mobile.foundation.configurationprovider.ConfigurationProvider
import com.sap.cloud.mobile.foundation.configurationprovider.JsonConfigurationProvider
import com.sap.cloud.mobile.foundation.logging.Logging
import com.sap.cloud.mobile.foundation.securestore.OpenFailureException
import com.sap.cloud.mobile.onboarding.launchscreen.LaunchScreenSettings
import com.sap.cloud.mobile.onboarding.qrcodereader.QRCodeConfirmSettings
import com.sap.cloud.mobile.onboarding.qrcodereader.QRCodeReaderSettings

import de.datatrain.mietercockpitnative.R
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.net.MalformedURLException

import ch.qos.logback.classic.Level
import com.sap.cloud.mobile.flow.onboarding.oauth.OAuthStep
import com.sap.cloud.mobile.flow.onboarding.oauth.OAuthStoreStep
import com.sap.cloud.mobile.flow.onboarding.saml.SamlStep
import okhttp3.OkHttpClient
import com.sap.cloud.mobile.foundation.authentication.OAuth2Configuration as OAuth2Configuration1

class LoginActivity : AppCompatActivity() {
    private var flowManagerService: FlowManagerService? = null
    private var flowContext: OnboardingContext? = null
    private val appID = "de.datatrain.mietercockpitnative"
    private val myLogUploadListener: Logging.UploadListener? = null
    private val settingsDownloadStep = SettingsDownloadStep()
    private val eulaScreenStep = EulaScreenStep()

    private var deviceID: String? = null
    private var settingsParameters: SettingsParameters? = null

    private val OAUTH_CLIENT_ID = "794d2747-18f4-46df-8665-a5b587ba9647"
    val TAG = "myDebuggingTag"
    val SERVICE_URL = "https://mobile-a71f9a2af.hana.ondemand.com"
    val CONNECTION_ID = "com.sap.edm.sampleservice.v2"
    private val AUTH_END_POINT = "https://oauthasservices-a71f9a2af.hana.ondemand.com/oauth2/api/v1/authorize"
    private val TOKEN_END_POINT = "https://oauthasservices-a71f9a2af.hana.ondemand.com/oauth2/api/v1/token"
    private val OAUTH_REDIRECT_URL = "https://oauthasservices-a71f9a2af.hana.ondemand.com"

    private var isOnboarded: Boolean = false

    internal var connection: ServiceConnection = object : ServiceConnection() {
        override fun onServiceConnected(className: ComponentName?, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            flowManagerService = (service as FlowManagerService.LocalBinder).service

            if(isOnboarded){
                startRestoreFlow()
            }else{
                startOnboardingFlow()
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            flowManagerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.hide()
        setContentView(R.layout.splash_screen)

        initializeLogging(Level.TRACE)
        LOGGER.debug("Log level in onCreate is: " + Logging.getRootLogger().level.toString())

        // grab the isOnboarded value from the StoreHelper/policies store
        isOnboarded = StoreHelper.isOnboarded(application);
        LOGGER.debug("onCreate: isOnboarded = " + isOnboarded);

        // Uncomment the below line to not show the passcode screen.  Requires that Passcode Policy is disabled in the management cockpit
        //settingsDownloadStep.passcodePolicy = null;
        eulaScreenStep.eulaVersion = "0.1"

        this.bindService(
            Intent(this, FlowManagerService::class.java),
            this.connection, Activity.BIND_AUTO_CREATE
        )
    }

    private fun initializeLogging(level: Level) {
        val cb = Logging.ConfigurationBuilder()
            .logToConsole(true)
            .initialLevel(level)  // levels in order are all, trace, debug, info, warn, error, off
        Logging.initialize(this.applicationContext, cb)
    }

    private fun startOnboardingFlow() {
        flowContext = OnboardingContext()

        // setting details on the welcome screen and store
        val welcomeScreenStep = WelcomeScreenStep()
        welcomeScreenStep.setApplicationId(appID)
        welcomeScreenStep.setApplicationVersion("1.0")
        welcomeScreenStep.setDeviceId(
            android.provider.Settings.Secure.getString(
                this.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        )

        val lss = LaunchScreenSettings()
        lss.isDemoAvailable = false
        lss.launchScreenTitles = arrayOf("Wiz App")
        lss.launchScreenHeadline = "Now with Flows!"
        lss.launchScreenDescriptions = arrayOf("See how easy it is to onboard with Flows")
        lss.launchScreenImages = intArrayOf(R.drawable.graphic_airplane)
        welcomeScreenStep.setWelcomeScreenSettings(lss)

        // adds the QR code activation screen during onboarding
        //welcomeScreenStep.setProviders(new ConfigurationProvider[] {new JsonConfigurationProvider()});

        // skips the Scan Succeeded screen after scanning the QR code
        //QRCodeReaderSettings qrcrs = new QRCodeReaderSettings();
        //qrcrs.setSkipConfirmScreen(true);
        //welcomeScreenStep.setQrCodeConfirmSettings(qrcs);
        //welcomeScreenStep.setQrCodeReaderSettings(qrcrs);

        val oAuth2Configuration = OAuth2Configuration1.Builder(applicationContext)
            .clientId(OAUTH_CLIENT_ID)
            .responseType("code")
            .authUrl(AUTH_END_POINT)
            .tokenUrl(TOKEN_END_POINT)
            .redirectUrl(OAUTH_REDIRECT_URL)
            .build()

        welcomeScreenStep.setOAuthConfiguration(oAuth2Configuration);

        // Creating flow and configuring steps
        val flow = Flow("onboard")
        flow.setSteps(
            arrayOf(
                PasscodePolicyStoreStep(), // Creates the passcode policy store (RLM_SECURE_STORE)
                welcomeScreenStep, // Shows the welcome screen and getting the configuration data
                OAuthStep(), // Authenticates with Mobile Services
                settingsDownloadStep, // Get the client policy data from the server
                LoggingStep(), // available in 2.0.1 and above
                StoreManagerStep(), // Manages the Application Store (APP_SECURE_STORE), encrypted using passcode key
                OAuthStoreStep(), // Persists the credentials into the application Store
                WelcomeScreenStoreStep(), // Persists the configuration data into the application store
                SettingsStoreStep(), // Persists the passcode policy into the application store
                eulaScreenStep                  // Presents the EULA screen and persists the version of the EULA into the application store
            )
        )

        // Preparing the flow context
        flowContext!!.context = application
        flowContext!!.setFlowPresentationActionHandler(FlowPresentationActionHandlerImpl(this))

        flowManagerService!!.execute(flow, flowContext!!, object : FlowActionHandler {
            override fun onFailure(t: Throwable) {
                // flowManagerService failed to execute so create an alert dialog to inform users of errors
                LOGGER.debug("Failed to onboard.  " + t.message)
                showAlertDialog("Onboard", t)
            }

            override fun onSuccess(result: FlowContext) {
                initializeLogging(Level.DEBUG) // TODO remove when https://support.wdf.sap.corp/sap/support/message/1980000361 is fixe
                LOGGER.debug("Successfully onboarded")

                // remove the splash screen and replace it with the actual working app screen
                supportActionBar!!.show()
                setContentView(R.layout.activity_tile)
                // save the onboarded status
                LOGGER.debug("changing isOnboarded to true");
                StoreHelper.setIsOnboarded(getApplicationContext(), true);

                // save the onboarded status
                LOGGER.debug("changing isOnboarded to true");
                StoreHelper.setIsOnboarded(getApplicationContext(), true);

                // log values from the stores
                LOGGER.debug("About to log values from the Passcode Policy Store");
                StoreHelper.logStoreValues((result as OnboardingContext).getPasscodePolicyStore());
                LOGGER.debug("About to log values from the Application Store");
                StoreHelper.logStoreValues((result as OnboardingContext).getApplicationStore());
            }
        })
    }

    fun startRestoreFlow() {
        LOGGER.debug("startRestoreFlow: starting restore flow");
        flowContext = OnboardingContext();

        // Creating flow and configuring steps
        val flow = Flow("restore")
        flow.setSteps(
            arrayOf(
                PasscodePolicyStoreStep(), // Creates the passcode policy store (RLM_SECURE_STORE)
                StoreManagerStep(), // Manages the Application Store (APP_SECURE_STORE), encrypted using passcode key
                WelcomeScreenStoreStep(),     // Reads the previously stored config data and adds it to the context
                SettingsStoreStep(), // Persists the passcode policy into the application store
                OAuthStep(), // Authenticates with Mobile Services
                OAuthStoreStep(), // Persists the credentials into the application Store
                settingsDownloadStep, // Get the client policy data from the server
                LoggingStep(), // available in 2.0.1 and above
                ChangePasscodeStep(),         // If required by a change in client policy, display the change passcode screen
                eulaScreenStep                  // Presents the EULA screen and persists the version of the EULA into the application store
            )
        )

        // Preparing the flow context
        flowContext!!.context = application
        flowContext!!.setFlowPresentationActionHandler(FlowPresentationActionHandlerImpl(this))

        flowManagerService!!.execute(flow, flowContext!!, object : FlowActionHandler {
            override fun onFailure(t: Throwable) {
                // flowManagerService failed to execute so create an alert dialog to inform users of errors
                LOGGER.debug("Failed to restore.  " + t.message)
                showAlertDialog("Restore", t)
            }

            override fun onSuccess(result: FlowContext) {
                initializeLogging(flowContext!!.policy.logLevel)
                supportActionBar!!.show()
                setContentView(R.layout.activity_tile)
                LOGGER.debug("Successfully restored");
                LOGGER.warn("Log level after restore is: " + Logging.getRootLogger().getLevel().toString());
            }
        })

    }

    fun onUploadLog(view: View) {
        LOGGER.debug("In onUploadLog")
    }

    fun onChange(view: View) {
        LOGGER.debug("In onChange")
    }

    fun onReset(view: View) {
        LOGGER.debug("In onReset")
    }

    fun showAlertDialog(flow: String, t: Throwable) {
        // create an alert dialog because an error has been thrown
        val alertDialog = AlertDialog.Builder(this@LoginActivity).create()
        alertDialog.setTitle("Failed to execute $flow Flow")
        alertDialog.setMessage(if (t.message == "Eula Rejected") "EULA Rejected" else "" + t.message)

        // dismisses the dialog if OK is clicked, but if the EULA was rejected then app is reset
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "OK"
        ) { dialog, which ->
            //                        if (t.getMessage().equals("Eula Rejected") || flow.equals("Onboard")) {
            //                            startResetFlow();
            //                        }
            dialog.dismiss()
        }

        // changes the colour scheme
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#008577"))
        }

        alertDialog.show()
    }



    companion object {
        private val LOGGER = LoggerFactory.getLogger(MainActivity::class.java)
    }
}
