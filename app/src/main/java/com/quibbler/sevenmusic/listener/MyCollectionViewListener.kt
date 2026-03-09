package com.quibbler.sevenmusic.listener

/**
 * Package:        com.quibbler.sevenmusic.listener
 * ClassName:      MyCollectionListener
 * Description:    观察数据变化，显示不同页面内容,Collection收藏用
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 10:54
 */
interface MyCollectionViewListener {
    fun changeView()

    fun removeData(id: Int)
}
