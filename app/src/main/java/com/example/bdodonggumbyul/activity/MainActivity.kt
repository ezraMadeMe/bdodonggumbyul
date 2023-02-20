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
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    val vcAdapter by lazy { VCalendarAdapter(initDaylist(setNow())) }
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
        binding.layoutMY.setOnClickListener { openMonthlyCalendar() }
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
                    Log.d("@@@@@@@@쿼리 문자열 넘어오는지 확인","${response.body()}")
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
        binding.vDateRecycler.adapter = vcAdapter
        binding.homeRv.adapter = homeAdapter
        binding.vDateRecycler.itemAnimator = null

        binding.vDateRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                //한번 스크롤해서 일자가 불러와진 후 다시 스크롤할 때 월/연변화를 인식할 수 없음

                val top = !binding.vDateRecycler.canScrollVertically(-1)
                val bottom = !binding.vDateRecycler.canScrollVertically(1)
                val start = binding.vMonth.text.equals("01")
                val end = binding.vMonth.text.equals("12")

                var y = (binding.vYear.text).toString().toInt()
                var m = (binding.vMonth.text).toString().toInt()

                val direction = binding.vDateRecycler.layoutManager as LinearLayoutManager
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                when { //애니메이션이나 연속성이 애매함
                    top -> { //스크롤이 최상단에 위치하면 월이 하나씩 감소
                        //상단스크롤하면 숫자가 역순으로 안찍힘
                        vibrator.vibrate(100)
                        if (start) {
                            binding.vMonth.text = "12"
                            y--
                            binding.vYear.text = "$y"
                        } else {
                            binding.vMonth.text = DecimalFormat("00").format(m - 1)
                        }
                        //아래로 스크롤은 무한스크롤이 되는데 위로 스크롤은 안됨 //내가해냄
                        initDaylist("$y.${m - 1}.01")
                        //무한정 데이터가 추가되는거 쫌 그럼 정리하고싶음
                        Toast.makeText(this@MainActivity, "상단: ${dates.size}", Toast.LENGTH_SHORT)
                            .show()
                    }
                    bottom -> { //스크롤이 최하단에 위치하면 월이 하나씩 증가
                        //하단스크롤하면 연/월이 안바뀜
                        vibrator.vibrate(100)
                        if (end) {
                            binding.vMonth.text = "01"
                            y++
                            binding.vYear.text = "$y"
                        } else {
                            binding.vMonth.text = DecimalFormat("00").format(m + 1)
                        }
                        initDaylist("$y.${m + 1}.${cal.getActualMaximum(m + 1)}")
                        Toast.makeText(this@MainActivity, "상단: ${dates.size}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })

//        binding.vDateRecycler.addItemDecoration() //헤더 추가

        vcAdapter.setItemClickListener(object : VCalendarAdapter.OnClickListener {
            override fun onClick(view: View, position: Int) {
                var y = binding.vYear.text
                var m = binding.vMonth.text
                var d = dates[position]

                val retrofitService = RetrofitService.newInstance()
                val call = retrofitService.queryDate("$y.$m.$d")
                call.enqueue(object : Callback<MutableList<MemoItem>>{
                    override fun onResponse(
                        call: Call<MutableList<MemoItem>>,
                        response: Response<MutableList<MemoItem>>
                    ) {
                        Log.d("@@@@@@@@날짜 쿼리 문자열 넘어오는지 확인","${response.body()}")

                        memos.clear()
                        homeAdapter.notifyDataSetChanged()

                        val result = response.body()
                        var index = 0
                        if (result != null){
                            for(i in result){
                                memos.add(index, i)
                                homeAdapter.notifyItemInserted(index)
                                index++
                            }
                        }
                    }

                    override fun onFailure(call: Call<MutableList<MemoItem>>, t: Throwable) {
                        Log.d("@@@@@@키워드 쿼리 실패 확인", "error : ${t.message}")
                    }
                })

                binding.mainTb.title = "$y.$m.$d"
                binding.drawerLayout.closeDrawer(Gravity.LEFT)
                vcAdapter.notifyDataSetChanged()
            }
        })
        homeAdapter.setItemClickListener(object : MainRecyclerAdapter.OnClickListener {
            override fun onClick(view: View, position: Int) {
                Toast.makeText(this@MainActivity, "" + memos[position].content, Toast.LENGTH_SHORT)
                    .show()
                //특정 메모에 대한 자세히 보기 다이얼로그 팝업
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

    fun initDaylist(string: String): MutableList<String> { //특정 날짜에 해당하는 월의 일자 리스트를 반환

        var split = string.split(".")
        val y = split[0].toInt()
        val m = split[1].toInt()
        val d = split[2].toInt()

        var newList = mutableListOf<String>()

        if (dates.size == 0) {
            cal.set(y, m - 1, d)
        } else {
            if (d == 1) cal.set(y, m - 2, d)
            else cal.set(y, m, d)
        }

        var last = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        if (d == 1) {
//            Toast.makeText(this@MainActivity, "상단 스크롤 $last", Toast.LENGTH_SHORT).show()
            for (i in 1..last) {
                newList.add(DecimalFormat("00").format(i))
            }
            vcAdapter.notifyItemMoved(0, dates.size)
        } else {
//            Toast.makeText(this@MainActivity, "하단 스크롤 $last", Toast.LENGTH_SHORT).show()
            for (i in 1..last) {
                newList.add(DecimalFormat("00").format(i))
            }
            binding.vDateRecycler.scrollToPosition(d - 1)
        }
        dates.addAll(newList)

        return dates
    }

    fun openMonthlyCalendar() { //먼슬리 캘린더 다이얼로그 열기
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        val pick = DatePickerDialog(
            this@MainActivity,
            R.style.Theme_NavigationDrawer,
            { _, y, m, d ->
                cal.set(y, m, d)
                binding.mainTb.title = "$y.${m + 1}.$d"
                binding.vYear.text = "$y"
                binding.vMonth.text = "${m + 1}"
                //선택한 일자로 스크롤 이동 - 간헐적으로 작동 안함
                binding.vDateRecycler.scrollToPosition(d - 1)

            }, y, m, d
        )
        pick.show()
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