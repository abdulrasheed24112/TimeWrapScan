package com.development.nest.time.wrap.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.ConfirmDeleteDialogLayoutBinding
import com.development.nest.time.wrap.databinding.ConfirmDiscardDialogLayoutBinding
import com.development.nest.time.wrap.databinding.DialogRateUsLayoutBinding
import com.development.nest.time.wrap.databinding.ExitDialogeBinding
import com.development.nest.time.wrap.databinding.MenuLayoutBinding
import com.development.nest.time.wrap.databinding.ScanningDiscardDialogLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.permissionx.guolindev.PermissionX
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit

fun Activity.renameDialog(file: File, onCommit: (String) -> Unit) {
    val oldName = file.nameWithoutExtension

    try {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.extentions_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val enterNameEt = dialog.findViewById<EditText>(R.id.enterNameEt)
        val renameBtn = dialog.findViewById<TextView>(R.id.commitBtn)
        val clearBtn = dialog.findViewById<ImageView>(R.id.clearEdiText)
        clearBtn.setOnClickListener {
            enterNameEt.setText("")
        }

        enterNameEt.setText(oldName)

        renameBtn.setOnClickListener {
            val fileName = enterNameEt.text.trim()
            if (fileName.isNotEmpty()) {
                val destination = File("${file.parent}/$fileName.${file.extension}")

                if (!destination.exists()) {
                    val name = "" + enterNameEt.text
                    onCommit.invoke(name)
                    dialog.dismiss()
                } else {
                    showToast("File already exist!")
                }
            } else {
                showToast("Please enter name")
            }
        }

        val cancelBtn = dialog.findViewById<TextView>(R.id.cancelBtn)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()

        enterNameEt.setSelection(enterNameEt.length())

        Handler(Looper.getMainLooper()).postDelayed({
            enterNameEt.requestFocus()
            try {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(enterNameEt, InputMethodManager.SHOW_IMPLICIT)
            } catch (e: Exception) {
                Log.d("createPlaylistDialog", "createPlaylistDialog: $e")
            }
        }, 500)
    } catch (_: Exception) {

    }
}


val cameraPermission: Array<String> = arrayOf(Manifest.permission.CAMERA)

val storagePermissions = if (Build.VERSION.SDK_INT >= 33) {
    arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
} else {
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
}

/*val storagePermissions = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
)*/

val allPermissions = if (Build.VERSION.SDK_INT >= 33) {
    arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.POST_NOTIFICATIONS
    )
} else {
    arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
}

//val allPermissions = arrayOf(
//    Manifest.permission.CAMERA,
//    Manifest.permission.READ_EXTERNAL_STORAGE,
//    Manifest.permission.WRITE_EXTERNAL_STORAGE
//)

fun Activity.requestPermission(
    permission: Array<String>, activity: FragmentActivity, click: ((Boolean) -> Unit)? = null
) {
    PermissionX.init(activity).permissions(
        *permission
    ).onExplainRequestReason { scope, deniedList ->
        scope.showRequestReasonDialog(
            deniedList,
            "The core fundamentals are based on these permissions.",
            "ok",
            "cancel"
        )
    }.onForwardToSettings { scope, deniedList ->
        scope.showForwardToSettingsDialog(
            deniedList,
            "Permissions are denied",
            "ok",
            "cancel"
        )
    }.request { allGranted, _, _ ->
        if (allGranted) {
            click?.invoke(true)
        } else {
            showToast("Please enable permissions")
        }
    }
}

fun Activity.checkPermissions(permissions: Array<String>): Boolean {
    var allGranted = true
    for (permission in permissions) {
        val result = ContextCompat.checkSelfPermission(this, permission)
        if (result != PackageManager.PERMISSION_GRANTED) {
            allGranted = false
        }
    }
    return allGranted
}

fun shareOnSocialMedia(context: Context, path1: String, packageName: String) {
    if (isAppInstalled(context, packageName)) {
        try {
            Log.d("shareOnSocialMedia", "shareOnSocialMedia: $path1")
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.setPackage(packageName)

            val uri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName.toString() + ".provider",
                File(path1)
            )
            sharingIntent.type = "*/*"
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
            context.startActivity(sharingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        context.showToast("App is not installed")
    }
}

fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= 33) {
            /*context.packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(0)
            )*/
        } else {
            @Suppress("DEPRECATION") context.packageManager.getPackageInfo(packageName, 0)
        }
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun copyFile(
    activity: Activity, sourcePath: String, destinationPath: String, result: (Boolean) -> Unit
) {
    try {
        val sourceFile = File(sourcePath)
        val destinationFile = File(destinationPath)

        val inputStream = FileInputStream(sourceFile)
        val outputStream = FileOutputStream(destinationFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        inputStream.close()
        outputStream.close()

        updateGallery(activity, destinationPath)
        result(true)
    } catch (e: Exception) {
        result(false)
        Log.d("copyFile", "copyFile: $e")
    }
}

fun updateGallery(activity: Activity, path1: String) {
    val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    val contentUri = Uri.fromFile(File(path1))
    scanIntent.data = contentUri
    activity.sendBroadcast(scanIntent)
}

fun requestVideoWritePermissions(activity: Activity, fromUri: Uri): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Log.d("renameVideoFile", "requestVideoWritePermissions")

        var hasPermission = true
        if (activity.checkUriPermission(
                fromUri,
                Binder.getCallingPid(),
                Binder.getCallingUid(),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            hasPermission = false
        }
        val uriList: MutableList<Uri> = ArrayList()
        uriList.add(fromUri)
        if (!hasPermission) {
            val pi = MediaStore.createWriteRequest(activity.contentResolver, uriList)
            try {
                activity.startIntentSenderForResult(pi.intentSender, 55, null, 0, 0, 0)
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
            return false
        }
        return true
    }
    return true
}

fun getFilePathToMediaID(songPath: String, context: Context): Long {
    var id: Long = 0
    val cr: ContentResolver = context.contentResolver
    val uri = MediaStore.Files.getContentUri("external")
    val selection = MediaStore.Video.Media.DATA
    val selectionArgs = arrayOf(songPath)
    val projection = arrayOf(MediaStore.Video.Media._ID)
//        val sortOrder = MediaStore.Video.Media.TITLE + " ASC"
    val cursor: Cursor? = cr.query(uri, projection, "$selection=?", selectionArgs, null)
    if (cursor != null) {
        while (cursor.moveToNext()) {
            val idIndex: Int = cursor.getColumnIndex(MediaStore.Video.Media._ID)
            id = cursor.getString(idIndex).toLong()
        }
    }
    cursor?.close()
    return id
}

fun deleteFileWithScan(context: Context, path: String, onDeleted: () -> Unit) {
    val file = File(path)
    Log.d("deletePermanent", "file deleted success: $file")
    var isDeleted = false
    try {
        isDeleted = file.delete()
        Log.d("deletePermanent", "file deleted success: $isDeleted")
    } catch (e: Exception) {
        Log.d("deletePermanent", "Exception: $e")
    }
    if (isDeleted) {
        Log.d("deletePermanent", "file deleted success: $file")

        try {
            MediaScannerConnection.scanFile(
                context, arrayOf(path), null
            ) { path, uri ->

                val uri1 = getContentUri(context, path)

                context.contentResolver.delete(uri1, null, null)
            }
            onDeleted.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun getContentUri(context: Context, path: String): Uri {
    val path1 = File(path)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        FileProvider.getUriForFile(
            context, context.applicationContext.packageName.toString() + ".provider", path1
        )
    } else {
        Uri.fromFile(path1)
    }
}

fun Activity.itemMenuBottomSheetDialog(file: File, onRename: () -> Unit, onDelete: () -> Unit) {
    val bottomSheetDialog = MenuLayoutBinding.inflate(layoutInflater)
    val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme).apply {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        setContentView(bottomSheetDialog.root)
    }
    with(bottomSheetDialog) {
        when (file.extension) {
            "jpg" -> {
                txtSave.text = getString(R.string.save_photo)
                txtShare.text = getString(R.string.share_photo)
            }
            "mp4" -> {
                txtSave.text = getString(R.string.save_video)
                txtShare.text = getString(R.string.sahre_video)
            }
        }

        sharePhoto.safeClick({
            dialog.dismiss()
            shareFile(file.absolutePath)
        }, 1000)
        savePhoto.setOnClickListener {
            saveFile(file)
            dialog.dismiss()
        }
        renameFile.safeClick({
            onRename()
            dialog.dismiss()
        })
        deleteFile.setOnClickListener {
            onDelete()
            dialog.dismiss()
        }
        if (isFileSaved(file)) {
            savePhoto.visibility = View.GONE
        }
    }

    dialog.show()
}

fun Activity.saveFile(file: File) {
    if (!isFileSaved(file)) {
        val fileType = file.extension
        val externalPicturesDir = if (fileType == "jpg") {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        }
        val directory = File("$externalPicturesDir/Time Warp Scan")
        if (!directory.exists()) {
            directory.mkdir()
        }
        val destination = "$directory/${file.name}"
        copyFile(this, file.absolutePath, destination) {}
        showToast("Successfully Saved!")
    } else {
        showToast("Already Saved!")
    }
}

fun isFileSaved(file: File): Boolean {
    val fileType = file.extension
    val externalPicturesDir = if (fileType == "jpg") {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    } else {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
    }
    val file1 = File("$externalPicturesDir/Time Warp Scan/${file.name}")
    return file1.exists()
}

fun Activity.shareFile(path1: String) {
    try {
        path1.let {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "*/*"

            val body = ""

            share.putExtra(Intent.EXTRA_TEXT, body)

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileProvider.getUriForFile(
                    this, applicationContext.packageName.toString() + ".provider", File(path1)
                )
            } else {
                Uri.fromFile(File(path1))
            }

            share.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(
                Intent.createChooser(
                    share, ""
                )
            )
        }
    } catch (e: Exception) {
        Log.d("shareStatus", "shareStatus: $e")
    }
}

fun Activity.rateUsDialog() {
    val bottomSheetDialog = DialogRateUsLayoutBinding.inflate(layoutInflater)
    val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme).apply {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        setContentView(bottomSheetDialog.root)
    }
    var rating = 5.0
    with(bottomSheetDialog) {
        rateOnPlaystore.safeClick({
            goForRating(rating, dialog)
        })
        notNow.safeClick({ dialog.dismiss() })
        ratingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
            rating = fl.toDouble()
//            goForRating(rating, dialog)
        }
    }
    dialog.show()
}

fun Activity.exitDialog(onExit: () -> Unit) {
    val binding = ExitDialogeBinding.inflate(layoutInflater)
    val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme).apply {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        setContentView(binding.root)
    }

    with(binding) {
        btnExit.safeClick({
            dialog.dismiss()
            onExit()
        })
        tvNotNow.safeClick({ dialog.dismiss() })
    }
    dialog.show()
}

fun Activity.goForRating(rating: Double, dialog: BottomSheetDialog) {
    if (rating > 4) {
        goToPlayStore()
    } else {
        feedbackListener(
            getString(R.string.feedback_msg), dialog
        )
    }
}

private fun Activity.feedbackListener(feedbackMsg: String, dialog: BottomSheetDialog) {
    if (feedbackMsg.isEmpty()) {
        showToast(getString(R.string.please_enter_feedback))
    } else {
        dialog.dismiss()
        sendMail(
            title = getString(R.string.app_name), bodyMessage = feedbackMsg
        )
    }
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.listenerFiveRating(dialog: Dialog?): View.OnClickListener {
    val listener = View.OnClickListener {
        goToPlayStore()
        dialog?.dismiss()
    }
    return listener
}

fun Context.sendMail(title: String, bodyMessage: String) {
    val selectorIntent = Intent(Intent.ACTION_SENDTO)
    selectorIntent.data = Uri.parse("mailto:")
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("developmentnest85@gmail.com"))
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
    emailIntent.putExtra(Intent.EXTRA_TEXT, "")
    emailIntent.selector = selectorIntent
    startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
}

fun Context.goToPlayStore() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}

fun Context.fullScreen(window: Window) {
    window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
    window.decorView.systemUiVisibility =
        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    window.statusBarColor = Color.TRANSPARENT
}

fun TextView.changeTextColor(context: Context?, color: Int) {
    context?.let {
        setTextColor(resources.getColor(color, context.theme))
    }
}

fun Fragment?.runOnUiThread(action: () -> Unit) {
    this ?: return
    if (!isAdded) return // Fragment not attached to an Activity
    activity?.runOnUiThread(action)
}

fun Fragment.onBackPressedCustomAction(action: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                action()
            }
        })
}

fun Activity.scanningDiscardDialog(action: () -> Unit) {
    try {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val binding = ScanningDiscardDialogLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnDiscard.setOnClickListener {
            action()
            dialog.dismiss()
        }
        binding.tvNo.setOnClickListener {
            dialog.dismiss()
        }

        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()
    } catch (e: Exception) {

    }
}

fun Activity.confirmDiscardDialog(action: () -> Unit) {
    try {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val binding = ConfirmDiscardDialogLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnDiscard.setOnClickListener {
            action()
            dialog.dismiss()
        }
        binding.tvNo.setOnClickListener {
            dialog.dismiss()
        }

        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()
    } catch (e: Exception) {

    }
}

fun Activity.confirmDeleteDialog(onDelete: () -> Unit) {
    try {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val binding = ConfirmDeleteDialogLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.tvNo.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnDelete.setOnClickListener {
            onDelete()
            dialog.dismiss()
        }

        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()
    } catch (e: Exception) {

    }
}

fun View.safeClick(listener: View.OnClickListener, blockInMillis: Long = 500) {
    var lastClickTime: Long = 0
    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < blockInMillis) return@setOnClickListener
        lastClickTime = SystemClock.elapsedRealtime()
        listener.onClick(this)
    }
}

/*fun Activity.timerDialog(time: Int, onFinish: () -> Unit) {
    try {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val binding = TimerDialogLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var isRunning = true
        val timer = startTimer(binding.timeText, time) {
            dialog.dismiss()
            isRunning = false
            onFinish()
        }
        dialog.setOnDismissListener {
            if (isRunning) {
                timer.cancel()
            }
        }

        dialog.window?.setLayout(200, 200)

        dialog.show()
    } catch (e: Exception) {

    }
}*/

fun startTimer(textView: TextView, time: Int, onFinish: () -> Unit): CountDownTimer {
    textView.text = time.toString()
    val timer: CountDownTimer = object : CountDownTimer(time * 1000L, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
            textView.text = seconds.toString()
        }

        override fun onFinish() {
            onFinish()
        }
    }
    timer.start()
    return timer
}

fun shareApp(context: Context?) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        var shareMessage = ""
        shareMessage =
            "$shareMessage https://play.google.com/store/apps/details?id=${context?.packageName}"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        context?.startActivity(
            Intent.createChooser(
                shareIntent, context.getString(R.string.share_app)
            )
        )
    } catch (e: Exception) {
    }
}