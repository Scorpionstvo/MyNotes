package com.example.myproject.project.trash

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.currentnote.R
import com.example.currentnote.databinding.FragmentTrashBinding
import com.example.myproject.project.adapter.NoteAdapter
import com.example.myproject.project.type.Type
import com.example.myproject.project.model.DataModel
import com.example.myproject.project.data.Note
import com.example.myproject.project.util.Constants
import kotlin.collections.HashSet

class TrashFragment : Fragment(), NoteAdapter.ItemClickListener {
    private var binding: FragmentTrashBinding? = null
    private val dataModel: DataModel by viewModels()
    private val adapter = NoteAdapter(this)
    private var isListView = false

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
        initDataModelContract()
        recyclerViewStateCreated()
        initToolbar()
        initSearchView()
        initBottomNavigationView()
        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    private fun initDataModelContract() {
        dataModel.noteItemList.observe(viewLifecycleOwner, {
            adapter.updateAdapter(it)
            binding?.tvTrashListEmpty?.visibleIf(it.isEmpty())
        })
    }

    private fun View.visibleIf(show: Boolean) {
        visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun recyclerViewStateCreated() {
        val share =
            activity?.getSharedPreferences(
                Constants.SHARED_PREF_NAME_NOTES_FRAGMENT,
                Context.MODE_PRIVATE
            )
        isListView = share!!.getBoolean(Constants.SHARED_PREF_KEY_NOTES_FRAGMENT, false)
        choiceStateRecyclerView(isListView)
    }

    private fun choiceStateRecyclerView(isListView: Boolean) {
        if (isListView) {
            binding!!.rcTrashList.layoutManager = GridLayoutManager(context, 1)
            binding!!.tbTrashCan.menu.findItem(R.id.list).icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_grid)
        } else {
            binding!!.tbTrashCan.menu.findItem(R.id.list).icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_list)
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
                    saveTableVariant(isListView)
                }
                R.id.chooseAll -> {
                    val check = dataModel.getCheckedId().size < dataModel.noteItemList.value!!.size
                    dataModel.allChecked(check)
                    it.changeColor(check)
                    val newCount = dataModel.getCheckedId().size
                    binding?.tvTrashTitle?.text =
                        resources.getString(R.string.selected) + " $newCount"
                    bottomMenuEnable(newCount > 0)
                    val isAnchor = dataModel.defineAnchor()
                    if (isAnchor) {
                        changeIcon(true)
                    } else {
                        if (newCount != 0) {
                            changeIcon(false)
                        }
                    }
                }
            }
            true
        }
    }


    private fun MenuItem.changeColor(color: Boolean) {
        if (color) icon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ContextCompat.getColor(
                requireContext(), R.color.blue
            ), BlendModeCompat.SRC_ATOP
        )
        else icon.clearColorFilter()
    }

    private fun changeIcon(isAnchor: Boolean) {
        val menuItem = binding?.btMenuTrash?.menu?.findItem(R.id.pinToTopOfList)
        if (isAnchor) {
            menuItem?.setIcon(R.drawable.ic_pin)
            menuItem?.setTitle(R.string.anchor)
        } else {
            menuItem?.setIcon(R.drawable.ic_unfasten)
            menuItem?.setTitle(R.string.unfasten)
        }
    }

    private fun saveTableVariant(isListView: Boolean) {
        val savedVariant =
            activity?.getSharedPreferences(
                Constants.SHARED_PREF_NAME_NOTES_FRAGMENT,
                Context.MODE_PRIVATE
            )
                ?.edit()
        savedVariant?.putBoolean(Constants.SHARED_PREF_KEY_NOTES_FRAGMENT, isListView)
        savedVariant?.apply()
    }

    private fun initSearchView() {
        binding?.svDeletedNotes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                dataModel.getAdapterItemList(newText!!, type)
                return true
            }
        })
    }


    private fun initBottomNavigationView() {
        binding!!.btMenuTrash.setOnItemSelectedListener {
            val checkedId: HashSet<Int> = dataModel.getCheckedId()
            when (it.itemId) {
                R.id.restore -> {
                    checkedId.forEach { id ->
                        dataModel.movieToNormal(id)
                        adapter.notifyItemRemoved(id)
                    }
                    dataModel.getAdapterItemList("", type)
                    goToNormalView()
                }
                R.id.delete_permanently -> {
                    val alertDialog = AlertDialog.Builder(activity)
                    alertDialog.setTitle(R.string.deleting_notes)
                    val noteString =
                        this.resources.getQuantityString(
                            R.plurals.plurals_note_count,
                            checkedId.size, checkedId.size
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
                        checkedId.forEach { id ->
                            dataModel.deleteNote(id)
                            adapter.notifyItemRemoved(id)
                        }
                        dialog.dismiss()
                        goToNormalView()
                        dataModel.getAdapterItemList("", type)
                    }
                    val alert = alertDialog.create()
                    alert.show()
                }

            }
            true
        }
    }


    private fun goToNormalView() {
        binding?.btMenuTrash?.visibility = View.GONE
        binding?.tvTrashTitle?.text = resources.getString(R.string.title_toolbar_trash_fragment)
        binding?.tbTrashCan?.menu?.clear()
        binding?.tbTrashCan?.inflateMenu(R.menu.list_or_grid_toolbar_menu)
        adapter.isShowCheckBox(false)
        dataModel.allChecked(false)
        binding?.tbTrashCan?.menu?.findItem(R.id.chooseAll)?.changeColor(false)
    }


    override fun onClickItem(note: Note?) = if (binding?.btMenuTrash?.visibility == View.VISIBLE) {
        dataModel.updateCheckedList(note!!.id)
        dataModel.getAdapterItemList("", type)
        val count = dataModel.getCheckedId().size
        binding?.tvTrashTitle?.text = resources.getString(R.string.selected) + " $count"
        if (count > 0) bottomMenuEnable(true) else bottomMenuEnable(false)
    } else {
        Toast.makeText(context, R.string.click_trash_item, Toast.LENGTH_LONG).show()
    }

    override fun onLongClickItem() {
        binding?.btMenuTrash?.visibility = View.VISIBLE
        binding?.tvTrashTitle?.text = resources.getString(R.string.select_objects)
        binding?.tbTrashCan?.menu?.clear()
        binding?.tbTrashCan?.inflateMenu(R.menu.choose_all_toolbar_menu)
        adapter.isShowCheckBox(true)
        if (dataModel.getCheckedId().isEmpty()) bottomMenuEnable(false)
    }

    private fun bottomMenuEnable(isEnabled: Boolean) {
        if (isEnabled) {
            binding!!.btMenuTrash.menu.findItem(R.id.delete_permanently)?.isEnabled = true
            binding!!.btMenuTrash.menu.findItem(R.id.restore)?.isEnabled = true
        } else {
            binding!!.btMenuTrash.menu.findItem(R.id.delete_permanently)?.isEnabled = false
            binding!!.btMenuTrash.menu.findItem(R.id.restore)?.isEnabled = false
            binding!!.btMenuTrash.menu.findItem(R.id.plug).isChecked = true
            binding!!.btMenuTrash.menu.setGroupCheckable(0, false, true)
        }
    }


    private val callback = object : OnBackPressedCallback(true) {
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
    }

    override fun onResume() {
        super.onResume()
        dataModel.getAdapterItemList("", type)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback.remove()
        binding?.rcTrashList?.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
