package com.ezralee.bdodonggumbyul.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.emc.verticalweekcalendar.VerticalWeekCalendar
import com.emc.verticalweekcalendar.model.CalendarDay
import com.ezralee.bdodonggumbyul.model.MemoItem
import com.ezralee.bdodonggumbyul.adapter.MainRecyclerAdapter
import com.ezralee.bdodonggumbyul.adapter.SelectedTagAdapter
import com.ezralee.bdodonggumbyul.R
import com.ezralee.bdodonggumbyul.databinding.ActivityMainBinding
import com.ezralee.bdodonggumbyul.dialog.AddBSDialog
import com.ezralee.bdodonggumbyul.model.UserData
import com.ezralee.bdodonggumbyul.retrofit.RetrofitService
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val homeAdapter by lazy { MainRecyclerAdapter(this@MainActivity, memos) }

    val gson = GsonBuilder().create()
    val pref by lazy { this@MainActivity.getSharedPreferences("user_data", Context.MODE_PRIVATE) }
    val editor by lazy { pref.edit() }
    val id: Int by lazy {
        gson.fromJson(
            pref.getString("user_data", ""),
            UserData::class.java
        ).seq.toInt()
    }
    var tagList = ""
    var selectedDay = SimpleDateFormat("yyyy.MM.dd").format(Date())

    var memos = mutableListOf<MemoItem>()

    companion object {
        const val REQUEST_DATE_PICKER = 1024
        const val REQUEST_FILTERED_TAGS = 1994
        const val REQUEST_SELECTED_TAGS = 2023
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        queryDateNTag(id, "", "")
        binding.mainTb.title = "?????? ????????????"

        setHomeRecycler()
        setCalendarRecycler()
        setListenter()

        verifyStoragePermissions(this@MainActivity)
    }//onCreate

    fun getTbTitle(): String {
        return when(binding.mainTb.title.contains(".")){
            true -> binding.mainTb.title.toString()
            else -> ""
        }
    }

    fun setListenter() {
        binding.fabAdd.setOnClickListener {
            val bundle = Bundle()
            if (binding.mainTb.title.contains(".")) openAddBSDialog(getTbTitle(), bundle, 0)
            else openAddBSDialog(selectedDay, bundle, 0)
            true
        }
        binding.swipe.setOnRefreshListener {
            queryDateNTag(id, getTbTitle(), tagList)
            binding.swipe.isRefreshing = false
        }
        binding.mainTb.setOnMenuItemClickListener { toolbarListener(it) }
        binding.etKw.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etKw.windowToken, 0)
                queryKeyword(binding.etKw.text.toString())

                selectedTags.clear()
                selTagAdapter.notifyDataSetChanged()
                binding.mainTb.title = "'${binding.etKw.text}'??? ?????? ??????"
                binding.etKw.visibility = View.GONE
                binding.etKw.text.clear()
                true
            }
            false
        }
    }

    fun setHomeRecycler() {
        binding.homeRv.adapter = homeAdapter

        homeAdapter.setItemClickListener(object : MainRecyclerAdapter.OnClickListener {
            override fun onClick(view: View, position: Int) {
                val bundle = Bundle()
                openAddBSDialog("", bundle, position)
            }
        })
        homeAdapter.setItemLongClickListener(object : MainRecyclerAdapter.OnLongClickListener {
            override fun onLongClick(view: View, position: Int): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("${memos[position].content} ????????? ???????????????. ?????????????????????????")
                    .setPositiveButton("???", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            //?????? ?????? ????????????
                            deleteMemo(memos[position])
                            dialog?.dismiss()
                        }
                    })
                    .setNegativeButton("?????????", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            dialog?.dismiss()
                        }
                    }).show()
                return true
            }
        })
    }

    fun setCalendarRecycler() {
        var selected = GregorianCalendar()

        val vcb = VerticalWeekCalendar.Builder()
            .setView(R.id.cv_vertical)
            .init(this)

        vcb.setOnDateClickListener { y, m, d ->
            selectedDay = "$y.${DecimalFormat("00").format(m + 1)}.${DecimalFormat("00").format(d)}"
            binding.mainTb.title = selectedDay
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
            queryDateNTag(id, selectedDay, tagList)
            Log.d("@@@@@????????? ??????", selectedDay) //month+1 ?????? ?????? ?????? ???????????? ?????????
        }

        var scroll = arrayListOf<Int>()

        vcb.setDateWatcher { y, m, d ->
            if (scroll.size == 9) {
                when {
                    scroll[8] < scroll[0] -> { //?????? ?????????
                        scroll.add(0, scroll[8])
                        scroll.removeAt(8)
                        scroll.removeAt(scroll.size - 1)
                    }
                    scroll[8] > scroll[7] -> { //?????? ?????????
                        scroll.add(d)
                        scroll.removeAt(0)
                    }
                }
            } else if (scroll.size < 9) {
                scroll.add(d)
            }
            when (selected.compareTo(GregorianCalendar(y, m, d))) {
                0 -> CalendarDay.SELECTED
                else -> CalendarDay.DEFAULT
            }
        }
    }

    fun queryKeyword(keyword: String) {
        if (keyword != null) {
            val retrofitService = RetrofitService.newInstance()
            val call = retrofitService.queryKeyword(id, keyword)

            call.enqueue(object : Callback<MutableList<MemoItem>> {
                override fun onResponse(
                    call: Call<MutableList<MemoItem>>,
                    response: Response<MutableList<MemoItem>>
                ) {
                    updateMemos(response)
                }

                override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                    failureMessage(t)
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "????????? ????????? ???????????????", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteMemo(memoItem: MemoItem) {
        val memoid = memoItem.memoId

        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.deleteMemo(id, memoid)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    homeAdapter.notifyDataSetChanged()
                }
                Toast.makeText(this@MainActivity, "????????? ?????????????????????", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@MainActivity, "?????? ????????? ??????????????????. ?????? ??????????????????.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    fun queryDateNTag(id: Int, date: String, tag: String) {
        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.queryDateNTag(id, date, tag)

        call.enqueue(object : Callback<MutableList<MemoItem>> {
            override fun onResponse(
                call: Call<MutableList<MemoItem>>,
                response: Response<MutableList<MemoItem>>
            ) {
                updateMemos(response)
            }

            override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                failureMessage(t)
            }
        })
    }

    var selectedTags = arrayListOf<String>()
    val selTagAdapter = SelectedTagAdapter(selectedTags)

    fun setTagString(intent: String): String{
        val tags = arrayListOf<String>()
        val result = gson.fromJson(intent, tags::class.java)
        var tagList = ""
        if (intent != "") {
            for (i in result) {
                tagList += " #$i"
            }
            this.tagList = tagList
        } else {
            this.tagList = tagList
        }
        return tagList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_DATE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {

                }
            }
            REQUEST_FILTERED_TAGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    binding.etKw.visibility = View.GONE
                    val intent = data?.extras?.getString("tags")
                    if (intent != null) {
                        queryDateNTag(id, getTbTitle(), setTagString(intent))
                        selectedTags.clear()

                        val list = mutableListOf<String>()
                        val tagResult = gson.fromJson(intent, list::class.java)

                        if (tagResult.size > 0 && tagResult[0] != "") {
                            for (i in tagResult) {
                                selectedTags.add(i)
                                selTagAdapter.notifyDataSetChanged()
                            }
                        } else {
                            selectedTags.add("??????")
                            selTagAdapter.notifyDataSetChanged()
                        }

                        binding.rvTag.adapter = selTagAdapter
                        binding.rvTag.visibility = View.VISIBLE
                        selTagAdapter.setFilterTagClickListener(object :
                            SelectedTagAdapter.OnFilterTagClickListener {
                            override fun onClick(view: View, position: Int) {
                                tagList = tagList.replace(" #${selectedTags[position]}","")
                                Log.d("@@@@?????? ?????? ?????? ??????", "$tagList + ${selectedTags[position]}")

                                val retrofitService = RetrofitService.newInstance()
                                val call = retrofitService.queryDateNTag(id, getTbTitle(), tagList)
                                call.enqueue(object : Callback<MutableList<MemoItem>>{
                                    override fun onResponse(call: Call<MutableList<MemoItem>>, response: Response<MutableList<MemoItem>>) {
                                        updateMemos(response)
                                    }

                                    override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                                        failureMessage(t)
                                    }
                                })
                                selectedTags.remove(selectedTags[position])
                                selTagAdapter.notifyDataSetChanged()
                            }
                        })
                    }
                }
            }
        }
    }

    fun getDate(string: String): String {
        if (string.contains(".")) return string
        else return selectedDay
    }

    fun toolbarListener(it: MenuItem): Boolean {
        return when (it.itemId) {
            R.id.tag -> {
                binding.etKw.visibility = View.GONE
                startActivityForResult(
                    Intent(this@MainActivity, SetTagActivity::class.java),
                    REQUEST_FILTERED_TAGS
                )
                true
            }
            R.id.key -> {
                binding.rvTag.visibility = View.INVISIBLE
                selectedTags.clear()
                selTagAdapter.notifyDataSetChanged()
                binding.etKw.visibility =
                    when (binding.etKw.visibility) {
                        View.VISIBLE -> View.GONE
                        else -> View.VISIBLE
                    }
                true
            }
            R.id.date -> {
                binding.drawerLayout.openDrawer(Gravity.LEFT)
                true
            }
            R.id.all -> {
                binding.etKw.visibility = View.GONE
                binding.rvTag.visibility = View.INVISIBLE
                selectedTags.clear()
                selTagAdapter.notifyDataSetChanged()
                queryDateNTag(id, "", "")
                binding.mainTb.title = "?????? ????????????"
                true
            }
            else -> {
                Toast.makeText(this@MainActivity, "error", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun openAddBSDialog(date: String, bundle: Bundle, position: Int) {
        when (date) {
            "" -> { //??? ????????????????????? ????????? ???
                val memoId = memos[position].memoId
                val date = memos[position].date
                val timestamp = memos[position].timestamp
                val tag = " #${memos[position].tag}"
                val content = memos[position].content
                val image = memos[position].image
                val imgUrl = "http://ezra2022.dothome.co.kr/memo/${image}"

                bundle.putString("memoId", memoId)
                bundle.putString("date", date)
                bundle.putString("timestamp", timestamp)
                bundle.putString("tag", tag)
                bundle.putString("content", content)
                if (image != "") bundle.putString("image", imgUrl)
                else bundle.putString("image", "")
            }
            "?????? ????????????" -> { //add ????????? ????????? ??? 11
                bundle.putString("date", selectedDay)
            }
            else -> { //add ????????? ????????? ??? 22
                bundle.putString("date", date)
            }
        }

        val d = AddBSDialog.newInstance()

        d.arguments = bundle
        d.setOnClickListener(object : AddBSDialog.CompleteClickListener {
            override fun onClick(memo: MemoItem) {
                memos.add(memo)
                homeAdapter.notifyDataSetChanged()
            }
        })
        d.show(supportFragmentManager, attributionTag)
    }

    fun updateMemos(response: Response<MutableList<MemoItem>>) {

        Log.d("@@@@????????? ????????? ?????? ???????????????", "${response.body()}")
        memos.clear()
        homeAdapter.notifyDataSetChanged()

        val result = response.body()
        if (result != null && result.size > 0) {
            binding.resultAlert.visibility = View.GONE
            for (i in result) {
                memos.add(0, i)
                homeAdapter.notifyItemInserted(0)
            }
        } else {
            binding.resultAlert.visibility = View.VISIBLE
            binding.resultAlert.text = "?????? ????????? ????????????"
        }
    }

    fun failureMessage(t: Throwable) {
        Log.d("@@?????? ????????? ????????? ??? ??????", "${t.message}")
    }

    //use-permission ????????? ????????? ????????? ???????????? ????????? ?????? ??????
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun verifyStoragePermissions(activity: Activity) {
        val permission =
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }
}