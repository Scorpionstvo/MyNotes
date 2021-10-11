package com.example.myproject.project

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.currentnote.R
import com.example.myproject.project.detail.DetailFragment
import com.example.myproject.project.detail.DetailFragmentParams
import com.example.myproject.project.hidden.HiddenNotesFragment
import com.example.myproject.project.list.NotesFragment
import com.example.myproject.project.note.Note
import com.example.myproject.project.password.PasswordFragment
import com.example.myproject.project.password.PasswordFragmentParams
import com.example.myproject.project.trash.TrashFragment
import com.example.myproject.project.util.OnBackPressedListener

class MainActivity : AppCompatActivity(), NotesFragment.OpenFragment, PasswordFragment.OpenFragment,
        HiddenNotesFragment.OpenFragment {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startNotesFragment()
    }

    private fun startNotesFragment() {
        supportFragmentManager.beginTransaction().replace(
                R.id.fl_container,
                NotesFragment.newInstance()
        ).commit()
    }

    override fun openDetailFragment(note: Note, isNew: Boolean, callerFragment: String) {

        supportFragmentManager.beginTransaction().replace(
                R.id.fl_container,
                DetailFragment.newInstance(DetailFragmentParams(note, isNew, callerFragment))
        ).addToBackStack(null).commit()
    }

    override fun openTrashCanFragment() {
        supportFragmentManager.beginTransaction().replace(
                R.id.fl_container,
                TrashFragment.newInstance()
        ).addToBackStack(null).commit()
    }

    override fun openHiddenNotesFragment() {
        supportFragmentManager.beginTransaction().replace(
                R.id.fl_container,
                HiddenNotesFragment.newInstance()
        ).addToBackStack(null).commit()
    }

    override fun openPasswordFragment(isDataChange: Boolean) {
        val dialog = PasswordFragment.newInstance(PasswordFragmentParams(isDataChange))
        dialog.show(supportFragmentManager, "passwordFragment")
    }

    fun send(message: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, message)
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.toSend)))
    }




    override fun onBackPressed() {
        for (fragment: Fragment in supportFragmentManager.fragments) {
            if (fragment is OnBackPressedListener) {
                if (fragment.onBackPressed()) super.onBackPressed()
            }
        }
    }

}
