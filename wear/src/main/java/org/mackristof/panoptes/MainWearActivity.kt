package org.mackristof.panoptes

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.CardFragment
import android.support.wearable.view.DotsPageIndicator
import android.support.wearable.view.FragmentGridPagerAdapter
import android.support.wearable.view.GridViewPager
import android.util.Log
import org.mackristof.panoptes.fragment.DisplayFragment
import org.mackristof.panoptes.fragment.StartFragment
import org.mackristof.panoptes.service.WearMessageListener
import java.text.SimpleDateFormat
import java.util.*

class MainWearActivity : WearableActivity(){

    private inner class SampleGridPagerAdapter : FragmentGridPagerAdapter {
        val mRows: ArrayList<SampleGridPagerAdapter.Row> = ArrayList<SampleGridPagerAdapter.Row>()
        var mCtx: Context? = null
        constructor(ctx : Context, fm: FragmentManager) : super(fm) {
            mCtx = ctx
            val firstRow = Row(StartFragment(), DisplayFragment()) //, DisplayMapFragment())
            mRows.add(firstRow)
            mRows.add(Row(cardFragment(R.string.card_start_title, R.string.card_start_text)))
        }
        override fun getRowCount(): Int {
            return mRows.size
        }

        override fun getColumnCount(rowNum: Int): Int {
            return mRows[rowNum].getColumnCount();
        }

        override fun getFragment(row: Int, col: Int): Fragment? {
             return mRows[row].getColumn(col)
        }

        fun cardFragment(titleRes: Int, textRes: Int): Fragment{
            val res = mCtx?.resources
            val fragment = CardFragment.create(res?.getText(titleRes), res?.getText(textRes))
            return fragment
        }
        private inner class Row(vararg fragments: Fragment) {
            internal val columns: MutableList<Fragment> = ArrayList()

            init {
                for (f in fragments) {
                    add(f)
                }
            }

            fun add(f: Fragment) {
                columns.add(f)
            }

            internal fun getColumn(i: Int): Fragment {
                return columns[i]
            }

            fun getColumnCount(): Int {
                return columns.size
            }
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainWearActivityInstance = this
        setContentView(R.layout.activity_main)
        setAmbientEnabled()
        val pager =  findViewById(R.id.pager) as GridViewPager
        pager.setAdapter(SampleGridPagerAdapter(this, getFragmentManager()));
        var dotsPageIndicator = findViewById(R.id.page_indicator) as DotsPageIndicator
        dotsPageIndicator.setPager(pager)

        val intentMsg = Intent(this, WearMessageListener::class.java)
        if (!Utils.isServiceRunning(applicationContext, WearMessageListener::class.java.name)) {
            startService(intentMsg)
        }

    }

    override fun onPause() {

        super.onPause()
    }

    override fun onStop(){
        if (Utils.isServiceRunning(this.applicationContext ,GpsService::class.java.name)){
            Log.i(Constants.TAG,"app runnning so wont stop recording")
            Utils.generateNotification(this.applicationContext, R.drawable.ic_place_24dp, this::javaClass.get() as Class<Activity>, "clic to restore ${R.string.app_name}" )
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }




    companion object {
        private val AMBIENT_DATE_FORMAT = SimpleDateFormat("HH:mm", Locale.US)
        val TAG: String = MainWearActivity::class.java.simpleName
        private var MainWearActivityInstance: MainWearActivity? = null
        fun getInstance(): MainWearActivity {
            if (MainWearActivityInstance !=null) {
                return MainWearActivityInstance as MainWearActivity
            } else {
                throw IllegalStateException("MainWearActivity instance is null")
            }
        }

    }
}
