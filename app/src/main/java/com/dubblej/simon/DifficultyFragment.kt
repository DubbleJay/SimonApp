package com.dubblej.simon

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels


class DifficultyFragment : DialogFragment() {

    private val viewModel: Game by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val difficulties = arrayOf("Hard", "Medium", "Easy")
        var num : Int = viewModel.difficulty.value!!

        return AlertDialog.Builder(requireContext())
            .setTitle("Set Difficulty")
            .setSingleChoiceItems(difficulties, num) { dialog, which ->
                num = which
            }
            .setPositiveButton(android.R.string.ok
            ) { _, _ ->
                viewModel.setDifficulty(num)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        
    }
}