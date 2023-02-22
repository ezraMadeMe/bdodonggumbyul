package com.example.bdodonggumbyul.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.emc.verticalweekcalendar.VerticalWeekCalendar
import com.emc.verticalweekcalendar.model.CalendarDay
import com.example.bdodonggumbyul.MemoItem
import com.example.bdodonggumbyul.R
import com.example.bdodonggumbyul.adapter.MainRecyclerAdapter
import com.example.bdodonggumbyul.adapter.SelectedTagAdapter
import com.example.bdodonggumbyul.databinding.ActivityMainBinding
import com.example.bdodonggumbyul.dialog.AddBSDialog
import com.example.bdodonggumbyul.retrofit.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val homeAdapter by lazy { MainRecyclerAdapter(this@MainActivity, memos) }
    lateinit var pref: SharedPreferences

    var memos = mutableListOf<MemoItem>()

    companion object {
        const val REQUEST_DATE_PICKER = 1024
        const val REQUEST_FILTERED_TAGS = 1994
        const val REQUEST_SELECTED_TAGS = 2023
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setRecycler()
        verifyStoragePermissions(this@MainActivity)

        pref = this@MainActivity.getSharedPreferences("userData", Context.MODE_PRIVATE)

        binding.mainTb.setOnMenuItemClickListener { toolbarListener(it) }
        binding.vYmLayout.setOnClickListener { popupDatePicker() }
        binding.etKw.setOnKeyListener { v, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    if (event.action == KeyEvent.ACTION_DOWN) { //분기처리해서 중복입력 방지 //내가 해냄
                        queryMemo()
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.swipe.setOnRefreshListener {
            if (binding.mainTb.title.equals("메모 전체보기")) loadAll()
            else loadMemo()
            binding.swipe.isRefreshing = false
        }
        binding.btnSetToday.setOnClickListener { }
    }//onCreate

    override fun onResume() {
        super.onResume()
        loadMemo()
    }

    fun popupDatePicker() {
//        binding.dp.visibility = View.VISIBLE
//        binding.dp.setOnDateChangedListener { view, y, m, dayOfMonth ->
//            Log.d("@@@데이트피커 확인", "$y.$m.$dayOfMonth")
//            val month = DecimalFormat("00").format(m + 1)
//            binding.vYear.text = y.toString()
//            binding.vMonth.text = month
//            binding.mainTb.title = "$y.$month.$dayOfMonth"
//            binding.dp.visibility = View.INVISIBLE
//        }
    }


    fun queryMemo() {
        if (binding.etKw.text != null) {
            val retrofitService = RetrofitService.newInstance()
            val call = retrofitService.queryMemo(binding.etKw.text.toString())
            call.enqueue(object : Callback<MutableList<MemoItem>> {
                override fun onResponse(
                    call: Call<MutableList<MemoItem>>,
                    response: Response<MutableList<MemoItem>>
                ) {
                    Log.d("@@@@@@@@쿼리 문자열 넘어오는지 확인", "${response.body()}")
                    memos.clear()
                    homeAdapter.notifyDataSetChanged()
                    val result = response.body()
                    var index = 0
                    if (result != null) {
                        for (i in result) {
                            memos.add(index, result[index])
                            homeAdapter.notifyItemInserted(index)
                            index++
                        }
                    }
                }

                override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                    Log.d("@@@@@@키워드 쿼리 실패 확인", "error : ${t.message}")
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "검색할 단어를 입력하세요", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadMemo() {
        val nickname = pref.getString("nickname", "").toString()
        val date = binding.mainTb.title.toString()
        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.loadMemo(nickname, date)
        call.enqueue(object : Callback<MutableList<MemoItem>> {
            override fun onResponse(
                call: Call<MutableList<MemoItem>>,
                response: Response<MutableList<MemoItem>>
            ) {
                memos.clear()
                homeAdapter.notifyDataSetChanged()

                val result = response.body()
                var index = 0
                if (result != null) {
                    for (i in result) {
                        memos.add(index, result[index])
                        homeAdapter.notifyItemInserted(index)
                        index++
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                Log.d("@@@@@@데이터 로딩 확인", "error : ${t.message}")
            }
        })
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

    fun setRecycler() {
        binding.mainTb.title = SimpleDateFormat("yyyy.MM.dd").format(Date())

        binding.homeRv.adapter = homeAdapter
        homeAdapter.setItemClickListener(object : MainRecyclerAdapter.OnClickListener {
            override fun onClick(view: View, position: Int) {
                val d = AddBSDialog.newInstance()
                val bundle = Bundle()

                val date = memos[position].date
                val timestamp = memos[position].timestamp
                val tag = memos[position].tag
                val content = memos[position].content
                val image = memos[position].image
                val imgUrl = "http://ezra2022.dothome.co.kr/memo/${image}"

                Log.d("@@@@번들 데이터 확인", "$date $timestamp $tag $content $image")

                bundle.putString("date", date)
                bundle.putString("timestamp", timestamp)
                bundle.putString("tag", tag)
                bundle.putString("content", content)
                if (image != "") bundle.putString("image", imgUrl)
                else bundle.putString("image", "")

                d.arguments = bundle
                d.show(supportFragmentManager, attributionTag)
                d.setOnClickListener(object : AddBSDialog.CompleteClickListener {
                    override fun onClick(memo: MemoItem) {
                        //특정 메모가 수정되었음을 notify
                        //UPDATE
                        homeAdapter.notifyItemChanged(position)
                    }
                })
                //특정 메모에 대한 자세히 보기 다이얼로그 팝업
            }
        })

        var selected = GregorianCalendar()

        val vcb = VerticalWeekCalendar.Builder()
            .setView(R.id.cv_vertical)
            .init(this)

        vcb.setOnDateClickListener { y, m, d ->

            val selDate = "$y.${DecimalFormat("00").format(m + 1)}.${DecimalFormat("00").format(d)}"
            binding.mainTb.title = selDate
            binding.drawerLayout.closeDrawer(Gravity.LEFT)

            Log.d("@@@@@날짜값 확인", selDate) //month+1 해야 해당 월의 데이터가 쿼리됨

            val retrofitService = RetrofitService.newInstance()
            val call = retrofitService.queryDate(selDate)
            call.enqueue(object : Callback<MutableList<MemoItem>> {
                override fun onResponse(
                    call: Call<MutableList<MemoItem>>,
                    response: Response<MutableList<MemoItem>>
                ) {
                    memos.clear()
                    homeAdapter.notifyDataSetChanged()

                    val result = response.body()
                    var index = 0

                    Log.d("@@@@@쿼리 결과 확인", result.toString())

                    if (result != null) {
                        for (i in result) {
                            memos.add(index, i)
                            homeAdapter.notifyItemInserted(index)
                            index++
                        }
                    }
                }

                override fun onFailure(
                    call: Call<MutableList<MemoItem>>,
                    t: Throwable
                ) {
                    Log.d("@@@@@@쿼리 실패 확인", "${t.message}")
                }
            })
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

    fun loadAll() {
        val nickname = pref.getString("nickname", "").toString()
        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.loadAll(nickname)
        call.enqueue(object : Callback<MutableList<MemoItem>> {
            override fun onResponse(
                call: Call<MutableList<MemoItem>>,
                response: Response<MutableList<MemoItem>>
            ) {
                memos.clear()
                homeAdapter.notifyDataSetChanged()

                val result = response.body()
                var index = 0

                Log.d("@@@@@쿼리 결과 확인", result.toString())

                if (result != null) {
                    for (i in result) {
                        memos.add(index, i)
                        homeAdapter.notifyItemInserted(index)
                        index++
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                Log.d("@@@@@@쿼리 실패 확인", "${t.message}")
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
                    val intent = data?.extras?.getStringArrayList("tags")
                    if (intent != null) {

                        var tags = ""
                        for (i in intent){ tags += " #$i" }
                        Log.d("@@@@받아온 태그 확인",tags)
                        val retrofitService = RetrofitService.newInstance()
                        val call = retrofitService.queryTag(tags)

                        call.enqueue(object : Callback<MutableList<MemoItem>>{
                            override fun onResponse(
                                call: Call<MutableList<MemoItem>>,
                                response: Response<MutableList<MemoItem>>
                            ) {
                                 val result = response.body()
                                if (result != null){
                                    var index = 0
                                    memos.clear()
                                    homeAdapter.notifyDataSetChanged()

                                    for (i in result){
                                        memos.add(index, i)
                                        index++
                                        homeAdapter.notifyDataSetChanged()
                                    }
                                }
                            }

                            override fun onFailure(
                                call: Call<MutableList<MemoItem>>,
                                t: Throwable
                            ) {
                                Log.d("@@@@태그 쿼리 실패 확인","${t.message}")
                            }
                        })
                        selectedTags.clear()
                        for (i in intent) {
                            selectedTags.add(i)
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
            R.id.tag -> { //태그명 검색창
                val intent = Intent(this@MainActivity, SetTagActivity::class.java)
                startActivityForResult(intent, REQUEST_FILTERED_TAGS)
                true
            }
            R.id.key -> { //키워드 검색창
                binding.etKw.visibility =
                    when (binding.etKw.visibility) {
                        View.VISIBLE -> View.GONE
                        else -> View.VISIBLE
                    }
                true
            }
            R.id.add -> { //메모 추가창

                val sdf = SimpleDateFormat("HH:mm", Locale.KOREAN)
                val tz = TimeZone.getTimeZone("Asia/Seoul")
                sdf.timeZone = tz
                val timestamp = sdf.format(Date())//시간이 3시간 뒤로 찍힘 뭐임 //내가 해냄 고침

                val d = AddBSDialog.newInstance()
                val bundle = Bundle()
                if (binding.mainTb.title.toString() == "메모 전체보기")
                    bundle.putString("date", SimpleDateFormat("yyyy.MM.dd").format(Date()))
                else bundle.putString("date", binding.mainTb.title.toString())
                bundle.putString("timestamp", timestamp)
                bundle.putString("image","")
                d.arguments = bundle
                d.show(supportFragmentManager, attributionTag)
                d.setOnClickListener(object : AddBSDialog.CompleteClickListener {
                    override fun onClick(memo: MemoItem) {
                        Toast.makeText(this@MainActivity, memo.content, Toast.LENGTH_SHORT).show()
                        memos.add(memo)
                        homeAdapter.notifyDataSetChanged()
                    }
                })
                true
            }
            R.id.all -> {
                loadAll()
                binding.mainTb.title = "메모 전체보기"
                true
            }
            else -> {
                Toast.makeText(this@MainActivity, "error", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}