package com.example.myproject.project.password

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentPasswordBinding
import com.example.myproject.project.util.Constants

const val SHARED_PREFERENCES_NAME = "shared preferences name"
const val PASSWORD_KEY = "password key"
const val EMAIL_KEY = "email key"

class PasswordFragment : DialogFragment() {
    private var binding: FragmentPasswordBinding? = null
    var password: String? = null
    private var email: String? = null
    private var isEntry = false
    private var isVisiblePassword = false
    private var isDataChange = false

    interface OpenFragment {
        fun openHiddenNotesFragment()
    }

    companion object {
        fun newInstance(params: PasswordFragmentParams) = PasswordFragment().apply {
            arguments = Bundle().apply {
                putParcelable(Constants.PARAMS_PASSWORD_FRAGMENT, params)
            }
            isDataChange = params.isDataChange
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPasswordBinding.inflate(layoutInflater)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences =
            activity?.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        password = sharedPreferences!!.getString(PASSWORD_KEY, null)
        email = sharedPreferences.getString(EMAIL_KEY, null)
        isEntry = !password.equals(null)

        determinateViewState(isEntry)
    }

    private fun determinateViewState(isEntry: Boolean) {
        if (isDataChange) {
            binding?.linEntry?.visibility = View.GONE
            binding?.linCreatePassword?.visibility = View.VISIBLE
            initUpdateData()
        } else {
            if (isEntry) {
                binding?.linEntry?.visibility = View.VISIBLE
                binding?.linCreatePassword?.visibility = View.GONE
                initEntry()
            } else {
                binding?.linEntry?.visibility = View.GONE
                binding?.linCreatePassword?.visibility = View.VISIBLE

                initCreatePassword()
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageButton: ImageButton) {
        if (isVisiblePassword) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageButton.setImageResource(R.drawable.ic_visibility_off)
        } else {
            editText.transformationMethod = null
            imageButton.setImageResource(R.drawable.ic_visibility_on)
        }
        isVisiblePassword = !isVisiblePassword
    }

    private fun initEntry() {
        binding!!.ibSee.setOnClickListener {
            togglePasswordVisibility(binding!!.etEntry, binding!!.ibSee)
        }

        binding!!.bEntry.setOnClickListener {

            if (binding!!.etEntry.text.toString() == password) {
                (activity as OpenFragment).openHiddenNotesFragment()
                dialog?.dismiss()
            } else {
                if (binding!!.etEntry.text.toString().isEmpty()) {
                    binding!!.tvWarningEntry.text = resources.getString(R.string.enter_password)
                } else {
                    binding!!.tvWarningEntry.text = resources.getString(R.string.wrong_password)
                }
            }
        }

        binding!!.bRestorePassword.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle(R.string.password_recovery)
            alertDialog.setMessage(R.string.send_password_to_mail)
            alertDialog.setNegativeButton(
                R.string.undo
            ) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.setPositiveButton(
                R.string.ok
            ) { dialog, _ ->
                Toast.makeText(context, R.string.check_your_email, Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            val alert = alertDialog.create()
            alert.show()
        }
    }

    private fun isValidEmail(mail: CharSequence): Boolean {
        return !TextUtils.isEmpty(mail) && android.util.Patterns.EMAIL_ADDRESS.matcher(mail)
            .matches()

    }

    private fun initCreatePassword() {
        binding!!.bSavePassword.setOnClickListener {
            val writtenEmail = binding!!.etWriteEmail.text.toString()
            val createdPassword = binding!!.etCreatedPassword.text.toString()
            if (!isValidEmail(writtenEmail)) {
                if (writtenEmail.isEmpty()) {

                    binding!!.tvWarning.text = resources.getString(R.string.write_your_email)
                } else {
                    binding!!.tvWarning.text =
                        resources.getString((R.string.this_email_does_not_exist))
                }
            } else {
                if (createdPassword.length < 4) {
                    binding!!.tvWarning.text = resources.getString(R.string.password_is_empty)
                } else {
                    saveData()
                    (activity as OpenFragment).openHiddenNotesFragment()

                    dialog?.dismiss()
                }
            }
        }

        binding!!.bCancel.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun saveData() {
        val newEmail = binding!!.etWriteEmail.text.toString().trim()
        val newPassword = binding!!.etCreatedPassword.text.toString()
        val savedVariant =
            activity?.getSharedPreferences(
                SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
                ?.edit()
        savedVariant?.putString(PASSWORD_KEY, newPassword)
        savedVariant?.putString(EMAIL_KEY, newEmail)
        savedVariant?.apply()
        Toast.makeText(
            context,
            R.string.data_saved,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun initUpdateData() {
        binding!!.tvWriteEmail.visibility = View.GONE
        binding!!.tvCreatedPassword.visibility = View.GONE
        binding!!.etWriteEmail.setText(email)
        binding!!.etCreatedPassword.setText(password)
        binding!!.bSavePassword.setOnClickListener {

            val writtenEmail = binding!!.etWriteEmail.text.toString()
            val createdPassword = binding!!.etCreatedPassword.text.toString()
            if (!isValidEmail(writtenEmail)) {
                if (writtenEmail.isEmpty()) {
                    binding!!.tvWarning.text = resources.getString(R.string.write_your_email)
                } else {
                    binding!!.tvWarning.text =
                        resources.getString((R.string.this_email_does_not_exist))
                }
            } else {
                if (createdPassword.isEmpty()) {
                    binding!!.tvWarning.text = resources.getString(R.string.password_is_empty)
                } else {
                    saveData()
                    dialog?.dismiss()
                }

            }

        }
    }
}
