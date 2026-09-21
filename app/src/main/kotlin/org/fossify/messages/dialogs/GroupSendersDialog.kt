package org.fossify.messages.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.extensions.showKeyboard
import org.fossify.commons.extensions.toast
import org.fossify.messages.R
import org.fossify.messages.databinding.DialogRenameConversationBinding

class GroupSendersDialog(
    private val activity: Activity,
    private val prefilledName: String,
    private val callback: (name: String) -> Unit,
) {
    private var dialog: AlertDialog? = null

    init {
        val binding = DialogRenameConversationBinding.inflate(activity.layoutInflater).apply {
            renameConvEditText.setText(prefilledName)
            renameConvEditText.setSelection(prefilledName.length)
            renameConvInputLayout.hint = activity.getString(R.string.group_name)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.fossify.commons.R.string.ok, null)
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.group_senders) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.showKeyboard(binding.renameConvEditText)
                    alertDialog.getButton(BUTTON_POSITIVE).apply {
                        setOnClickListener {
                            val newTitle = binding.renameConvEditText.text.toString().trim()
                            if (newTitle.isEmpty()) {
                                activity.toast(org.fossify.commons.R.string.empty_name)
                                return@setOnClickListener
                            }

                            callback(newTitle)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
