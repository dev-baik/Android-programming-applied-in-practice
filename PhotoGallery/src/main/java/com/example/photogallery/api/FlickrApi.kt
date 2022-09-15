package com.example.photogallery.api

import com.example.photogallery.FlickrFetchr
import retrofit2.Call
import retrofit2.http.GET

interface FlickrApi {
//    @GET("/")
//    fun fetchContents(): Call<String>
//    fun fetchPhotos(): Call<String>

    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=f38224baa1e1e8b19d729c9078a1352f" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    fun fetchPhotos(): Call<FlickrResponse>
}