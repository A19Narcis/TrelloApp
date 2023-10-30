package curs.narcis.neisisnotes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import curs.narcis.neisisnotes.R
import curs.narcis.neisisnotes.databinding.ActivityTaskListBinding
import curs.narcis.neisisnotes.firebase.FirestoreClass
import curs.narcis.neisisnotes.models.Board
import curs.narcis.neisisnotes.utils.Constants

class TaskListActivity : BaseActivity() {

    private var binding: ActivityTaskListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var boardDocumentID = ""
        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentID = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardsDetails(this, boardDocumentID)

    }

    private fun setActionBar(title: String){
        setSupportActionBar(binding?.toolbarTaskListActivity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar?.title = title

        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun boardDetails(board: Board){
        hideProgressDialog()
        board.name?.let { setActionBar(it) }
    }
}