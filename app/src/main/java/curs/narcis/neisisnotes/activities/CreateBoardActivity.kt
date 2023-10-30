package curs.narcis.neisisnotes.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import curs.narcis.neisisnotes.R
import curs.narcis.neisisnotes.databinding.ActivityCreateBoardBinding
import curs.narcis.neisisnotes.firebase.FirestoreClass
import curs.narcis.neisisnotes.models.Board
import curs.narcis.neisisnotes.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var mSelectedImageUri: Uri? = null
    private var mBoardImageURL : String = ""

    private var binding : ActivityCreateBoardBinding? = null

    private lateinit var mUsername : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setActionBar()

        if (intent.hasExtra(Constants.NAME)){
            mUsername = intent.getStringExtra(Constants.NAME).toString()
        }

        binding?.ivBoardImage?.setOnClickListener {
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
                        binding?.ivBoardImage?.let { Glide.with(this).load(mSelectedImageUri).into(it) }
                    } catch (e: IOException){
                        e.printStackTrace()
                    }
                }
            }
        }

        binding?.btnCreate?.setOnClickListener {
            if (mSelectedImageUri != null){
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun setActionBar() {
        setSupportActionBar(binding?.toolbarCreateBoardActivity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar?.title = resources.getString(R.string.create_board_title)

        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
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

    private fun createBoard(){
        val assignedUsers : ArrayList<String> = ArrayList()
        assignedUsers.add(getCurrentUserID())

        var board = Board(binding?.etBoardName?.text.toString(), mBoardImageURL, mUsername, assignedUsers)

        FirestoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageUri != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this, mSelectedImageUri))
            sRef.putFile(mSelectedImageUri!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i("Board Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mBoardImageURL = uri.toString()

                    createBoard()
                }
            }.addOnFailureListener{
                    exception ->
                Toast.makeText(this@CreateBoardActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}