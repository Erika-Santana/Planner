package br.edu.ifsp.dmo1.plannersensorial.helper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo1.plannersensorial.ui.activities.MainActivity

class CameraHelper(
    activity: AppCompatActivity,
    private val callback: MainActivity
) {
    interface Callback {
        fun onFotoRecebida(bitmap: Bitmap?, dialogView: View)
    }

    private var currentDialogView: View? = null

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.extras?.getParcelable("data", Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.extras?.get("data") as? Bitmap
            }
            bitmap?.let {
                val copia = it.copy(Bitmap.Config.ARGB_8888, true)
                currentDialogView?.let { view ->
                    callback.onFotoRecebida(copia, view)
                }
            }
        }
    }

    fun tirarFoto(dialogView: View) {
        currentDialogView = dialogView
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        launcher.launch(intent)
    }
}
