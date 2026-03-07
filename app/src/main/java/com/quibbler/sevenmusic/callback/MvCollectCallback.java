package com.quibbler.sevenmusic.callback;

/**
  *
  * Package:        com.quibbler.sevenmusic.callback
  * ClassName:      MvCollectCallback
  * Description:    查询、收藏mv的callback
  * Author:         lishijun
  * CreateDate:     2019/10/16 16:06
 */
public interface MvCollectCallback {
    void isCollected();
    void notCollected();
}
