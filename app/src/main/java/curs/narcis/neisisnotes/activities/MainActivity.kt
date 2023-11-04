package curs.narcis.neisisnotes.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import curs.narcis.neisisnotes.R
import curs.narcis.neisisnotes.adapters.BoardItemsAdapter
import curs.narcis.neisisnotes.databinding.ActivityMainBinding
import curs.narcis.neisisnotes.firebase.FirestoreClass
import curs.narcis.neisisnotes.models.Board
import curs.narcis.neisisnotes.models.User
import curs.narcis.neisisnotes.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding : ActivityMainBinding? = null

    private val onBackPressedCallback = object: OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
                binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
            } else {
                // A double back press function is added in Base Activity.
                doubleBackToExit()
            }
        }
    }

    private lateinit var myProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var mCreateBoardLauncher: ActivityResultLauncher<Intent>

    private lateinit var mUsername : String

    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mSharedPreferences = this.getSharedPreferences(Constants.NEISISNOTES_PREFERENCES, Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        if (tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
                if (!it.isSuccessful) {
                    Log.w("GET_TOKEN_FAIL", "Fetching FCM registration token failed", it.exception)
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = it.result
                updateFCMToken(token)
            })
        }

        myProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            if (resultCode == Activity.RESULT_OK) {
                FirestoreClass().loadUserData(this)
            } else {
                Log.e("Cancelled", "Cancelled")
            }
        }

        mCreateBoardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            val resultCode = result.resultCode
            if (resultCode == Activity.RESULT_OK){
                FirestoreClass().getBoardsList(this)
            } else {
                Log.e("Cancelled", "Cancelled")
            }
        }

        setActionBar()
        binding?.navView!!.setNavigationItemSelectedListener(this)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        FirestoreClass().loadUserData(this, true)

        binding?.fabCreateBoard?.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUsername)
            mCreateBoardLauncher.launch(intent)
        }
    }


    private fun setActionBar(){
        setSupportActionBar(binding?.toolbarMainActivity)
        binding?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        binding?.toolbarMainActivity?.title = R.string.app_name.toString()

        binding?.toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                myProfileLauncher.launch(intent)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply() //reset preferences

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User?, readBoard: Boolean) {
        hideProgressDialog()
        if (readBoard){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
        mUsername = loggedInUser?.name.toString()
        binding?.navView?.getHeaderView(0)?.findViewById<CircleImageView>(R.id.iv_user_image)?.let { Glide.with(this).load(loggedInUser!!.image).placeholder(R.drawable.ic_user_place_holder).into(it) }
        binding?.navView?.getHeaderView(0)?.findViewById<TextView>(R.id.tv_username)?.text = loggedInUser?.name
    }

    fun populateBoardListToUI(boardList: ArrayList<Board>) {
        hideProgressDialog()
        if (boardList.size > 0){
            binding?.rvBoardsList?.visibility = View.VISIBLE
            binding?.tvNoBoardsAvailable?.visibility = View.GONE

            binding?.rvBoardsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvBoardsList?.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardList)
            binding?.rvBoardsList?.adapter = adapter

            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

        } else {
            binding?.rvBoardsList?.visibility = View.GONE
            binding?.tvNoBoardsAvailable?.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor : SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        FirestoreClass().loadUserData(this, readBoardsList = true)
    }

    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
}