package com.mahmutalperenunal.kriptex.ui.history

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.util.EncryptionType

class FilterBottomSheet(
    selectedTypes: Set<EncryptionType>,
    private val onTypesSelected: (Set<EncryptionType>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var adapter: FilterAdapter
    private var selectedSet = selectedTypes.toMutableSet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_filter, container, false)
        val rvFilters = view.findViewById<RecyclerView>(R.id.rvFilterTypes)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilters)

        adapter = FilterAdapter(requireContext(), EncryptionType.entries.toList(), selectedSet) { type ->
            if (selectedSet.contains(type)) selectedSet.remove(type)
            else selectedSet.add(type)
        }

        rvFilters.layoutManager = LinearLayoutManager(context)
        rvFilters.adapter = adapter

        btnApply.setOnClickListener {
            onTypesSelected(selectedSet)
            dismiss()
        }

        return view
    }

    class FilterAdapter(
        private val context: Context,
        private val items: List<EncryptionType>,
        private val selectedSet: Set<EncryptionType>,
        private val onToggle: (EncryptionType) -> Unit
    ) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val checkbox: CheckBox = view.findViewById(R.id.checkboxFilter)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_filter_type, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val type = items[position]
            holder.checkbox.text = type.getLocalizedLabel(context)
            holder.checkbox.isChecked = selectedSet.contains(type)

            holder.checkbox.setOnCheckedChangeListener(null)
            holder.checkbox.setOnCheckedChangeListener { _, _ ->
                onToggle(type)

                holder.itemView.post {
                    val adapterPosition = holder.adapterPosition
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(adapterPosition)
                    }
                }
            }
        }

        override fun getItemCount() = items.size
    }
}