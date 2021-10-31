package com.dubblej.simon

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.telephony.gsm.GsmCellLocation
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import java.lang.ClassCastException
import androidx.lifecycle.ViewModelProvider
import java.util.EnumSet.of


class StartNewGameFragment : DialogFragment() {


    interface OnInputListener {
        fun sendInput(userResponse: Int)
    }
    lateinit var onInputListener : OnInputListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(requireContext()).setTitle("")
            .setMessage("Start a New Game?")

            .setPositiveButton(android.R.string.ok
            ) { _, _ ->
                onInputListener.sendInput(Activity.RESULT_OK)
            }
            .setNegativeButton("Quit") { _, _ ->
                onInputListener.sendInput(Activity.RESULT_CANCELED)
            }

            .create()

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