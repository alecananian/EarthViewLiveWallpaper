package com.alecananian.earthviewlivewallpaper.interfaces;

import com.alecananian.earthviewlivewallpaper.models.Wallpaper;

public interface SetWallpaperTaskInterface {
    void onSetWallpaperTaskComplete(Wallpaper wallpaper);
    void onSetWallpaperTaskError();
}
