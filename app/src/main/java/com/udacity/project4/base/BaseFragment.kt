package com.udacity.project4.base

import android.content.DialogInterface
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val _viewModel: BaseViewModel

    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showToast.observe(this, Observer {
            Toast.makeText(requireActivity(), it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showSnackBar.observe(this, Observer {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showSnackBarInt.observe(this, Observer {
            Snackbar.make(requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        })

        _viewModel.navigationCommand.observe(this, Observer { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        })
    }

    fun showConfirmationAlert(@StringRes title: Int, @StringRes message: Int, buttonListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(android.R.string.ok, buttonListener)
            setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
        }.create().show()
    }
}