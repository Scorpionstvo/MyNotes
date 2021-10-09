package com.example.myproject.project.detail

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.*
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currentnote.*
import com.example.currentnote.databinding.FragmentDetailBinding
import com.example.myproject.project.MainActivity
import com.example.myproject.project.application.MyApplication
import com.example.myproject.project.note.Note
import com.example.myproject.project.util.Constants
import com.example.myproject.project.model.DataModel
import com.example.myproject.project.util.OnBackPressedListener
import com.example.myproject.project.wallpaper.Wallpaper
import com.example.myproject.project.wallpaper.WallpaperAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetailFragment : Fragment(), WallpaperAdapter.TryOnWallpaper, OnBackPressedListener {
    private var binding: FragmentDetailBinding? = null
    private val dbManager = MyApplication.dbManager
    private val dataModel: DataModel by activityViewModels()
    lateinit var note: Note
    lateinit var callerFragment: String
    private val wallpapers: ArrayList<Wallpaper> = ArrayList(EnumSet.allOf(Wallpaper::class.java))
    lateinit var adapter: WallpaperAdapter
    var isNew = false
    private val uriImages = ArrayList<String>()
    private var wallpaperName: String? = null


    companion object {
        fun newInstance(params: DetailFragmentParams) = DetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(Constants.PARAMS_DETAIL_FRAGMENT, params)
            }
            note = params.note
            isNew = params.isNew
            callerFragment = params.callerFragment!!
        }
    }

    override fun onResume() {
        super.onResume()
        dbManager.openDb()
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
        dataModel.imageUri.observe(activity as LifecycleOwner) {
            addImage(it)
        }
        adapter = WallpaperAdapter(this, wallpapers)
        initNote()
        initToolbar()
        initBottomNavigationView()
        initRecyclerView()
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
                    saveNote()
                    activity?.onBackPressed()
                }
                R.id.send -> {
                    val message = "${binding!!.etTitle.text} \n ${binding!!.etContent.text}"
                    (activity as MainActivity).send(message)
                }
                R.id.delete -> {
                    deleteNote()
                    activity?.onBackPressed()
                }
            }
            true
        }
    }


    private fun saveNote() {
        val savedTitle = binding!!.etTitle.text.toString()
        val savedContent = binding!!.etContent.text.toString()
        val isTop = note.isTop
        val now = getCurrentTime()
        if (isNew) {
            val newNote = Note(savedTitle, savedContent, 0, now, isTop, wallpaperName)
            if (savedTitle.isEmpty() && savedContent.isEmpty()) return
            dbManager.insertToTable(newNote)
            isNew = false
        } else {
            if (savedTitle != note.title || savedContent != note.content || wallpaperName != note.wallpaperName) {
                val editNote = Note(savedTitle, savedContent, note.id, now, isTop, wallpaperName)
                when (callerFragment) {
                    Constants.NOTES_FRAGMENT -> dbManager.updateItem(editNote)
                    Constants.HIDDEN_FRAGMENT -> dbManager.updateToHiddenTable(editNote)
                }
            }
        }
    }


    private fun deleteNote() {
        val savedTitle = binding!!.etTitle.text.toString()
        val savedContent = binding!!.etContent.text.toString()
        val time = if (savedTitle != note.title || savedContent != note.content) getCurrentTime(
        ) else note.editTime
        val deleteNote = Note(savedTitle, savedContent, note.id, time, note.isTop, wallpaperName)
        dbManager.removeItem(deleteNote)
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
                    (activity as MainActivity).putPictureFromGallery()
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

    private fun addImage(uri: String) {
        val imageUri = uri.toUri()
        val imageSpan = context?.let { it -> ImageSpan(it, imageUri) }
        val builder = SpannableStringBuilder()
        builder.append(binding!!.etContent.text)
        val selStart = binding!!.etContent.selectionStart
        builder.replace(
            binding!!.etContent.selectionStart,
            binding!!.etContent.selectionEnd,
            uri
        )
        builder.setSpan(
            imageSpan,
            selStart,
            selStart + uri.length,
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

    override fun onDestroy() {
        super.onDestroy()
        saveNote()
        dbManager.closeDb()
        binding = null
    }

    override fun onBackPressed(): Boolean {
        return if (binding?.rcWallpapers?.visibility == View.VISIBLE) {
            binding?.rcWallpapers?.visibility = View.GONE
            false
        } else true
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
}







