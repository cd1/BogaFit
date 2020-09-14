package com.gmail.cristiandeives.bogafit

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@MainThread
class SelectGenderDialogFragment : DialogFragment(),
    DialogInterface.OnClickListener {

    private val args by navArgs<SelectGenderDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "> onCreateDialog(savedInstanceState=$savedInstanceState)")

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_gender_label)
            .setSingleChoiceItems(R.array.genders, args.genderIndex, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        Log.d(TAG, "< onCreateDialog(savedInstanceState=$savedInstanceState): $dialog")
        return dialog
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        Log.v(TAG, "> onClick(dialog=$dialog, which=$which)")

        val selectedGender = Gender.values()[which]

        Log.i(TAG, "user selected gender $selectedGender")
        val result = bundleOf(
            ProfileFragment.BUNDLE_KEY_GENDER to selectedGender,
        )
        setFragmentResult(ProfileFragment.REQUEST_KEY_SELECT_GENDER, result)

        dialog.dismiss()

        Log.v(TAG, "< onClick(dialog=$dialog, which=$which)")
    }

    companion object {
        private val TAG = SelectGenderDialogFragment::class.java.simpleName
    }
}