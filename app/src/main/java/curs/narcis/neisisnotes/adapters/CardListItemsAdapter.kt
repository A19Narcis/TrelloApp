package curs.narcis.neisisnotes.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import curs.narcis.neisisnotes.R
import curs.narcis.neisisnotes.activities.TaskListActivity
import curs.narcis.neisisnotes.models.Card
import curs.narcis.neisisnotes.models.SelectedMembers

open class CardListItemsAdapter(private val context: Context, private var list: ArrayList<Card>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card, parent, false))
    }

    @SuppressLint("CutPasteId")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            if (model.labelColor.isNotEmpty()){
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.VISIBLE
                holder.itemView.findViewById<View>(R.id.view_label_color).setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
            }
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name

            if ((context as TaskListActivity).mAssignedMembersDetailsList.size > 0){
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
                for (i in context.mAssignedMembersDetailsList.indices){
                    for (j in model.assignedTo){
                        if (context.mAssignedMembersDetailsList[i].id == j){
                            context.mAssignedMembersDetailsList[i].id?.let { context.mAssignedMembersDetailsList[i].image?.let { it1 -> SelectedMembers(it, it1) } }?.let { selectedMembersList.add(it) }
                        }
                    }
                }

                if (selectedMembersList.size > 0){
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.VISIBLE
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).layoutManager = GridLayoutManager(context, 4)
                    val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).adapter = adapter
                    adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                        override fun onClick() {
                            if (onClickListener != null){
                                onClickListener!!.onClick(position)
                            }
                        }
                    })

                } else {
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                onClickListener?.onClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}