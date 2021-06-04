package com.eiualee.easyapi.iface;

/**
  *  liweihua
  *  2021/6/3 5:46 下午
  *  取得定义的API接口
  */
public  interface EasyApiServiceIface<T> {

    /**
     *  获取API接口
     * @return 返回泛型
     */
    T getEasyService();
}
