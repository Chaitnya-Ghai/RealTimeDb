package com.example.recyclercrud2

data class StudentInfo(
    var id: String,
    var rollNo: String,
    var name: String,
    var Class: String
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "")
}

