package com.fake.wastingmoney.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.fake.wastingmoney.R

class StreakAchievementDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Make the dialog background transparent and remove title
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        return inflater.inflate(R.layout.dialog_streak_achievement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lottieTrophyAnimationView = view.findViewById<LottieAnimationView>(R.id.lottieTrophyAnimationView)
        val lottieConfettiAnimationView = view.findViewById<LottieAnimationView>(R.id.lottieConfettiAnimationView)
        val btnAwesome = view.findViewById<Button>(R.id.btnAwesome)

        // Play trophy animation - Ensure your Lottie JSON file is named 'trophy_animation.json' in res/raw
        lottieTrophyAnimationView.setAnimation(R.raw.trophy)
        lottieTrophyAnimationView.playAnimation()

        // Play confetti animation - Ensure your Lottie JSON file is named 'confetti_animation.json' in res/raw
        lottieConfettiAnimationView.setAnimation(R.raw.confetti)
        lottieConfettiAnimationView.playAnimation()


        // Set all streak indicators to filled for a 7-day achievement
        val streakIndicators = listOf<ImageView>(
            view.findViewById(R.id.ivDay1),
            view.findViewById(R.id.ivDay2),
            view.findViewById(R.id.ivDay3),
            view.findViewById(R.id.ivDay4),
            view.findViewById(R.id.ivDay5),
            view.findViewById(R.id.ivDay6),
            view.findViewById(R.id.ivDay7)
        )

        streakIndicators.forEach {
            it.setImageResource(R.drawable.ic_streak_filled)
        }

        btnAwesome.setOnClickListener {
            dismiss() // Dismiss the dialog when "Awesome!" button is clicked
        }
    }
}