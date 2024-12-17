package com.example.recyclercrud2

interface StudentInterface {
    fun delete(position :Int,model: StudentInfo)
    fun update(position: Int,model:StudentInfo)
}