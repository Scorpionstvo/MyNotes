package com.example.myproject.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.currentnote.R
import com.example.myproject.project.detail.DetailFragment
import com.example.myproject.project.detail.DetailFragmentParams
import com.example.myproject.project.hidden.HiddenNotesFragment
import com.example.myproject.project.list.NotesFragment
import com.example.myproject.project.note.Note
import com.example.myproject.project.password.PasswordFragment
import com.example.myproject.project.password.PasswordFragmentParams
import com.example.myproject.project.trash.TrashFragment

class MainActivity : AppCompatActivity(), NotesFragment.OpenFragment, PasswordFragment.OpenFragment,
    HiddenNotesFragment.OpenFragment {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) startNotesFragment()
    }

    private fun addAnimation(transaction: FragmentTransaction) {
       transaction.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right)
    }

    private fun startNotesFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        addAnimation(transaction)
        transaction.replace(
            R.id.fl_container,
            NotesFragment.newInstance()
        ).commit()
    }

    override fun openDetailFragment(note: Note, isNew: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        addAnimation(transaction)
        transaction.replace(
            R.id.fl_container,
            DetailFragment.newInstance(DetailFragmentParams(note, isNew))
        ).addToBackStack(null).commit()
    }

    override fun openTrashCanFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        addAnimation(transaction)
        transaction.replace(
            R.id.fl_container,
            TrashFragment.newInstance()
        ).addToBackStack(null).commit()
    }


    override fun openHiddenNotesFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        addAnimation(transaction)
        transaction.replace(
            R.id.fl_container,
            HiddenNotesFragment.newInstance()
        ).addToBackStack(null).commit()
    }


    override fun openPasswordFragment(isDataChange: Boolean) {
        val dialog = PasswordFragment.newInstance(PasswordFragmentParams(isDataChange))
        dialog.show(supportFragmentManager, PasswordFragment::class.simpleName)
    }

}
