package de.datatrain.mietercockpitnative.onboarding


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.content.Intent
import de.datatrain.mietercockpitnative.R
import org.slf4j.LoggerFactory

class LaunchActivity : AppCompatActivity() {

    private var isOnboarded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar()?.hide(); // hide the title bar
        this.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen

        isOnboarded = StoreHelper.isOnboarded(application);
        if(isOnboarded){
            val int1 = Intent(this@LaunchActivity, LoginActivity::class.java)
            startActivity(int1)
        }else{
            setContentView(R.layout.activity_launch)
        }
    }

    fun onRegister(view: View){
        val int1 = Intent(this@LaunchActivity, RegisterActivity::class.java)
        startActivity(int1)
    }

    fun onLogin(view: View){
        val int1 = Intent(this@LaunchActivity, LoginActivity::class.java)
        startActivity(int1)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MainActivity::class.java)
    }


}
