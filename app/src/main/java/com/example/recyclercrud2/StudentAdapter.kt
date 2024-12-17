package com.example.recyclercrud2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(var context: Context, var students :ArrayList<StudentInfo>, val studentInterface: StudentInterface)
    :RecyclerView.Adapter<StudentAdapter.ViewHolder>() {
    class ViewHolder( view: View) :RecyclerView.ViewHolder(view){
        val name=view.findViewById<TextView>(R.id.tvName)
        val Class =view.findViewById<TextView>(R.id.tvClass)
        val updaeBtn = view.findViewById<Button>(R.id.updateBtn)
        val deleteBtn = view.findViewById<Button>(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.recycler_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return students.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item=students[position]
        holder.name.setText(students[position].name)
        holder.Class.setText(students[position].Class)
        holder.updaeBtn.setOnClickListener{ studentInterface.update(position,item) }
        holder.deleteBtn.setOnClickListener { studentInterface.delete(position,item) }

    }

}