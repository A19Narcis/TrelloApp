package curs.narcis.neisisnotes.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import curs.narcis.neisisnotes.activities.CardDetailsActivity
import curs.narcis.neisisnotes.activities.CreateBoardActivity
import curs.narcis.neisisnotes.activities.MainActivity
import curs.narcis.neisisnotes.activities.MembersActivity
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
                val board = document.toObject(Board::class.java)
                board?.documentId = document.id
                taskListActivity.boardDetails(board!!)

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

    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId!!)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Task list added successfully")
                if (activity is TaskListActivity){
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener {e ->
                if (activity is TaskListActivity){
                    activity.hideProgressDialog()
                } else if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Log.e("ERROR_CLASS", "Error creating a board: ${e.message.toString()}", e)
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFirestore.collection(Constants.USERS).document(getCurrentUserID()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile updated")
                if (activity is MainActivity){
                    activity.tokenUpdateSuccess()
                } else if (activity is MyProfileActivity){
                    activity.profileUpdateSuccess()
                }
            }
            .addOnFailureListener { e ->
                if (activity is MainActivity){
                    activity.hideProgressDialog()
                } else if (activity is MyProfileActivity){
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while updating", e)
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

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
        mFirestore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())

                val usersList : ArrayList<User> = ArrayList()
                for (i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if (activity is MembersActivity){
                    activity.setUpMembersList(usersList)
                } else if (activity is TaskListActivity){
                    activity.boardMembersDetailsList(usersList)
                }

            }
            .addOnFailureListener { e ->
                if (activity is MembersActivity){
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity){
                    activity.hideProgressDialog()
                }

                Log.e("ERROR_CLASS", "Error getting members: ${e.message.toString()}", e)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        mFirestore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                if (document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("ERROR_CLASS", "Error while getting user details: ${e.message.toString()}", e)
            }
    }

    fun assignMembersToBoard(activity: MembersActivity, board: Board, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId!!)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("ERROR_CLASS", "Error while updating a board: ${e.message.toString()}", e)
            }
    }

}