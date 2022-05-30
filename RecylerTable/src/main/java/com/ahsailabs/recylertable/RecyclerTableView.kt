package com.ahsailabs.recylertable

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.AdapterListUpdateCallback
import kotlin.collections.ArrayList

/**
 * Created by ahmad s on 28/04/22.
 */
class RecyclerTableView : FrameLayout{
    private val dataList: ArrayList<Any> = ArrayList()
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    init {
        val view = LayoutInflater.from(context).inflate(R.layout.main_layout, this, false)
        addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun addData(data: Any){
        dataList.add(data)
    }

    fun addCollectionData(collectionData: List<Any>){
        dataList.addAll(collectionData)
    }




}