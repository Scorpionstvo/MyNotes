package com.example.myproject.project.trash

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentTrashBinding
import com.example.myproject.project.adapter.NoteAdapter
import com.example.myproject.project.type.Type
import com.example.myproject.project.application.MyApplication
import com.example.myproject.project.note.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class TrashFragment : Fragment(), NoteAdapter.ItemClickListener {
    private var binding: FragmentTrashBinding? = null
    private var trashList = ArrayList<Note>()
    private val adapter = NoteAdapter(this)
    private val dbManager = MyApplication.dbManager
    private var isListView = false
    private var isChecked = true
    lateinit var alertDialog: AlertDialog.Builder
    private var job: Job? = null

    private val type = Type.IS_TRASHED.name

    companion object {
        fun newInstance() = TrashFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrashBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rcTrashList?.adapter = adapter
        recyclerViewStateCreated()
        initToolbar()
        initSearchView()
        initBottomNavigationView()
        initOnBackPressedListener()
        dbManager.openDb()
    }

    private fun recyclerViewStateCreated() {
        if (isListView) {
            binding?.rcTrashList?.layoutManager = GridLayoutManager(context, 1)
            binding?.tbTrashCan?.menu?.findItem(R.id.list)?.icon =
                resources.getDrawable(R.drawable.ic_grid)
        } else {
            binding?.rcTrashList?.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            binding?.tbTrashCan?.menu?.findItem(R.id.list)?.icon =
                resources.getDrawable(R.drawable.ic_list)
        }
    }

    private fun choiceStateRecyclerView(isListView: Boolean) {
        if (isListView) {
            binding!!.rcTrashList.layoutManager = GridLayoutManager(context, 1)
            binding!!.tbTrashCan.menu.findItem(R.id.list).icon = resources.getDrawable(R.drawable.ic_grid)
        } else {
            binding!!.tbTrashCan.menu.findItem(R.id.list).icon = resources.getDrawable(R.drawable.ic_list)
            binding!!.rcTrashList.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun initToolbar() {
        binding!!.tbTrashCan.setNavigationIcon(R.drawable.ic_back)
        binding!!.tbTrashCan.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding!!.tbTrashCan.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.list -> {
                  isListView = !isListView
                    choiceStateRecyclerView(isListView)
                }
                R.id.chooseAll -> {
                    val count = adapter.getCheckedCount()
                    isChecked = count < trashList.size
                    adapter.allChecked(isChecked)
                    val newCount = adapter.getCheckedCount()
                    binding?.tvTrashTitle?.text =
                        resources.getString(R.string.selected) + " $newCount"
                    val isEnabled = newCount > 0
                    bottomMenuEnable(isEnabled)
                }
            }
            true
        }
    }

    private fun initSearchView() {
        binding?.svDeletedNotes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                fillAdapter(newText!!)
                return true
            }
        })
    }


    private fun initBottomNavigationView() {
        binding!!.btMenuTrash.setOnItemSelectedListener {
            val checkedItems = adapter.getCheckedNotes()
            when (it.itemId) {
                R.id.restore -> {
                    for (i in checkedItems) {
                        restore(i)
                    }
                    fillAdapter("")
                    goToNormalView()
                }
                R.id.delete_permanently -> {
                    alertDialog = AlertDialog.Builder(activity)
                    alertDialog.setTitle(R.string.deleting_notes)
                    val noteString =
                        this.resources.getQuantityString(
                            R.plurals.plurals_note_count,
                            checkedItems.size, checkedItems.size
                        )
                    val message = "${resources.getString(R.string.delete_permanently)} $noteString?"
                    alertDialog.setMessage(message)
                    alertDialog.setNegativeButton(
                        R.string.undo
                    )
                    { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialog.setPositiveButton(
                        R.string.ok
                    )
                    { dialog, _ ->
                        for (i in checkedItems) {
                            deletePermanently(i)
                        }
                        fillAdapter("")
                        dialog.dismiss()
                    }
                    goToNormalView()
                    val alert = alertDialog.create()
                    alert.show()
                }
            }
            true
        }
    }

    private fun restore(note: Note) {
        note.typeName = Type.IS_NORMAL.name
        note.removalTime = 0
        dbManager.updateItem(note)
    }

    private fun deletePermanently(note: Note) {
        dbManager.removeItem(note)
    }

    private fun goToNormalView() {
        binding?.btMenuTrash?.visibility = View.GONE
        binding?.tvTrashTitle?.text = ""
        binding?.tbTrashCan?.menu?.clear()
        binding?.tbTrashCan?.inflateMenu(R.menu.list_or_grid_toolbar_menu)
        adapter.isShowCheckBox(false)
    }

    private fun deleteTimer(note: Note): Boolean {
        val calendar = Calendar.getInstance()
        val date = Date(note.removalTime)
        calendar.time = date
        val now = Calendar.getInstance()
        now.add(Calendar.DAY_OF_YEAR, -30)
        return calendar.get(Calendar.DAY_OF_YEAR) < now.get(Calendar.DAY_OF_YEAR) || calendar.get(
            Calendar.YEAR
        ) < now.get(Calendar.YEAR)
    }

    private fun fillAdapter(text: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            trashList = dbManager.readDataFromTable(text, type)
            for (it in trashList) {
                if (deleteTimer(it)) dbManager.removeItem(it)
            }
            adapter.updateAdapter(trashList)
            if (trashList.size > 0) {
                binding?.tvTrashListEmpty?.visibility = View.GONE
            } else {
                binding?.tvTrashListEmpty?.visibility = View.VISIBLE
            }
        }
    }

    override fun onClickItem(note: Note?) {
        if (binding?.btMenuTrash?.visibility == View.VISIBLE) {
            val count = adapter.getCheckedCount()
            binding?.tvTrashTitle?.text = resources.getString(R.string.selected) + " $count"
            if (count > 0) bottomMenuEnable(true) else bottomMenuEnable(false)
        } else {
            Toast.makeText(context, "Невозможно открыть заметку", Toast.LENGTH_LONG).show()
        }
    }

    override fun onLongClickItem() {
        binding?.btMenuTrash?.visibility = View.VISIBLE
        binding?.tvTrashTitle?.text = resources.getString(R.string.select_objects)
        binding?.tbTrashCan?.menu?.clear()
        binding?.tbTrashCan?.inflateMenu(R.menu.choose_all_toolbar_menu)
        adapter.isShowCheckBox(true)
        if (adapter.getCheckedNotes().isEmpty()) bottomMenuEnable(false)
    }

    private fun bottomMenuEnable(isEnabled: Boolean) {
        if (isEnabled) {
            binding!!.btMenuTrash.menu.findItem(R.id.deleteNotes)?.isEnabled = true
            binding!!.btMenuTrash.menu.findItem(R.id.restore)?.isEnabled = true
        } else {
            binding!!.btMenuTrash.menu.findItem(R.id.deleteNotes)?.isEnabled = false
            binding!!.btMenuTrash.menu.findItem(R.id.restore)?.isEnabled = false
            binding!!.btMenuTrash.menu.findItem(R.id.plug).isChecked = true
            binding!!.btMenuTrash.menu.setGroupCheckable(0, false, true)
        }
    }

    override fun onResume() {
        super.onResume()
        fillAdapter("")
    }

    private fun initOnBackPressedListener() {
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding?.btMenuTrash?.visibility == View.VISIBLE) {
                    binding?.btMenuTrash?.visibility = View.GONE
                    binding?.tvTrashTitle?.text =
                        resources.getString(R.string.title_toolbar_trash_fragment)
                    adapter.isShowCheckBox(false)
                    binding?.tbTrashCan?.menu?.clear()
                    binding?.tbTrashCan?.inflateMenu(R.menu.list_or_grid_toolbar_menu)
                } else {
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbManager.closeDb()
        binding = null
    }


}
