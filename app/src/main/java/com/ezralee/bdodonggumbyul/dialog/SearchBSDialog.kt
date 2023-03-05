package com.ezralee.bdodonggumbyul.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ezralee.bdodonggumbyul.R
import com.ezralee.bdodonggumbyul.databinding.BsdSearchBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchBSDialog: BottomSheetDialogFragment() {

    lateinit var binding: BsdSearchBinding

    companion object{
        fun newInstance(): SearchBSDialog {
            val fragment = SearchBSDialog()
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
        binding = BsdSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //서치 아이콘/완료버튼 클릭시 쿼리
    }
}