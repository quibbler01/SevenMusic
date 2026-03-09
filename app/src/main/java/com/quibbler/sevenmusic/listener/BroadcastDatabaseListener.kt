package com.quibbler.sevenmusic.listener;

/**
 * Package:        com.quibbler.sevenmusic.listener
 * ClassName:      BroadcastDatabaseListener
 * Description:
 * Author:         11103905
 * CreateDate:     2019/9/27 18:54
 */
public interface BroadcastDatabaseListener extends BroadcastListener {
    public void onDatabaseChanged();
}
