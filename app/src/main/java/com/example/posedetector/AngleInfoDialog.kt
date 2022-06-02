package com.example.posedetector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posedetector.adapter.AngleInfoAdapter
import com.example.posedetector.databinding.DialogAngleInfoBinding
import com.example.posedetector.model.AngleInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AngleInfoDialog(
    private val rightSidePoseAngleList: MutableList<AngleInfo>,
    private val leftSidePoseAngleList: MutableList<AngleInfo>
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogAngleInfoBinding
    private var rootView: View? = null
    private var rightSideAngleInfoAdapter: AngleInfoAdapter? = null
    private var leftSideAngleInfoAdapter: AngleInfoAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.theme_bottom_dialog)
//        setObserver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.peekHeight = bottomSheet.height
            dialog.dismissWithAnimation = true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                DialogAngleInfoBinding.inflate(inflater, container, false)
            rootView = binding.root
            initViews()
        }
        return rootView
    }

    private fun initViews() {
        binding.rrMain.setOnClickListener {
            dismissAllowingStateLoss()
        }

        leftSideAngleInfoAdapter = AngleInfoAdapter(requireContext(), leftSidePoseAngleList)
        rightSideAngleInfoAdapter = AngleInfoAdapter(requireContext(), rightSidePoseAngleList)

        binding.rcvRight.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rightSideAngleInfoAdapter
        }
        binding.rcvLeft.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leftSideAngleInfoAdapter
        }
    }
}