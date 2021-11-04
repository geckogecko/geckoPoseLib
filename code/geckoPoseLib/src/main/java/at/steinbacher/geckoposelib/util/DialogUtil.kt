package at.steinbacher.geckoposelib.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import at.steinbacher.geckoposelib.R

object DialogUtil {
    enum class Selection {
        CAMERA, GALLERY
    }

    interface ChooseDialogListener {
        fun onResult(selection: Selection)
        fun onDismiss()
    }

    fun showChooseDialog(
        context: Context,
        listener: ChooseDialogListener,
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customView = layoutInflater.inflate(R.layout.dialog_choose_app, null)

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_choose_image_provider)
            .setView(customView)
            .setOnCancelListener {
                listener.onDismiss()
            }
            .setNegativeButton(R.string.action_cancel) { _, _ ->
                listener.onDismiss()
            }
            .setOnDismissListener {
                listener.onDismiss()
            }
            .show()

        // Handle Camera option click
        customView.findViewById<View>(R.id.lytCameraPick).setOnClickListener {
            listener.onResult(Selection.CAMERA)
            dialog.dismiss()
        }

        // Handle Gallery option click
        customView.findViewById<View>(R.id.lytGalleryPick).setOnClickListener {
            listener.onResult(Selection.GALLERY)
            dialog.dismiss()
        }
    }
}