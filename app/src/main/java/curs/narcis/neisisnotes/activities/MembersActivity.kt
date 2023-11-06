package curs.narcis.neisisnotes.activities

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import curs.narcis.neisisnotes.R
import curs.narcis.neisisnotes.adapters.MemberListItemsAdapter
import curs.narcis.neisisnotes.databinding.ActivityMembersBinding
import curs.narcis.neisisnotes.firebase.FirestoreClass
import curs.narcis.neisisnotes.models.Board
import curs.narcis.neisisnotes.models.User
import curs.narcis.neisisnotes.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MembersActivity : BaseActivity() {

    private var binding: ActivityMembersBinding? = null

    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<User>

    private var anyChangesMade: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
        }

        setActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    fun memberDetails(user: User){
        mBoardDetails.assignedTo.add(user.id!!)
        FirestoreClass().assignMembersToBoard(this, mBoardDetails, user)
    }

    fun setUpMembersList(list: ArrayList<User>){
        mAssignedMembersList = list
        hideProgressDialog()
        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this)
        binding?.rvMembersList?.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        binding?.rvMembersList?.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if (email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)
            } else {
                Toast.makeText(this@MembersActivity, "Please enter members email address.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setActionBar(){
        setSupportActionBar(binding?.toolbarMembersActivity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar?.title = resources.getString(R.string.members)

        binding?.toolbarMembersActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (anyChangesMade){
            setResult(Activity.RESULT_OK)
        }

        super.onBackPressed()
    }


    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMembersList.add(user)

        anyChangesMade = true

        setUpMembersList(mAssignedMembersList)

        SendNotificationToUserCoroutine(mBoardDetails.name!!, user.fcmToken!!).execute()

    }

    private inner class SendNotificationToUserCoroutine(val boardName: String, val token: String) {
        fun execute() {
            // Mostrar el diálogo de progreso
            showProgressDialog(resources.getString(R.string.please_wait))

            // Utilizar una coroutine para realizar la operación de red en un hilo de fondo
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = sendNotification()

                    // Actualizar la interfaz de usuario en el hilo principal
                    withContext(Dispatchers.Main) {
                        // Ocultar el diálogo de progreso
                        hideProgressDialog()

                        // Trabajar con el resultado aquí (por ejemplo, mostrarlo en un TextView)
                        Log.e("JSON Response Result", result)
                    }
                } catch (e: Exception) {
                    // Manejar errores aquí
                }
            }
        }

        private suspend fun sendNotification(): String {
            var result: String

            try {
                val url = URL(Constants.FCM_BASE_URL)
                val connection = withContext(Dispatchers.IO) {
                    url.openConnection()
                } as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty(Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}")
                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)

                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the Board $boardName")
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the new board by ${mAssignedMembersList[0].name}"
                )

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                withContext(Dispatchers.IO) {
                    wr.writeBytes(jsonRequest.toString())
                }
                withContext(Dispatchers.IO) {
                    wr.flush()
                }
                withContext(Dispatchers.IO) {
                    wr.close()
                }

                val httpResult: Int = connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?

                    while (withContext(Dispatchers.IO) {
                            reader.readLine()
                        }.also { line = it } != null) {
                        sb.append(line + "\n")
                    }

                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }

            } catch (e: Exception) {
                result = "Error : " + e.message
            }

            return result
        }
    }
}