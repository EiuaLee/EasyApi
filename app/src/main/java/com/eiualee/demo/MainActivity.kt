package com.eiualee.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eiualee.demo.api.ApiRepoertory
import com.eiualee.demo.bean.ExtendsBaseListRespBean
import com.eiualee.easyapi.R
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        compositeDisposable.add(ApiRepoertory.fun1().subscribeWith(object :EasySubscriver<List<ExtendsBaseListRespBean>>(){
            override fun onNext(t: List<ExtendsBaseListRespBean>?) {
                // 调用接口返回的结果
            }
        }))


    }


}