package org.fossify.messages.adapters

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.getTextSize
import org.fossify.messages.R
import org.fossify.messages.activities.SimpleActivity
import org.fossify.messages.databinding.ItemSenderPickerBinding
import org.fossify.messages.models.ShortCodeSender

class SenderPickerAdapter(
    private val activity: SimpleActivity,
    private val selectedAddresses: HashSet<String>,
    private val groupTitles: Map<String, String>,
    private val onSelectionChanged: () -> Unit,
) : RecyclerView.Adapter<SenderPickerAdapter.ViewHolder>() {
    private val textColor = activity.getProperTextColor()
    private val primaryColor = activity.getProperPrimaryColor()
    private val fontSize = activity.getTextSize()
    var senders = ArrayList<ShortCodeSender>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSenderPickerBinding.inflate(activity.layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(senders[position])
    }

    override fun getItemCount() = senders.size

    inner class ViewHolder(private val binding: ItemSenderPickerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sender: ShortCodeSender) {
            val addressKey = sender.address.uppercase()
            val groupTitle = groupTitles[addressKey]
            binding.senderPickerAddress.apply {
                text = sender.address
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize * 1.1f)
            }
            binding.senderPickerDetails.apply {
                text = groupTitle?.let {
                    activity.getString(R.string.in_sender_group, it)
                } ?: sender.snippet
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize * 0.85f)
            }
            binding.senderPickerCheck.apply {
                isChecked = selectedAddresses.contains(addressKey)
                buttonTintList = ColorStateList.valueOf(primaryColor)
            }
            binding.root.setOnClickListener {
                if (selectedAddresses.contains(addressKey)) {
                    selectedAddresses.remove(addressKey)
                } else {
                    selectedAddresses.add(addressKey)
                }
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position)
                }
                onSelectionChanged()
            }
        }
    }
}
