package com.dubblej.simon

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.DialogFragment

import android.os.Bundle

import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import java.lang.ClassCastException


class OptionsFragment : DialogFragment() {

    interface OnInputListener {
        fun buttonPressed(userResponse: Int)
    }
    lateinit var onInputListener : OnInputListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.options_fragment, container)

        val difficultyButton = view.findViewById<Button>(R.id.difficulty_button)
        difficultyButton.setOnClickListener {
            onInputListener.buttonPressed(Activity.RESULT_OK)
        }

        val  startNewGameButton = view.findViewById<Button>(R.id.start_new_game_button)
        startNewGameButton.setOnClickListener {
            onInputListener.buttonPressed(Activity.RESULT_CANCELED)
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