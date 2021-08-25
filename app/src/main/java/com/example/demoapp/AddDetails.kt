package com.example.demoapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class AddDetails : AppCompatActivity() {
    lateinit var stepperIndicator: StepperIndicator
    lateinit var infoDetailsHeader: TextView
    lateinit var saveButton: Button
    lateinit var cancelButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_details)
        saveButton = findViewById<Button>(R.id.saveButton)
        cancelButton = findViewById<Button>(R.id.cancelButton)
        stepperIndicator = findViewById<StepperIndicator>(R.id.stepper_indicator)
        infoDetailsHeader = findViewById<TextView>(R.id.infoDetailsHeader)
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