package com.example.bdodonggumbyul.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.provider.Settings
import android.text.method.MovementMethod
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emc.verticalweekcalendar.VerticalWeekCalendar
import com.emc.verticalweekcalendar.interfaces.DateWatcher
import com.emc.verticalweekcalendar.model.CalendarDay
import com.example.bdodonggumbyul.MemoItem
import com.example.bdodonggumbyul.R
import com.example.bdodonggumbyul.adapter.MainRecyclerAdapter
import com.example.bdodonggumbyul.adapter.VCalendarAdapter
import com.example.bdodonggumbyul.databinding.ActivityMainBinding
import com.example.bdodonggumbyul.dialog.AddBSDialog
import com.example.bdodonggumbyul.dialog.SearchBSDialog
import com.example.bdodonggumbyul.retrofit.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val homeAdapter by lazy { MainRecyclerAdapter(this@MainActivity, memos) }
    lateinit var pref: SharedPreferences

    var memos = mutableListOf<MemoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setRecycler()
        verifyStoragePermissions(this@MainActivity)

        pref = this@MainActivity.getSharedPreferences("userData",Context.MODE_PRIVATE)

        binding.mainTb.setOnMenuItemClickListener { toolbarListener(it) }
        binding.etKw.setOnKeyListener { v, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    if (event.action == KeyEvent.ACTION_DOWN) { //분기처리해서 중복입력 방지 //내가 해냄
                        queryMemo()
                    }
                    true
                }
                else -> {
                    Log.d("@@@@@@키워드 ET 이벤트 실패", keyCode.toString())
                    false
                }
            }
        }
        binding.swipe.setOnRefreshListener {
            if (binding.mainTb.title.equals("메모 전체보기")) loadAll()
            else loadMemo()
            binding.swipe.isRefreshing = false
        }
        binding.btnSetToday.setOnClickListener {  }
    }//onCreate

    override fun onResume() {
        super.onResume()
        loadMemo()
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
        val nickname = pref.getString("nickname","").toString()
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
                Toast.makeText(this@MainActivity, "" + memos[position].content, Toast.LENGTH_SHORT)
                    .show()
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
            var recentM = binding.vMonth.text.toString().toInt()
            var recentY = binding.vYear.text.toString().toInt()

            if (scroll.size == 9) {
                when {
                    scroll[8] < scroll[0] -> { //상승 스크롤
                        scroll.add(0, scroll[8])
                        scroll.removeAt(8)
                        scroll.removeAt(scroll.size - 1)
                        Log.d("@@@상승", "$scroll")
                    }
                    scroll[8] > scroll[7] -> { //하강 스크롤
                        scroll.add(d)
                        scroll.removeAt(0)
                        Log.d("@@@하강", "$scroll")
                    }
                }
            } else if (scroll.size < 9) {
                scroll.add(d)
                Log.d("@@@초기데이터 확인", "$scroll")
            }

            Log.d("@@@예외 확인", "$scroll")

            when (selected.compareTo(GregorianCalendar(y, m, d))) {
                0 -> CalendarDay.SELECTED
                else -> CalendarDay.DEFAULT
            }
        }
    }

    fun loadAll(){
        val nickname = pref.getString("nickname","").toString()
        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.loadAll(nickname)
        call.enqueue(object : Callback<MutableList<MemoItem>>{
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

    fun toolbarListener(it: MenuItem): Boolean {
        return when (it.itemId) {
            R.id.tag -> { //태그명 검색창
                val intent = Intent(this@MainActivity, SetTagActivity::class.java)
                startActivityForResult(intent, 1994)
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
                var d = AddBSDialog.newInstance()
                var bundle = Bundle()
                bundle.putString("date", binding.mainTb.title.toString())
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