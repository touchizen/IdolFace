package com.touchizen.idolface.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.touchizen.idolface.databinding.FragmentImageSrcSheetBinding

interface SheetListener{
    fun selectedItem(index: Int)
}
class FImageSrcSheet constructor() : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentImageSrcSheetBinding

    private lateinit var listener: SheetListener

    companion object{
        fun newInstance(bundle : Bundle) : FImageSrcSheet{
            val fragment = FImageSrcSheet()
            fragment.arguments=bundle
            return fragment
        }
    }

    fun addListener(listener: SheetListener){
        this.listener=listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FragmentImageSrcSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtCamera.setOnClickListener {
            listener.selectedItem(0)
            dismiss()
        }

        binding.txtGallery.setOnClickListener {
            listener.selectedItem(1)
            dismiss()
        }
    }
}