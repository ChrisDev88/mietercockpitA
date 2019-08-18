package de.datatrain.mietercockpitnative.onboarding

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.EditText
import androidx.annotation.RequiresApi
import de.datatrain.mietercockpitnative.R
import java.util.*

class RegisterActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar()?.hide(); // hide the title bar
        this.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_register)

        val inputDate : EditText = findViewById(de.datatrain.mietercockpitnative.R.id.inputVertragStart)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        inputDate.setOnClickListener{
            val dpd : DatePickerDialog = DatePickerDialog(this,R.style.MySpinnerDatePickerStyle, DatePickerDialog.OnDateSetListener{ view: DatePicker, i: Int, i1: Int, i2: Int ->

                c.set(Calendar.YEAR, i)
                c.set(Calendar.MONTH, i1)
                c.set(Calendar.DAY_OF_MONTH, i2)
                val myFormat : String = "dd.MM.yyyy"
                val sdf : SimpleDateFormat = SimpleDateFormat(myFormat, Locale.GERMANY)
                inputDate.setText(sdf.format(c.getTime()))
            },year,month,day)

            dpd.show()
        }

    }

    fun onCancel(view: View){
        val int1 = Intent(this@RegisterActivity, LaunchActivity::class.java)
        startActivity(int1)
    }
}
