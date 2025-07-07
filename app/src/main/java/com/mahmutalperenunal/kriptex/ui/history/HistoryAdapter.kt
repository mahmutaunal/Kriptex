package com.mahmutalperenunal.kriptex.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.data.model.EncryptedText
import com.mahmutalperenunal.kriptex.util.QrUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onAction: (EncryptedText, ActionType) -> Unit
) : ListAdapter<EncryptedText, HistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    enum class ActionType { COPY, SHARE, DELETE }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textOriginal: TextView = itemView.findViewById(R.id.textOriginal)
        val textEncrypted: TextView = itemView.findViewById(R.id.textEncrypted)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val imageQr: ImageView = itemView.findViewById(R.id.imageQr)
        val buttonCopy: View = itemView.findViewById(R.id.llCopy)
        val buttonShare: View = itemView.findViewById(R.id.llShare)
        val layoutRoot: View = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_encrypted_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.textOriginal.text = item.originalText
        holder.textEncrypted.text = item.encryptedText
        holder.textDate.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(item.timestamp))

        val qrImage = QrUtils.generate(item.qrContent, 64, holder.layoutRoot.context)
        holder.imageQr.setImageBitmap(qrImage)

        holder.buttonCopy.setOnClickListener {
            onAction(item, ActionType.COPY)
        }

        holder.buttonShare.setOnClickListener {
            onAction(item, ActionType.SHARE)
        }

        holder.layoutRoot.setOnLongClickListener {
            showPopupMenu(holder.layoutRoot, item)
            true
        }
    }

    private fun showPopupMenu(view: View, item: EncryptedText) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.menu_history_item, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_copy -> onAction(item, ActionType.COPY)
                R.id.action_share -> onAction(item, ActionType.SHARE)
                R.id.action_delete -> onAction(item, ActionType.DELETE)
            }
            true
        }
        popup.show()
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EncryptedText>() {
            override fun areItemsTheSame(oldItem: EncryptedText, newItem: EncryptedText) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: EncryptedText, newItem: EncryptedText) =
                oldItem == newItem
        }
    }
}