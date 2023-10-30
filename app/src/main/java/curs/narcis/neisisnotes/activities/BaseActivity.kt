package curs.narcis.neisisnotes.activities

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import curs.narcis.neisisnotes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressed = false

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        val tvProgress : TextView = mProgressDialog.findViewById(R.id.tv_progress_text)
        tvProgress.text = text
        mProgressDialog.show()
    }

    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }


    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit(){
        if(doubleBackToExitPressed){
            onBackPressedDispatcher.onBackPressed()
            return
        }

        this.doubleBackToExitPressed = true

        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            doubleBackToExitPressed = false
        }
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this@BaseActivity, R.color.snackbar_error_color))
        snackBar.show()
    }



}