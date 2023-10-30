package curs.narcis.neisisnotes.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import curs.narcis.neisisnotes.activities.CreateBoardActivity
import curs.narcis.neisisnotes.activities.MainActivity
import curs.narcis.neisisnotes.activities.MyProfileActivity
import curs.narcis.neisisnotes.activities.SignInActivity
import curs.narcis.neisisnotes.activities.SignUpActivity
import curs.narcis.neisisnotes.activities.TaskListActivity
import curs.narcis.neisisnotes.models.Board
import curs.narcis.neisisnotes.models.User
import curs.narcis.neisisnotes.utils.Constants

class FirestoreClass {

    private val mFirestore = FirebaseFirestore.getInstance()


    fun getBoardsDetails(taskListActivity: TaskListActivity, boardDocumentID: String) {
        mFirestore.collection(Constants.BOARDS)
            .document(boardDocumentID)
            .get()
            .addOnSuccessListener { document ->
                Log.i(taskListActivity.javaClass.simpleName, document.toString())

                taskListActivity.boardDetails(document.toObject(Board::class.java)!!)

            }
            .addOnFailureListener { e ->
                taskListActivity.hideProgressDialog()
                Log.e("ERROR_CLASS", "Error while getting board details: ${e.message.toString()}", e)
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFirestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successfully!", Toast.LENGTH_LONG).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("ERROR_CLASS", "Error creating board ${e.message.toString()}", e)
            }
    }

    fun getBoardsList(activity: MainActivity){
        mFirestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardsList : ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }
                activity.populateBoardListToUI(boardsList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("ERROR_CLASS", "Error while creating boards list ${e.message.toString()}", e)
            }
    }

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("ERROR_CLASS", "Error writing document ${e.message.toString()}", e)
            }
    }


    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFirestore.collection(Constants.USERS).document(getCurrentUserID()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile updated")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener {
                e -> Log.e(activity.javaClass.simpleName, "Error while updating", e)
            }
    }

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {document ->
                val loggedInUser = document.toObject(User::class.java)

                when(activity){
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }

            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("ERROR_CLASS", "Error writing document ${e.message.toString()}", e)
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }


}