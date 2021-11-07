package com.example.myproject.project.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currentnote.*
import com.example.currentnote.databinding.FragmentDetailBinding
import com.example.myproject.project.type.Type
import com.example.myproject.project.application.MyApplication
import com.example.myproject.project.note.Note
import com.example.myproject.project.util.Constants
import com.example.myproject.project.wallpaper.Wallpaper
import com.example.myproject.project.wallpaper.WallpaperAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetailFragment : Fragment(), WallpaperAdapter.TryOnWallpaper {
    private var binding: FragmentDetailBinding? = null
    private val dbManager = MyApplication.dbManager
    lateinit var note: Note
    private val wallpapers: ArrayList<Wallpaper> = ArrayList(EnumSet.allOf(Wallpaper::class.java))
    private val adapter = WallpaperAdapter(this, wallpapers)
    private var isNew = false
    private val uriList = ArrayList<Uri>()
    private var wallpaperName: String? = null

    companion object {
        fun newInstance(params: DetailFragmentParams) = DetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(Constants.PARAMS_DETAIL_FRAGMENT, params)
            }
            note = params.note
            isNew = params.isNew
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNote()
        initToolbar()
        initBottomNavigationView()
        initRecyclerView()
        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    private fun initNote() {
        binding?.etTitle?.setText(note.title)
        binding?.etContent?.setText(note.content)
        wallpaperName = note.wallpaperName
        if (wallpaperName != null) onClickElement(Wallpaper.valueOf(wallpaperName.toString()))
    }

    private fun initToolbar() {
        binding?.tbDetail?.setNavigationIcon(R.drawable.ic_back)
        binding!!.tbDetail.setNavigationOnClickListener {
            if (binding?.rcWallpapers?.visibility == View.GONE) {
                activity?.onBackPressed()
            } else {
                binding?.rcWallpapers?.visibility = View.GONE
            }
        }
        binding!!.tbDetail.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save -> {
                    saveNote(note.typeName)
                    activity?.onBackPressed()
                }
                R.id.send -> {
                    val message = "${binding!!.etTitle.text} \n ${binding!!.etContent.text}"
                    send(message)
                }
                R.id.delete -> {
                    saveNote(Type.IS_TRASHED.name)
                    activity?.onBackPressed()
                }
            }
            true
        }
    }

    private fun saveNote(typeName: String) {
        if (note.typeName == Type.IS_TRASHED.name) return
        val savedTitle = binding!!.etTitle.text.toString()
        val savedContent = binding!!.etContent.text.toString()
        val now = getCurrentTime()
        if (isNew) {
            if (savedTitle.isEmpty() && savedContent.isEmpty()) return
            note.typeName = typeName
            note.title = savedTitle
            note.content = savedContent
            note.editTime = now
            note.wallpaperName = wallpaperName
            if (typeName == Type.IS_TRASHED.name) note.removalTime = System.currentTimeMillis()
            dbManager.insertToTable(note)
            isNew = false
        } else {
            if (savedTitle != note.title || savedContent != note.content || wallpaperName != note.wallpaperName) {
                note.editTime = now
            }
            note.typeName = typeName
            note.title = savedTitle
            note.content = savedContent
            note.wallpaperName = wallpaperName
            if (typeName == Type.IS_TRASHED.name) note.removalTime = System.currentTimeMillis()
            dbManager.updateItem(note)
        }
    }


    private fun send(message: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, message)
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.toSend)))
    }


    private fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat(Constants.DATE_FORMAT)
        return formatter.format(currentTime)
    }

    private fun initBottomNavigationView() {
        binding!!.bnvDetail.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.addImage -> {
                    putPictureFromGallery()
                }
                R.id.check -> {
                }
                R.id.changeBackground -> {
                    binding?.rcWallpapers?.visibility = View.VISIBLE
                }
            }
            true
        }
    }

    private fun putPictureFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                context?.contentResolver?.takePersistableUriPermission(
                    imageUri!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                if (imageUri != null) addImage(imageUri)
            }
        }

    private fun addImage(imageUri: Uri) {
        uriList.add(imageUri)
        val imageSpan = context?.let { it -> ImageSpan(it, imageUri) }
        val builder = SpannableStringBuilder()
        builder.append(binding!!.etContent.text)
        val selStart = binding!!.etContent.selectionStart
        builder.replace(
            binding!!.etContent.selectionStart,
            binding!!.etContent.selectionEnd,
            imageUri.toString()
        )
        builder.setSpan(
            imageSpan,
            selStart,
            selStart + imageUri.toString().length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding!!.etContent.text = builder
    }

    private fun initRecyclerView() {
        binding?.rcWallpapers?.adapter = adapter
        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding?.rcWallpapers?.layoutManager = linearLayoutManager
    }

    override fun onResume() {
        super.onResume()
        dbManager.openDb()
    }

    override fun onClickElement(wallpaper: Wallpaper) {
        if (wallpaper == Wallpaper.INITIAL || wallpaper == null) {
            binding!!.etTitle.setTextColor(resources.getColor(R.color.white))
            binding!!.etContent.setTextColor(resources.getColor(R.color.white))
            binding!!.constDetail.setBackgroundColor(0)
            binding!!.lDetail.setPadding(0, 0, 0, 0)
            binding!!.bnvDetail.setBackgroundResource(0)
            binding!!.rcWallpapers.setBackgroundResource(0)
            wallpaperName = null
        } else {
            binding!!.etTitle.setTextColor(resources.getColor(wallpaper.textColor))
            binding!!.etContent.setTextColor(resources.getColor(wallpaper.textColor))
            binding!!.constDetail.setBackgroundResource(wallpaper.primaryBackground)
            binding!!.lDetail.setPadding(wallpaper.padding, wallpaper.padding, wallpaper.padding, 0)
            binding!!.bnvDetail.setBackgroundResource(wallpaper.secondaryBackground)
            binding!!.rcWallpapers.setBackgroundResource(wallpaper.secondaryBackground)
            wallpaperName = wallpaper.name
        }
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding?.rcWallpapers?.visibility == View.VISIBLE) {
                binding?.rcWallpapers?.visibility = View.GONE
            } else {
                isEnabled = false
                saveNote(note.typeName)
                activity?.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbManager.closeDb()
        binding = null
    }
}








