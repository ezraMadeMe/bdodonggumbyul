package com.example.bdodonggumbyul.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
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

    //    val vcAdapter by lazy { VCalendarAdapter(initDaylist(setNow())) }
    val homeAdapter by lazy { MainRecyclerAdapter(this@MainActivity, memos) }

    var memos = mutableListOf<MemoItem>()

    var dates = mutableListOf<String>()
    var cal = Calendar.getInstance()
    var n = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setRecycler()

        verifyStoragePermissions(this@MainActivity)
        binding.mainTb.setOnMenuItemClickListener { toolbarListener(it) }
//        binding.layoutMY.setOnClickListener { openMonthlyCalendar() }
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
            loadMemo()
            binding.swipe.isRefreshing = false
        }
    }

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
        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.loadMemo()
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
            val selectedDay = GregorianCalendar(y, m, d)
            if (selected.compareTo(selectedDay) != 0) selected = selectedDay //무슨 말인지 몰으갯읍니다

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

                    Log.d("@@@@@쿼리 결과 확인", result?.size.toString())

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



        vcb.setDateWatcher(object : DateWatcher {
            override fun getStateForDate(y: Int, m: Int, d: Int): Int {
                Log.d("@@@@@확인", "$y $m $d")

                var year = y
                var month = m + 1
                var day = d


                when(day){
//                when (d) {
//                    1 -> {
//                        binding.vMonth.text = DecimalFormat("00").format(month-1)
//                        if (binding.vMonth.text.equals("00")) {
//                            binding.vYear.text = "${year-1}"
//                            binding.vMonth.text = "12"
//                        }
//                    }
//                    cal.getActualMaximum(month) -> {
//                        binding.vMonth.text = DecimalFormat("00").format(month+1)
//                        if (binding.vMonth.text.equals("00")) {
//                            binding.vYear.text = "${year+1}"
//                            binding.vMonth.text = "01"
//                        }
//                    }
//                    else -> {
//                        binding.vMonth.text = DecimalFormat("00").format(month)
//                        if (binding.vMonth.text.equals())
//                    }
                }
                return when (selected.compareTo(GregorianCalendar(y, m, d))) {
                    0 -> CalendarDay.SELECTED
                    else -> CalendarDay.DEFAULT
                }
            }
        })
    }

    fun setNow(): String { //입력된 날짜를 한국시 date로 변환
        val now = Date(System.currentTimeMillis())
        var sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
        var tz = TimeZone.getTimeZone("Asia/Seoul")
        sdf.timeZone = tz

        return sdf.format(now)
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
            else -> {
                Toast.makeText(this@MainActivity, "error", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}