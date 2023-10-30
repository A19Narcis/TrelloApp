package curs.narcis.neisisnotes.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import curs.narcis.neisisnotes.R
import curs.narcis.neisisnotes.databinding.ActivitySignInBinding
import curs.narcis.neisisnotes.models.User

class SignInActivity : BaseActivity() {

    private var binding : ActivitySignInBinding? = null

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpActionBar()

        binding?.btnSignIn?.setOnClickListener {
            signInRegisteredUser()
        }
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding?.toolbarSignInActivity)
        val actionBar = supportActionBar

        auth = FirebaseAuth.getInstance()

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
            binding?.toolbarSignInActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun signInRegisteredUser() {
        val email: String = binding?.etEmail?.text. toString().trim { it <= ' '}
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' '}


        if (validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SIGNIN", "createUserWithEmail:success")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SIGNIN", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                }
            }
        }
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }

    fun signInSuccess(loggedInUser: User?) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}