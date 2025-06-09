package com.fake.wastingmoney.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.fake.wastingmoney.R

class WelcomeConfettiDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Make the dialog background transparent and remove title
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        return inflater.inflate(R.layout.dialog_welcome_confetti, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lottieConfettiAnimationView = view.findViewById<LottieAnimationView>(R.id.lottieWelcomeConfettiAnimationView)
        val btnGotIt = view.findViewById<Button>(R.id.btnGotIt)

        // Play confetti animation
        // Ensure you have confetti_animation.json in your res/raw folder
        lottieConfettiAnimationView.setAnimation(R.raw.confetti)
        lottieConfettiAnimationView.playAnimation()

        btnGotIt.setOnClickListener {
            dismiss() // Dismiss the dialog when "Got It!" button is clicked
        }
    }
}