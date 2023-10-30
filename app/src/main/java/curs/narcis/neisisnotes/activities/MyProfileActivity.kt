package curs.narcis.neisisnotes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import curs.narcis.neisisnotes.R
import curs.narcis.neisisnotes.databinding.ActivityMyProfileBinding
import curs.narcis.neisisnotes.firebase.FirestoreClass
import curs.narcis.neisisnotes.models.User
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import curs.narcis.neisisnotes.utils.Constants
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var mSelectedImageUri: Uri? = null
    private var mProfileImageURL : String = ""

    private lateinit var mUserDetails : User

    private var binding : ActivityMyProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        FirestoreClass().loadUserData(this)
        setActionBar()

        binding?.btnUpdate?.setOnClickListener {
            if (mSelectedImageUri != null){
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

        binding?.ivProfileUserImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED){
                //Show image chooser
                showImageChooser()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        Constants.READ_STORAGE_PERMISSION
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION
                    )
                }
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null){
                    mSelectedImageUri = data.data
                    try {
                        binding?.ivProfileUserImage?.let { Glide.with(this).load(mSelectedImageUri).into(it) }
                    } catch (e: IOException){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Show image chooser
                showImageChooser()
            }
        } else {
            Toast.makeText(this, "You just denied permissions, you will need to allow it if you want to pick an image", Toast.LENGTH_LONG).show()
        }
    }

    private fun showImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun setActionBar() {
        setSupportActionBar(binding?.toolbarMyProfileActivity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar?.title = resources.getString(R.string.my_profile)

        binding?.toolbarMyProfileActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    fun setUserDataInUI(loggedInUser: User?) {
        if (loggedInUser != null) {
            mUserDetails = loggedInUser
        }
        binding?.ivProfileUserImage?.let { Glide.with(this).load(loggedInUser?.image).into(it) }
        binding?.etName?.setText(loggedInUser?.name)
        binding?.etEmail?.setText(loggedInUser?.email)
        if (loggedInUser?.mobile != 0L) {
            binding?.etMobile?.setText(loggedInUser?.mobile.toString())
        }
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (binding?.etName?.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
        }

        if (binding?.etMobile?.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this, userHashMap)

    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageUri != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this, mSelectedImageUri))
            sRef.putFile(mSelectedImageUri!!).addOnSuccessListener {
                taskSnapshot ->
                    Log.i("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }

            }.addOnFailureListener{
                exception ->
                Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }


    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}