package com.example.bdodonggumbyul.activity

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
import com.example.bdodonggumbyul.MemoItem
import com.example.bdodonggumbyul.R
import com.example.bdodonggumbyul.adapter.MainRecyclerAdapter
import com.example.bdodonggumbyul.adapter.SelectedTagAdapter
import com.example.bdodonggumbyul.databinding.ActivityMainBinding
import com.example.bdodonggumbyul.dialog.AddBSDialog
import com.example.bdodonggumbyul.model.UserData
import com.example.bdodonggumbyul.retrofit.RetrofitService
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
    val id: Int by lazy { gson.fromJson(pref.getString("user_data", ""), UserData::class.java).seq.toInt() }
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

        queryDateNTag(id,"","")
        binding.mainTb.title = "메모 전체보기"

        setHomeRecycler()
        setCalendarRecycler()
        setListenter()
        reloadData()

        verifyStoragePermissions(this@MainActivity)

        if (!binding.mainTb.title.contains(".")) queryDateNTag(id, "", tagList)
        else queryDateNTag(id, binding.mainTb.title.toString(), tagList)
    }//onCreate

    fun setListenter() {
        binding.mainTb.setOnMenuItemClickListener { toolbarListener(it) }
        binding.etKw.setOnKeyListener { v, keyCode, event ->
            if (event.action ==KeyEvent.ACTION_DOWN && keyCode ==KeyEvent.KEYCODE_ENTER){
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etKw.windowToken, 0)
                queryKeyword(binding.etKw.text.toString())

                selectedTags.clear()
                selTagAdapter.notifyDataSetChanged()
                binding.mainTb.title = "'${binding.etKw.text}'의 검색 결과"
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
        homeAdapter.setItemLongClickListener(object : MainRecyclerAdapter.OnLongClickListener{
            override fun onLongClick(view: View, position: Int): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("${memos[position].content} 메모를 삭제합니다. 계속하시겠습니까?")
                    .setPositiveButton("네", object : DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            //메모 삭제 레트로핏
                            deleteMemo(memos[position])
                            dialog?.dismiss()
                        }
                    })
                    .setNegativeButton("아니오", object : DialogInterface.OnClickListener{
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
            Log.d("@@@@@날짜값 확인", selectedDay) //month+1 해야 해당 월의 데이터가 쿼리됨
        }

        var scroll = arrayListOf<Int>()

        vcb.setDateWatcher { y, m, d ->
            if (scroll.size == 9) {
                when {
                    scroll[8] < scroll[0] -> { //상승 스크롤
                        scroll.add(0, scroll[8])
                        scroll.removeAt(8)
                        scroll.removeAt(scroll.size - 1)
                    }
                    scroll[8] > scroll[7] -> { //하강 스크롤
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
                override fun onResponse(call: Call<MutableList<MemoItem>>, response: Response<MutableList<MemoItem>>) {
                    updateMemos(response)
                }
                override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                    failureMessage(t)
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "검색할 단어를 입력하세요", Toast.LENGTH_SHORT).show()
        }
    }
    fun deleteMemo(memoItem: MemoItem){
        val memoid = memoItem.memoId

        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.deleteMemo(id, memoid)
        call.enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    homeAdapter.notifyDataSetChanged()
                }
                Toast.makeText(this@MainActivity, "메모를 삭재하였습니다", Toast.LENGTH_SHORT).show()
                reloadData()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@MainActivity, "메모 삭제에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun queryDateNTag(id: Int, date: String, tag: String) {
        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.queryDateNTag(id, date, tag)

        call.enqueue(object : Callback<MutableList<MemoItem>> {
            override fun onResponse(call: Call<MutableList<MemoItem>>, response: Response<MutableList<MemoItem>>) {
                updateMemos(response)
            }

            override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                failureMessage(t)
            }
        })
    }

    var selectedTags = arrayListOf<String>()
    val selTagAdapter = SelectedTagAdapter(selectedTags)

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
                        val tags = arrayListOf<String>()
                        val result = gson.fromJson(intent, tags::class.java)
                        var tagList = ""
                        if (intent != "") {
                            for (i in result) { tagList += " #$i" }
                            this.tagList = tagList
                        }else{
                            this.tagList = tagList
                        }

                        if (binding.mainTb.title.contains(".")) queryDateNTag(id, binding.mainTb.title.toString(), this.tagList)
                        else {
                            binding.mainTb.title = "메모 전체보기"
                            queryDateNTag(id, "", this.tagList)
                        }

                        selectedTags.clear()

                        val list = mutableListOf<String>()
                        val tagResult = gson.fromJson(intent, list::class.java)
                        if (tagResult[0] != ""){
                            for (i in tagResult) {
                                selectedTags.add(i)
                                selTagAdapter.notifyDataSetChanged()
                            }
                        }else{
                            selectedTags.add("전체")
                            selTagAdapter.notifyDataSetChanged()
                        }

                        binding.rvTag.adapter = selTagAdapter
                        binding.rvTag.visibility = View.VISIBLE
                        selTagAdapter.setFilterTagClickListener(object :
                            SelectedTagAdapter.OnFilterTagClickListener {
                            override fun onClick(view: View, posision: Int) {
                                //여기서 레트로핏
                                selectedTags.remove(selectedTags[posision])
                                selTagAdapter.notifyDataSetChanged()
                                Log.d("@@@@필터 태그 삭제 확인", selectedTags.toString())
                            }
                        })
                    }
                }
            }
        }
    }
    fun toolbarListener(it: MenuItem): Boolean {
        return when (it.itemId) {
            R.id.tag -> {
                binding.etKw.visibility = View.GONE
                startActivityForResult(Intent(this@MainActivity, SetTagActivity::class.java), REQUEST_FILTERED_TAGS)
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
            R.id.add -> {
                val bundle = Bundle()
                if (binding.mainTb.title.contains("."))
                openAddBSDialog(binding.mainTb.title.toString(), bundle, 0)
                else openAddBSDialog(selectedDay, bundle, 0)
                true
            }
            R.id.all -> {
                binding.etKw.visibility = View.GONE
                binding.rvTag.visibility = View.INVISIBLE
                selectedTags.clear()
                selTagAdapter.notifyDataSetChanged()
                queryDateNTag(id, "", "")
                binding.mainTb.title = "메모 전체보기"
                true
            }
            else -> {
                Toast.makeText(this@MainActivity, "error", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun reloadData(){
        binding.swipe.setOnRefreshListener {
            if (binding.mainTb.title.toString() == "메모 전체보기") queryDateNTag(id, "", tagList)
            else queryDateNTag(id, binding.mainTb.title.toString(), tagList)
            binding.swipe.isRefreshing = false
        }
    }

    fun openAddBSDialog(date: String, bundle: Bundle, position: Int) {
        when (date) {
            "" -> { //홈 리사이클러뷰를 눌렀을 때
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
            "메모 전체보기" -> { //add 버튼을 눌렀을 때 11
                bundle.putString("date", selectedDay)
            }
            else -> { //add 버튼을 눌렀을 때 22
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
    fun updateMemos(response: Response<MutableList<MemoItem>>){

        Log.d("@@@@성공은 했는데 일단 확인해보셈","${response.body()}")
        memos.clear()
        homeAdapter.notifyDataSetChanged()

        val result = response.body()
        if (result != null && result.size > 0) {
            binding.resultAlert.visibility = View.GONE
            for (i in result) {
                memos.add(0, i)
                homeAdapter.notifyItemInserted(0)
            }
        }else{
            binding.resultAlert.visibility = View.VISIBLE
            binding.resultAlert.text = "검색 결과가 없습니다"
        }
    }
    fun failureMessage(t: Throwable){
        Log.d("@@일단 뭔가를 실패한 것 같음", "${t.message}")
    }

    //use-permission 으로도 해결이 안돼서 수동으로 퍼미션 받는 로직
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