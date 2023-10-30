package curs.narcis.neisisnotes.utils

import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap
import curs.narcis.neisisnotes.activities.CreateBoardActivity

object Constants {

    const val USERS: String = "Users"

    const val BOARDS: String = "Boards"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentId"

    const val READ_STORAGE_PERMISSION = 1

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }


}