package com.dubblej.simon

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import java.lang.ClassCastException

class EndGameFragment : DialogFragment() {

    private val viewModel: Game by activityViewModels()

    interface OnInputListener {
        fun sendInput(userResponse: Int)
    }
    lateinit var onInputListener : OnInputListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        isCancelable = false

        val view = inflater.inflate(R.layout.end_game_fragment, container)

        val finalScoreTextView = view.findViewById<TextView>(R.id.final_score_text_view)
        finalScoreTextView.text = "Final Score: " + viewModel.userScore.value.toString()

        val startNewGameButton = view.findViewById<Button>(R.id.start_new_game_button)
        startNewGameButton.setOnClickListener {
            onInputListener.sendInput(Activity.RESULT_OK)
        }

        val  quitButton = view.findViewById<Button>(R.id.quit_game_button)
        quitButton.setOnClickListener {
            onInputListener.sendInput(Activity.RESULT_CANCELED)
        }

        return view
    }
    
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            onInputListener = activity as OnInputListener
        } catch (ex : ClassCastException) {
            throw RuntimeException("$context must implement interface listener")
        }
    }
}