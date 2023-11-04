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
    const val TASK_LIST: String = "taskList"
    const val ID: String = "id"
    const val EMAIL: String = "email"

    const val BOARD_DETAIL: String = "board_detail"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val BOARD_MEMBERS_LIST: String = "board_members_list"

    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"

    const val READ_STORAGE_PERMISSION = 1

    const val NEISISNOTES_PREFERENCES: String = "NeisisnotesPrefs"
    const val FCM_TOKEN:String = "fcmToken"
    const val FCM_TOKEN_UPDATED:String = "fcmTokenUpdated"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAlRJf4cU:APA91bHzhFmK5cgipFlP8Md-XXmBkbPabV7mANv3k0SwGUlo9gqVM1INGPyEdd4PX6TnyKImnmOnmWU-4BC6kLiE5AFmj56A7qGoj8-j2ZJaL7eOnl_s06Uu3_XotNv5171u6PUSNIU0"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }


}