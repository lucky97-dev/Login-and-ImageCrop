package com.example.demoapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*

class AddDetails : AppCompatActivity() {
    lateinit var stepperIndicator: StepperIndicator
    lateinit var infoDetailsHeader: TextView
    lateinit var saveButton: Button
    lateinit var cancelButton: Button
    lateinit var state: Spinner
    lateinit var dist: Spinner
    var state_name=""
    var dist_name=""
    var allHobbies = ""
    var gender_name = ""
    var marital_status_string = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_details)
        saveButton = findViewById<Button>(R.id.saveButton)
        cancelButton = findViewById<Button>(R.id.cancelButton)
        state = findViewById<Spinner>(R.id.state)
        dist = findViewById<Spinner>(R.id.dist)
        stepperIndicator = findViewById<StepperIndicator>(R.id.stepper_indicator)
        infoDetailsHeader = findViewById<TextView>(R.id.infoDetailsHeader)

        state.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener { override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position)
                (parent.getChildAt(0) as TextView).textSize = 15f
                (parent.getChildAt(0) as TextView).setTextColor(Color.parseColor("#4d4d4d"))
                if (state.selectedItemPosition < 1) {
                    state_name = ""
                } else {
                    state_name = item.toString()
                    val toast = Toast.makeText(this@AddDetails, item.toString(), Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        dist.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener { override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position)
                (parent.getChildAt(0) as TextView).textSize = 15f
                (parent.getChildAt(0) as TextView).setTextColor(Color.parseColor("#4d4d4d"))
                if (dist.selectedItemPosition < 1) {
                    dist_name = ""
                } else {
                    dist_name = item.toString()
                    val toast = Toast.makeText(this@AddDetails, item.toString(), Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        saveButton.setOnClickListener {
                if(stepperIndicator.currentStep==0) {
                    stepperIndicator.currentStep = 1
                    findViewById<View>(R.id.linearLayoutPersonalInfo).visibility = View.VISIBLE
                    findViewById<View>(R.id.layoutAddress).visibility = View.GONE
                    cancelButton.text = "Back"
                    infoDetailsHeader.text="Address"
                } else if(stepperIndicator.currentStep==1) {
                    stepperIndicator.currentStep = 2
                    findViewById<View>(R.id.linearLayoutPersonalInfo).visibility = View.GONE
                    findViewById<View>(R.id.layoutAddress).visibility = View.GONE
                    findViewById<View>(R.id.hobbies).visibility = View.VISIBLE
                    saveButton.text = "Save"
                    infoDetailsHeader.text="Hobbies"
                }else if(stepperIndicator.currentStep==2){
                    infoDetailsHeader.text=""
                    stepperIndicator.currentStep = 3
                    findViewById<View>(R.id.linearLayoutPersonalInfo).visibility = View.GONE
                    findViewById<View>(R.id.layoutAddress).visibility = View.GONE
                    findViewById<View>(R.id.hobbies).visibility = View.GONE
                    findViewById<View>(R.id.saveSuccessfully).visibility = View.VISIBLE
                    super.onBackPressed()
                }


        }
        cancelButton.setOnClickListener {
            if (stepperIndicator.currentStep == 1) {
                stepperIndicator.currentStep = 0
                findViewById<View>(R.id.linearLayoutPersonalInfo).visibility = View.GONE
                findViewById<View>(R.id.layoutAddress).visibility = View.VISIBLE
                cancelButton.text = "Cancel"
                infoDetailsHeader.text = "Address"
            } else if (stepperIndicator.currentStep == 2) {
                stepperIndicator.currentStep = 1
                findViewById<View>(R.id.linearLayoutPersonalInfo).visibility = View.VISIBLE
                findViewById<View>(R.id.layoutAddress).visibility = View.GONE
                findViewById<View>(R.id.hobbies).visibility = View.GONE
                cancelButton.text = "Back"
                saveButton.text = "Next"
                infoDetailsHeader.text = "Personal Info"
            }

        }
    }
}