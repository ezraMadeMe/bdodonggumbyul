package com.ezralee.bdodonggumbyul.dialog

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.ezralee.bdodonggumbyul.model.MemoItem
import com.ezralee.bdodonggumbyul.R
import com.ezralee.bdodonggumbyul.activity.MainActivity.Companion.REQUEST_FILTERED_TAGS
import com.ezralee.bdodonggumbyul.activity.SetTagActivity
import com.ezralee.bdodonggumbyul.databinding.BsdAddBinding
import com.ezralee.bdodonggumbyul.model.UserData
import com.ezralee.bdodonggumbyul.retrofit.RetrofitService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.GsonBuilder
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

    val gson = GsonBuilder().create()
    val pref by lazy { activity!!.getSharedPreferences("user_data", Context.MODE_PRIVATE) }
    val editor by lazy { pref.edit() }
    lateinit var memonum: String

    lateinit var prevTxt: String

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
        prevTxt = ""
        memonum = pref.getString("memo_num", "")!!
        setArguments()
        setDateAndTime()
        binding.addImage.setOnClickListener { addImage() }
        binding.addTag.setOnClickListener { selectTag() }
        binding.addComplete.setOnClickListener { insertMemo() }
    }

    fun setArguments(){
        if (arguments!!.getString("memoId") != null) {
            val id =
                gson.fromJson(pref.getString("user_data", ""), UserData::class.java).seq.toInt()
            memonum = arguments!!.getString("memoId")!!
            val prevD = arguments!!.getString("date")
            val prevTS = arguments!!.getString("timestamp")
            val prevT = arguments!!.getString("tag")
            prevTxt = arguments!!.getString("content")!!
            val prevI = arguments!!.getString("image")

            binding.apply {
                this.addDate.text = prevD
                this.addTimestamp.text = prevTS
                this.selTag.text = prevT
                this.addEt.setText(prevTxt)

                if (prevI != "") {
                    this.addIv.visibility = View.VISIBLE
                    Glide.with(requireContext()).load(prevI).into(this.addIv)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_FILTERED_TAGS -> {
                if (resultCode == Activity.RESULT_OK) {

                    val result = data?.extras?.getString("tags")
                    val arr = arrayListOf<String>()
                    var string = ""
                    Log.d("@@@@BSD ?????? ??????", "$result")
                    if (result != null) {
                        var addedTag = gson.fromJson(result, arr::class.java)
                        for (i in addedTag) {
                            string += " #${i}"
                        }
                        binding.selTag.text = string
                    }
                }
            }
            else -> {
                Log.d("@@@????????? ?????? ?????????", "????????? ???????????????")
            }
        }
    }

    fun setDateAndTime() {
        binding.addDate.text = arguments!!.getString("date", binding.addDate.text.toString())

        val sdf = SimpleDateFormat("HH:mm", Locale.KOREAN)
        val tz = TimeZone.getTimeZone("Asia/Seoul")
        sdf.timeZone = tz
        binding.addTimestamp.text = sdf.format(Date())//????????? 3?????? ?????? ?????? ?????? //?????? ?????? ??????
    }

    fun getRealPathFromUri(uri: Uri): String { //Uri -- > ??????????????? ????????? ?????????????????? ?????????
        var result = ""
        val cursor = context?.contentResolver?.query(uri, null, null, null, null)
        when (cursor) {
            null -> result = uri.path!!
            else -> {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                result = cursor.getString(index)
                cursor.close()
            }
        }
        return result
    }

    fun insertMemo() {
        val id = gson.fromJson(pref.getString("user_data", ""), UserData::class.java).seq.toInt()
        val date = binding.addDate.text.toString()
        val timestamp = binding.addTimestamp.text.toString()
        val memo = binding.addEt.text.toString()
        val tag = binding.selTag.text.toString()

        if (memo == "")
            Toast.makeText(requireContext(), "????????? ???????????????", Toast.LENGTH_SHORT)
                .show()
        else {
            val retrofitService = RetrofitService.newInstance()
            var filePart: MultipartBody.Part? = null

            //imagePath != null ????????? ! .equals("") ?????? ?????? ????????????! ?????? ??????!
            if (!imagePath.equals("")) {
                val file = File(imagePath)
                val requestBody = RequestBody.create(MediaType.get("image/*"), file)
                filePart = MultipartBody.Part.createFormData("image", file.name, requestBody)
            }

            //dataPart??? ?????? ?????? ???????????? ????????? ???????????????
            val dataPart = HashMap<String, String>()
            dataPart["seq"] = id.toString()
            dataPart["memo_id"] = memonum
            dataPart["date"] = date
            dataPart["timestamp"] = timestamp
            dataPart["content"] = memo
            dataPart["tag"] = tag

            Log.d("@@dataPart ??????","$dataPart")

            val call = retrofitService.insertMemo(dataPart, filePart)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.body()!!.contains("?????? ????????? ??????")){
                        val add = memonum.toInt() + 1
                        editor.remove("memo_num")
                            .putString("memo_num", add.toString()).commit()
                    }
                    dismiss()
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    //????????? ????????? ???????????? imagePath ??????????????? ????????? ????????? ???????????? ????????? ????????? ????????????...
                    //?????? ?????? ???????????? ?????????????????? ???????????? ?????????...
                    //php?????? ????????????????????? ???????????? ????????? ?????????
                    Log.d("???????????? ?????? ?????????@@@@@@@", "${t.message}")
                }
            })
        }
    }
    fun selectTag() {
        val intent = Intent(this.requireContext(), SetTagActivity::class.java)
        startActivityForResult(intent, REQUEST_FILTERED_TAGS)
    }
    fun addImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }
}