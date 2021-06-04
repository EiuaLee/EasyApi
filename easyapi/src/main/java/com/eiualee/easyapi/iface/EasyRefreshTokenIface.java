package com.eiualee.easyapi.iface;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
  *  eiualee
  *  2021/6/1 2:08 下午
  *  刷新Token接口
  */
public interface EasyRefreshTokenIface<T extends EasyRespIface> {
    /**
     * 刷新token具体实现
     * @return ~
     */
   Function<Flowable<Throwable>, Flowable<T>> doRefresh();
}
