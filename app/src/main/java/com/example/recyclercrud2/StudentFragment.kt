package com.example.recyclercrud2

import android.app.ActionBar
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recyclercrud2.databinding.CustomLayoutBinding
import com.example.recyclercrud2.databinding.FragmentStudentBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudentFragment : Fragment(), StudentInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
//    real time data base
    var dbRefrence:DatabaseReference=FirebaseDatabase.getInstance().reference
    lateinit var binding: FragmentStudentBinding
    lateinit var mainActivity: MainActivity
    private var array = arrayListOf<StudentInfo>()
    lateinit var recyclerAdapter: StudentAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity= activity as MainActivity
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
//        on create mai hee krna yeh
//        add Child Event Listener responsible hotaa delete,add,update, etc kai
        dbRefrence.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val studentInfo:StudentInfo?=snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id = snapshot.key.toString()
                if (studentInfo!=null){
                    array.add(studentInfo)
                    recyclerAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                modification
                val studentInfo:StudentInfo?=snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id=snapshot.key.toString()
                array.forEachIndexed{
                    index,studentData ->
                    if (studentInfo != null) {
                        if (studentData.id==studentInfo.id){
                            array[index]=studentInfo
                            recyclerAdapter.notifyDataSetChanged()
                        }
                        return@forEachIndexed
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val studentInfo:StudentInfo?=snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id=snapshot.key.toString()
                if (studentInfo!=null){
                    array.remove(studentInfo)
                    recyclerAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        binding=FragmentStudentBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linearLayoutManager= LinearLayoutManager(mainActivity)
        recyclerAdapter=StudentAdapter(mainActivity,array,this)
        binding.recyclerView.layoutManager=linearLayoutManager
        binding.recyclerView.adapter=recyclerAdapter
        binding.fab.setOnClickListener {
            val customDialog = CustomLayoutBinding.inflate(mainActivity.layoutInflater)
            val dialog = Dialog(mainActivity).apply {
                setContentView(customDialog.root)
                setCancelable(false)
                window?.setLayout(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT
                )
                show()
            }
                customDialog.okBtn.setOnClickListener {
                    if (customDialog.edRollNo.text.toString().isNullOrBlank()){
                        customDialog.edRollNo.error="Enter roll no"
                    }
                   else if (customDialog.edClass.text.toString().isNullOrBlank()){
                        customDialog.edClass.error="Enter your class"
                    }
                   else if (customDialog.etName.text.toString().isNullOrBlank()){
                        customDialog.etName.error="Enter your name"
                    }
                    else{
                        val info =StudentInfo(
                            "",
                            customDialog.edRollNo.text.toString(),
                            customDialog.etName.text.toString(),
                            customDialog.edClass.text.toString())
                        dbRefrence.push().setValue(info).addOnCompleteListener {
                            Toast.makeText(mainActivity, "done", Toast.LENGTH_SHORT).show()
                        }
                            .addOnFailureListener {
                                Toast.makeText(mainActivity, "Failure", Toast.LENGTH_SHORT).show()
                                println("${it.message}")
                            }
//                        array.add(info)
                        dialog.dismiss()
                    }
                }
                customDialog.cancelBtn.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudentFrag.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun delete(position: Int, model: StudentInfo) {
//        array.removeAt(position)
        dbRefrence.child(model.id?:"").removeValue()
//        recyclerAdapter.notifyDataSetChanged()
    }

    override fun update(position: Int, model: StudentInfo) {
        val customDialog = CustomLayoutBinding.inflate(layoutInflater)
        val dialog = Dialog(mainActivity).apply {
            setContentView(customDialog.root)
            window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT)
            show()
        }
        val updateText="Update"
        customDialog.okBtn.text = updateText
// passing old values
        customDialog.etName.setText(model.name)
        customDialog.edClass.setText(model.Class)
        customDialog.edRollNo.setText(model.rollNo)

        customDialog.cancelBtn.visibility=View.GONE
        customDialog.okBtn.setOnClickListener {
            if (customDialog.edClass.text.toString().isNullOrBlank()){
                customDialog.edClass.error="Enter your class"
            }
            if (customDialog.etName.text.toString().isNullOrBlank()){
                customDialog.etName.error="Enter your name"
            }
            if (customDialog.edRollNo.text.toString().isNullOrBlank()){
                customDialog.edRollNo.error="Enter your name"
            }
            else{
                val info=StudentInfo("",customDialog.edRollNo.text.toString(),customDialog.etName.text.toString(),customDialog.edClass.text.toString())
                dbRefrence.child(model.id?:"").setValue(info).addOnCompleteListener {
                    Toast.makeText(
                        mainActivity,
                        "updated",
                        Toast.LENGTH_SHORT
                    ).show() }
                    .addOnFailureListener { Toast.makeText(mainActivity, "not Updated", Toast.LENGTH_SHORT).show() }
//                recyclerAdapter.notifyDataSetChanged()
//                array[position].name=customDialog.etName.text.toString()
//                array[position].Class=customDialog.edClass.text.toString()
            }
            dialog.dismiss()
        }
    }

    override fun onClickItem(position: Int, model: StudentInfo) {
        Toast.makeText(mainActivity, "chl rha hai ${position}", Toast.LENGTH_SHORT).show()
        val bundle=bundleOf("name" to model.name, "class" to model.Class , "rollNo" to model.id)
        findNavController().navigate(R.id.action_studentFrag_to_detailScreenFragment,bundle)

    }
}