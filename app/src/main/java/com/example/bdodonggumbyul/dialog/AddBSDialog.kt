package com.example.bdodonggumbyul.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.bdodonggumbyul.Memo
import com.example.bdodonggumbyul.R
import com.example.bdodonggumbyul.activity.SetTagActivity
import com.example.bdodonggumbyul.databinding.BsdAddBinding
import com.example.bdodonggumbyul.retrofit.RetrofitService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddBSDialog : BottomSheetDialogFragment() {

    interface CompleteClickListener { fun onClick(memo: Memo) }
    private lateinit var onClickListener: CompleteClickListener
    fun setOnClickListener(listener: CompleteClickListener) { this.onClickListener = listener }
    var imagePath = ""

    lateinit var binding: BsdAddBinding

    companion object {
        fun newInstance(): AddBSDialog {
            val fragment = AddBSDialog()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BsdAddBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDateAndTime()
        binding.addImage.setOnClickListener { addImage() }
        binding.addComplete.setOnClickListener { saveMemo() }
        binding.addTag.setOnClickListener { selectTag() }
    }

    fun selectTag(){
        val intent = Intent(requireContext(), SetTagActivity::class.java)
        startActivityForResult(intent,1111)
    }

    fun setDateAndTime(){
        binding.addDate.text = arguments!!.getString("date",binding.addDate.text.toString())

        var sdf = SimpleDateFormat("HH:mm", Locale.KOREAN)
        var tz = TimeZone.getTimeZone("Asia/Seoul")
        sdf.timeZone = tz
        binding.addTimestamp.text =  sdf.format(Date())//시간이 3시간 뒤로 찍힘 뭐임 //내가 해냄 고침
    }

    fun saveMemo(){
        var d = binding.addDate.text.toString()
        var t = binding.addTimestamp.text.toString()
        var i = binding.addIv.resources
        var m = binding.addEt.text.toString()
        var newMemo = Memo(d,t,m)

        onClickListener.onClick(newMemo)
        dismiss()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.addComplete.setOnClickListener {
            if (binding.addEt.equals("")) Toast.makeText(requireContext(), "메모를 입력하세요", Toast.LENGTH_SHORT).show()
            else addMemo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1024 -> {
//                    imagePath = data?.dataString!!
                    val uri = data?.data
                    binding.addIv.visibility = View.VISIBLE
                    binding.addIv.setImageURI(uri)
//                    Glide.with(this).load(uri).into(binding.addIv)
                }
                else -> Log.d("사진선택오류", "오류났다@@@@@")
            }
        }
    }

    fun addImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, 1024)
    }

    fun addMemo() {
        val date = binding.addDate.text.toString()
        val timestamp = binding.addTimestamp.text.toString()
        val memo = binding.addEt.text.toString()
        val image = binding.addIv.resources
        val retrofit = RetrofitService.newInstance()

        val intent = Intent()
        intent.putExtra("date",date)
        intent.putExtra("timestamp", timestamp)
        intent.putExtra("memo", memo)
        intent.putExtra("image", imagePath)

        //레트로핏작업 이따가.. 리사이클러뷰에 추가되는지 먼저
//        retrofit.postNewMemo(date, timestamp, memo).enqueue(object : Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                Log.d("업로드 성공", call.toString())
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                Log.d("업로드 실패", t.message.toString())
//            }
//        })
        dismiss()
    }

    fun changeImgPath(): File{
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val fileName = "IMG_${timestamp}"
        val directory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(fileName,".jpg",directory)
        imagePath = file.absolutePath
        return file
    }
}