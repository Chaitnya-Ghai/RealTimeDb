package com.example.recyclercrud2

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.recyclercrud2.databinding.CustomLayoutBinding
import com.example.recyclercrud2.databinding.FragmentStudentBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    lateinit var uriImg:String
    var dbRefrence:DatabaseReference=FirebaseDatabase.getInstance().reference
    lateinit var binding: FragmentStudentBinding
    lateinit var mainActivity: MainActivity
    private var array = arrayListOf<StudentInfo>()
    lateinit var recyclerAdapter: StudentAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
lateinit var customDialog:CustomLayoutBinding
//
    lateinit var supabaseClient: SupabaseClient
    private val pick_image_request = 1
    private val permisssion_req_code = 100
    private val manage_ext_storage_req = 101
    var imgUri: Uri? = null
//
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity= activity as MainActivity
        supabaseClient = (mainActivity.application as MyApplication).supabaseClient
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
                if (studentInfo != null) {
                array.forEachIndexed{
                    index,studentData ->
                        if (studentData.id==studentInfo.id){
                            array[index]=studentInfo
                        }
                        return@forEachIndexed
                    }
                }
                recyclerAdapter.notifyDataSetChanged()
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
        checkAndRequestPermission()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linearLayoutManager= LinearLayoutManager(mainActivity)
        recyclerAdapter=StudentAdapter(this,array,this)
        binding.recyclerView.layoutManager=linearLayoutManager
        binding.recyclerView.adapter=recyclerAdapter


        binding.fab.setOnClickListener {
            customDialog = CustomLayoutBinding.inflate(mainActivity.layoutInflater)
            val dialog = Dialog(mainActivity).apply {
                setContentView(customDialog.root)
                setCancelable(false)
                window?.setLayout(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT
                )
                show()
            }
            customDialog.imageView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, pick_image_request)
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
                            imgUri.toString(),
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pick_image_request){
            data?.data?.let {
                    uri ->
                imgUri=uri
                uploadImageToSupabase(imgUri!!)
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

        customDialog.imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, pick_image_request)
        }
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
                val info=StudentInfo("",imgUri.toString(),customDialog.edRollNo.text.toString(),customDialog.etName.text.toString(),customDialog.edClass.text.toString())
                dbRefrence.child(model.id?:"").setValue(info).addOnCompleteListener {
                    Toast.makeText(
                        mainActivity,
                        "updated",
                        Toast.LENGTH_SHORT
                    ).show() }
                    .addOnFailureListener {Toast.makeText(mainActivity,"not Updated",Toast.LENGTH_SHORT).show()}
//                recyclerAdapter.notifyDataSetChanged()
//                array[position].name=customDialog.etName.text.toString()
//                array[position].Class=customDialog.edClass.text.toString()
            }
            dialog.dismiss()
        }
    }
    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try{
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent,permisssion_req_code)
            }catch (e: ActivityNotFoundException){
                Toast.makeText(mainActivity, "Activity not Found", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()){
                    //permission granted, proceed
                }else{
                    //ask for permission
                    requestManageExternalStoragePermission()
                }
            }else{
                if(ContextCompat.checkSelfPermission(mainActivity,
                        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestManageExternalStoragePermission()
                }
            }
        }else{
            if (ContextCompat.checkSelfPermission(mainActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(mainActivity,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), permisssion_req_code)
            }
        }
    }

    fun uploadImageToSupabase(imgUri: Uri){
        val byteArray = uriToByteArray(mainActivity,imgUri)
        val fileName ="uploads/${System.currentTimeMillis()}.jpg"
//        document bna deta bucket mai
        val bucket=supabaseClient.storage.from("studentBucket")//choose youe bucket name
//        use lifecycleScope for coroutine Usage
        lifecycleScope.launch(Dispatchers.IO){
            try {
//                upload image and handle the response
                bucket.uploadAsFlow(fileName,byteArray).collect{
                        status->
                    withContext(Dispatchers.Main){
                        when (status){
                            is UploadStatus.Progress ->{
                                Toast.makeText(mainActivity, "Uploading", Toast.LENGTH_SHORT).show()
                            }
                            is UploadStatus.Success ->{
                                val imageUrl = bucket.publicUrl(fileName)
                                val img = customDialog.imageView
                                Glide.with(mainActivity)
                                    .load(imgUri)
                                    .placeholder(R.drawable.add_24)
                                    .into(img)
                                Toast.makeText(mainActivity, "upload Successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            catch (e : Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(mainActivity, "ERROR Uploading Image ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun uriToByteArray(context: Context, uri: Uri):ByteArray{
        val inputStream = context.contentResolver.openInputStream(uri)
        // content resolver uri ki read krne kaa kaam krta hai
        return inputStream?.readBytes() ?: ByteArray(0)
    }


    override fun onClickItem(position: Int, model: StudentInfo) {
        Toast.makeText(mainActivity,"chl rha hai ${position}",Toast.LENGTH_SHORT).show()
        val bundle=bundleOf("name" to model.name, "class" to model.Class , "rollNo" to model.rollNo , "img" to imgUri)
        findNavController().navigate(R.id.detailScreenFragment,bundle)
    }
}