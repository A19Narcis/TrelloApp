package curs.narcis.neisisnotes.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import curs.narcis.neisisnotes.R
import curs.narcis.neisisnotes.databinding.ActivitySignUpBinding
import curs.narcis.neisisnotes.firebase.FirestoreClass
import curs.narcis.neisisnotes.models.User

class SignUpActivity : BaseActivity() {

    private var binding : ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpActionBar()

        binding?.btnSignUp?.setOnClickListener {
            registerUser()
        }
    }


    private fun setUpActionBar(){
        setSupportActionBar(binding?.toolbarSignUpActivity)
        val actionBar = supportActionBar

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
            binding?.toolbarSignUpActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun registerUser(){
        val name: String = binding?.etName?.text.toString().trim { it <= ' '} //Clean empty chars
        val email: String = binding?.etEmail?.text. toString().trim { it <= ' '}
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' '}
        if (validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email
                    val user = User(firebaseUser.uid, name, registeredEmail)

                    FirestoreClass().registerUser(this, user)
                } else {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
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

    fun userRegisteredSuccess() {
        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_LONG).show()

        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

}