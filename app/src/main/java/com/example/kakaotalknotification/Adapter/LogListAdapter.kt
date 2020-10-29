package com.example.kakaotalknotification.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.kakaotalknotification.R

class LogListAdapter(val context: Context, val logList: List<LogList>): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.log_item_list, null)

        val title = view.findViewById<TextView>(R.id.LogTitleTextView)
        val content = view.findViewById<TextView>(R.id.LogContentTextView)
        val date = view.findViewById<TextView>(R.id.LogDateTextView)

        val log = logList[position]

        title.text = log.title
        content.text = log.content
        date.text = log.date

        return view
    }

    override fun getItem(position: Int): Any {
        return logList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return logList.size
    }

}