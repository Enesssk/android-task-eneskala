package com.eneskala.androidtaskkotlin.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.eneskala.androidtaskkotlin.R
import com.eneskala.androidtaskkotlin.data.model.Task
import com.eneskala.androidtaskkotlin.databinding.MaincardItemBinding

class TaskAdapter (private val taskList: List<Task>,
    private val onItemClick: (Task) -> Unit, // to click on each item.
    ): RecyclerView.Adapter<TaskAdapter.TaskHolder>() {
    inner class TaskHolder(val binding: MaincardItemBinding) : ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val binding = MaincardItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TaskHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {

        val item = taskList[position]

        holder.binding.taskText.text = item.task
        holder.binding.titleText.text = item.title

        //description can sometimes be null. If it is null, it should not appear empty.
        if(item.description.isNullOrEmpty()){
            holder.binding.descriptionText.text = "-"
        }else {
            holder.binding.descriptionText.text = item.description
        }


        // I access the constraint I created to assign a colorCode to the background of each item.
        val layer = holder.binding.taskConstraint.background as LayerDrawable
        val fillDrawable = layer.findDrawableByLayerId(R.id.fill_shape)
            .mutate() as GradientDrawable
        try {
            fillDrawable.setColor(Color.parseColor(item.colorCode))
        } catch(e: Exception) {
            fillDrawable.setColor(Color.WHITE)
        }

        holder.binding.root.setOnClickListener { onItemClick(item) }  // When I click on each item, I transfer the values of that item.

    }
}