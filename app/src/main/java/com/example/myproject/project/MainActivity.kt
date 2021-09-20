package com.example.myproject.project

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.example.currentnote.R
import com.example.myproject.project.util.Constants
import com.example.myproject.project.util.DataModel
import com.example.myproject.project.util.OnBackPressedListener


class MainActivity : AppCompatActivity(), NotesFragment.OpenFragment, PasswordFragment.OpenFragment,
    HiddenNotesFragment.OpenFragment {
    private val dataModel: DataModel by viewModels()
    private var picture: String? = null

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val imageUri: Uri?
        if (requestCode == Constants.MAIN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.data
                picture = imageUri.toString()
                if (picture != null) dataModel.imageUri.value = picture
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

     fun putPictureFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            return startActivityForResult(
                Intent.createChooser(intent, "put picture"),
                Constants.MAIN_REQUEST_CODE
            )
        }
    }


    override fun onBackPressed() {
        for (fragment: Fragment in supportFragmentManager.fragments) {
            if (fragment is OnBackPressedListener) {
                if (fragment.onBackPressed()) super.onBackPressed()
            }
        }
    }

}
