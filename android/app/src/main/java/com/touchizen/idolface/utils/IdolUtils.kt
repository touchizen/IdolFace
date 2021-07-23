package com.touchizen.idolface.utils;

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
//import com.fcmsender.FCMSender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
//import com.gowtham.letschat.models.UserProfile
import com.touchizen.idolface.SplashActivity
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.model.ModelDeviceDetails
import com.touchizen.idolface.model.UserStatus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
//import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.measureNanoTime

object IdolUtils {

    var queriedList=ArrayList<IdolProfile>()

    var resultCount=0

    const val NOTIFICATION_ID=23
    public const val IDOLS_COLLECTION = "Idols"
    public const val IDOLGALLERY_COLLECTION = "IdolGallery"
    public const val IDOLREPORT_COLLECTION = "IdolReport"

    var totalRecursionCount=0

    fun getUniqueId(): String {
        val db = FirebaseFirestore.getInstance()
        return db.collection(IDOLS_COLLECTION).document().id
    }

    fun getStorageRef(context: Context, idolId:String): StorageReference {
        //val preference = MPreference(context)
        val ref = Firebase.storage.getReference(IDOLS_COLLECTION)
        return ref.child(idolId)
    }

    fun getDocumentRef(context: Context, idolId:String): DocumentReference {

        val db = FirebaseFirestore.getInstance()
        return db.collection(IDOLS_COLLECTION).document(idolId!!)
    }

    fun getGalleryUniqueId(): String {
        val db = FirebaseFirestore.getInstance()
        return db.collection(IDOLGALLERY_COLLECTION).document().id
    }

    fun getGalleryStorageRef(context: Context, imageId:String): StorageReference {
        //val preference = MPreference(context)
        val ref = Firebase.storage.getReference(IDOLGALLERY_COLLECTION)
        return ref.child(imageId)
    }

    fun getGalleryDocumentRef(context: Context, idolId:String): DocumentReference {
        val db = FirebaseFirestore.getInstance()
        return db.collection(IDOLGALLERY_COLLECTION).document(idolId!!)
    }

    fun getReportUniqueId(): String {
        val db = FirebaseFirestore.getInstance()
        return db.collection(IDOLREPORT_COLLECTION).document().id
    }

    fun getReportDocumentRef(context: Context, idolId:String): DocumentReference {
        val db = FirebaseFirestore.getInstance()
        return db.collection(IDOLREPORT_COLLECTION).document(idolId!!)
    }

//    fun fetchContacts(context: Context): List<Contact> {
//        val preference=MPreference(context)
//        val names = ArrayList<String>()
//        val numbers = ArrayList<String>()
//        val contacts=ArrayList<Contact>()
//        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
//        val projection = arrayOf(
//            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//            ContactsContract.CommonDataKinds.Phone.NUMBER
//        )
//        val selection: String? =
//            null //it's like a where concept in mysql
//        val selectionArgs: Array<String>? = null
//        val sortOrder: String? = null
//        val resolver = context.contentResolver
//        val cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder)
//        while (cursor!!.moveToNext()) {
//            val name =
//                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
//            val number =
//                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//            if(number.contains(preference.getMobile()!!.number))
//                continue
//            names.add(name)
//            numbers.add(number)
//            contacts.add(Contact(name, number))
//        }
//        cursor.close()
//        val hashMap=getCountryCodeRemovedList(context, contacts)
//        contacts.clear()
//        for (number in hashMap.keys){
//            contacts.add(Contact(hashMap[number].toString(), number))
//        }
//        return contacts.sortedWith(compareBy { it.name })
//    }
//
//    private fun getCountryCodeRemovedList(context: Context, contacts: ArrayList<Contact>): HashMap<String, String> {
//        val hashMap:HashMap<String, String> = HashMap() //hashmap to get rid of duplication
//        val preference=MPreference(context)
//        contacts.forEach { contact ->
//            if (contact.mobile.length <= 5 ||
//                contact.mobile.contains(preference.getMobile()?.number!!))
//                return@forEach
//            var mobile=contact.mobile
//            for (country in Countries.getCountries()) {
//                if (mobile.contains(country.noCode)) {
//                    mobile = contact.mobile.replace(country.noCode, "")
//                    break
//                }
//            }
//            hashMap[mobile.replace(" ", "")] = contact.name
//        }
//        return hashMap
//    }

    fun getDeviceInfo(context: Context): JSONObject {
        try {
            val deviceInfo = JSONObject()
            deviceInfo.put("device_id", getDeviceId(context))
            deviceInfo.put("device_model", Build.MODEL)
            deviceInfo.put("device_brand", Build.BOARD)
            deviceInfo.put("device_country", Locale.getDefault())
            deviceInfo.put("device_os_v", Build.VERSION.RELEASE)
            deviceInfo.put("app_version", getVersionName(context))
            deviceInfo.put("package_name", context.packageName)
            deviceInfo.put("device_type", "android")
            return deviceInfo
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return JSONObject()
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String? {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun getVersionName(context: Context): String? {
        try {
            val packageName = context.packageName
            val pInfo = context.packageManager.getPackageInfo(packageName, 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "1.0"
    }

    fun logOut(context: Activity, preference: MPreference) {
        try {
            EventBus.getDefault().post(UserStatus("offline"))
            FirebaseAuth.getInstance().signOut()
            preference.clearAll()
            EtcUtils.startNewActivity(context, SplashActivity::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    fun logOut(context: Activity, preference: MPreference,db: ChatUserDatabase) {
//        try {
//            CoroutineScope(Dispatchers.IO).launch {
//                db.clearAllTables()
//            }
//            EventBus.getDefault().post(UserStatus("offline"))
//            ChatHandler.removeListeners()
//            GroupChatHandler.removeListener()
//            ChatUserProfileListener.removeListener()
//            AdSingleChatHome.allChatList= emptyList<ChatUserWithMessages>().toMutableList()
//            AdGroupChatHome.allList= emptyList<GroupWithMessages>().toMutableList()
//            FirebaseAuth.getInstance().signOut()
//            preference.clearAll()
//            Utils.startNewActivity(context, ActSplash::class.java)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    fun sendPush(context: Context, type: String,body: String, token: String,to: String) {
        try {
//            val data=JSONObject()
//            val pushData=JSONObject()
//            data.put("type", type)
//            data.put("message_body",body)
//            data.put("to",to)
//            pushData.put("data",data)
//            val push = FCMSender.Builder()
//                .serverKey(Constants.FCM_SERVER_KEY)
//                .setData(pushData)
//                .toTokenOrTopic(token)
//                .responseListener(object : FCMSender.ResponseListener {
//                    override fun onFailure(errorCode: Int,message: String) {
//                        LogMessage.v("notification sent Failed to $token")
//                    }
//
//                    override fun onSuccess(response: String) {
//                        LogMessage.v("notification sent Successfully to $token")
//                    }
//                }).build()
//            push.sendPush(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    fun setUnReadCountZero(repo: DbRepository, chatUser: ChatUser) {
//        try {
//            val time= measureNanoTime {
//                chatUser.unRead=0
//                repo.insertUser(chatUser)
//            }
//            Timber.v("Taken time $time")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }


//    fun getChatUserId(fromUser: String, message: Message)= if (message.from != fromUser) message.from
//    else message.to
//
//    fun sendTypingStatus(database: FirebaseDatabase, isTyping: Boolean, vararg users: String) {
//        try {
//            val typingRef = database.getReference("/Users/${users[0]}/typing_status")
//            val chatUserRef = database.getReference("/Users/${users[0]}/chatuser")
//            typingRef.setValue(if (isTyping) "typing" else "not_typing")
//            chatUserRef.setValue(users[1])
//            typingRef.onDisconnect().setValue("not_typing")
//            chatUserRef.onDisconnect().setValue("")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

//    fun updateContactsProfiles(listener :QueryCompleteListener?): Boolean {
//        Timber.v("Query Called")
//        val allContacts = fetchContacts(MApplication.appContext).toMutableList()
//        val listOfMobiles = ArrayList<String>()
//        allContacts.forEach {
//            listOfMobiles.add(it.mobile)
//        }
//        if(listOfMobiles.isEmpty())
//            return false
//        return makeQueryRecursively(listOfMobiles, 1,listener ?: onQueryCompleted)
//    }

//    private tailrec fun makeQueryRecursively(listOfMobNos: ArrayList<String>, position: Int
//                                             ,listener :QueryCompleteListener): Boolean {
//        val firstTen = ArrayList<String>()
//        val size = if (listOfMobNos.size < 10) listOfMobNos.size else 10
//        for (index in 0 until size)
//            firstTen.add(listOfMobNos[index])
//        listOfMobNos.subList(0, size).clear()  //remove queried elements
//        val contactsQuery = ContactsQuery(firstTen, position,listener)
//        contactsQuery.makeQuery()
//
//        return if (listOfMobNos.isEmpty()) {
//            totalRecursionCount=position
//            LogMessage.v("Queried times $position")
//            true
//        }else makeQueryRecursively(listOfMobNos, position + 1,listener)
//    }

//    fun getChatUser(
//        doc: UserProfile,
//        chatUsers: List<ChatUser>,
//        savedName: String): ChatUser {
//        var existData: ChatUser? = null
//        if (chatUsers.isNotEmpty()) {
//            val contact = chatUsers.firstOrNull { it.id == doc.uId }
//            contact?.let {
//                existData=it
//            }
//        }
//        return existData?.apply {
//            user = doc
//            localName = savedName
//            locallySaved=true
//        } ?: ChatUser(doc.uId.toString(), savedName, doc,locallySaved = true)
//    }
//
//
//    private val onQueryCompleted=object : QueryCompleteListener {
//        override fun onQueryCompleted(queriedList: ArrayList<UserProfile>) {
//            try {
//                Timber.v("onQueryCompleted ${queriedList.size}")
//                val localContacts= fetchContacts(MApplication.appContext)
//                val finalList = ArrayList<ChatUser>()
//                //set localsaved name to queried users
//                CoroutineScope(Dispatchers.IO).launch {
//                    val chatUsers=MApplication.userDaoo.getChatUserList()
//                    withContext(Dispatchers.Main){
//                        for(doc in queriedList){
//                            val savedNumber=localContacts.firstOrNull { it.mobile == doc.mobile?.number }
//                            if(savedNumber!=null){
//                                val chatUser=getChatUser(doc,chatUsers,savedNumber.name)
//                                finalList.add(chatUser)
//                            }
//                        }
//                        setDefaultValues()
//                        withContext(Dispatchers.IO){
//                            MApplication.userDaoo.insertMultipleUser(finalList)
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    private fun setDefaultValues() {
        totalRecursionCount =0
        resultCount =0
        queriedList.clear()
    }

//    fun setUnReadCountGroup(groupDao: GroupDao, group: Group) {
//        CoroutineScope(Dispatchers.IO).launch {
//            group.unRead=0
//            groupDao.insertGroup(group)
//        }
//    }
//
//    fun insertGroupMsg(groupMsgDao: GroupMessageDao, message: GroupMessage) {
//        CoroutineScope(Dispatchers.IO).launch {
//            groupMsgDao.insertMessage(message)
//        }
//    }
//
//    fun insertMutlipleGroupMsg(groupMsgDao: GroupMessageDao, messages: List<GroupMessage>) {
//        CoroutineScope(Dispatchers.IO).launch {
//            groupMsgDao.insertMultipleMessage(messages)
//        }
//    }
}