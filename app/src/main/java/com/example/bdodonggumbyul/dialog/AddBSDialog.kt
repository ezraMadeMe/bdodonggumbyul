package com.example.bdodonggumbyul.dialog

import android.Manifest
import android.app.Activity
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.loader.content.CursorLoader
import com.bumptech.glide.Glide
import com.example.bdodonggumbyul.MemoItem
import com.example.bdodonggumbyul.R
import com.example.bdodonggumbyul.activity.MainActivity
import com.example.bdodonggumbyul.activity.SetTagActivity
import com.example.bdodonggumbyul.databinding.BsdAddBinding
import com.example.bdodonggumbyul.retrofit.RetrofitService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class AddBSDialog : BottomSheetDialogFragment() {

    interface CompleteClickListener {
        fun onClick(memo: MemoItem)
    }

    private lateinit var onClickListener: CompleteClickListener
    fun setOnClickListener(listener: CompleteClickListener) {
        this.onClickListener = listener
    }

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

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                if (it.resultCode != Activity.RESULT_CANCELED) {
                    val uri = it.data?.data
                    binding.addIv.visibility = View.VISIBLE
                    Glide.with(this@AddBSDialog).load(uri).into(binding.addIv)
                    imagePath = getRealPathFromUri(uri!!)
                }
            })

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

    fun selectTag() {
        val intent = Intent(requireContext(), SetTagActivity::class.java)
        startActivityForResult(intent, 1111)
    }

    fun setDateAndTime() {
        binding.addDate.text = arguments!!.getString("date", binding.addDate.text.toString())

        var sdf = SimpleDateFormat("HH:mm", Locale.KOREAN)
        var tz = TimeZone.getTimeZone("Asia/Seoul")
        sdf.timeZone = tz
        binding.addTimestamp.text = sdf.format(Date())//시간이 3시간 뒤로 찍힘 뭐임 //내가 해냄 고침
    }

    fun getRealPathFromUri(uri: Uri): String { //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
        var result = ""
        val cursor = context?.contentResolver?.query(uri, null, null, null, null)
        when (cursor) {
            null -> result = uri.path!!
            else -> {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                result = cursor.getString(index)
                cursor?.close()
            }
        }
        return result
    }

    fun saveMemo() {
        val id = "아이디는 어케 부여할까"
        val date = binding.addDate.text.toString()
        val timestamp = binding.addTimestamp.text.toString()
        val memo = binding.addEt.text.toString()

        val retrofitService = RetrofitService.newInstance()
        var filePart: MultipartBody.Part? = null

        //imagePath != null 아니고 ! .equals("") 해야 일로 안넘어감! 내가 해냄!
        if (!imagePath.equals("")) {
            val file = File(imagePath)
            val requestBody = RequestBody.create(MediaType.get("image/*"), file)
            filePart = MultipartBody.Part.createFormData("image",file.name,requestBody)
        }

        //dataPart의 값이 몽땅 빈값으로 넘어감 머선일이고
        var dataPart = HashMap<String, String>()
        dataPart.put("id", id)
        dataPart.put("date", date)
        dataPart.put("timestamp", timestamp)
        dataPart.put("content", memo)

        val call = retrofitService.insertMemo(dataPart, filePart)
        call.enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                //토스트도 안 뜨고 이미지url말고는 저장이 안됨...  //당연함 업로드 실패하니까...
                Log.d("레트로핏 성공@@@@@@@","${response.body()}")
                dismiss()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                //이미지 추가를 안하는데 imagePath 처리로직이 돌아서 그중에 오류나니 이리로 오는게 당연했음...
                //근데 이제 추가안한 이미지패스가 들어가기 시작함...
                //php문서 새로고침한번씩 할때마다 컬럼이 추가됨
                Log.d("레트로핏 실패@@@@@@@","$id _ $date _ $timestamp _ $memo")
                Log.d("레트로핏 실패 메시지@@@@@@@","${t.message}")
            }
        })
    }
    fun addImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }
}


































