package com.example.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.photogallery.api.FlickrApi
import com.example.photogallery.api.FlickrResponse
import com.example.photogallery.api.PhotoResponse
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {
    private val flickrApi: FlickrApi

    init {
//        val retrofit: Retrofit = Retrofit.Builder()
//            .baseUrl("https://api.flickr.com/")
////            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()

        val gsonPhotoDeserializer = GsonBuilder()
            .registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer())
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gsonPhotoDeserializer))
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

//    fun fetchPhotos(): LiveData<String> {
//        val responseLiveData: MutableLiveData<String> = MutableLiveData()
//        val flickrRequest: Call<String> = flickrApi.fetchPhotos()
//
//        flickrRequest.enqueue(object : Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                Log.d(TAG, "Response received")
//                responseLiveData.value = response.body()
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                Log.e(TAG,"Failed to fetch photos", t)
//            }
//        })
//
//        return responseLiveData
//    }

//    fun fetchPhotos(): LiveData<List<GalleryItem>> {
//        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
//        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos()
//
//        flickrRequest.enqueue(object : Callback<FlickrResponse> {
//            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
//                Log.d(TAG, "Response received")
//                val flickrResponse: FlickrResponse? = response.body()
//                val photoResponse: PhotoResponse? = flickrResponse?.photos
//                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
//                galleryItems = galleryItems.filterNot {
//                    it.url.isBlank()
//                }
//                responseLiveData.value = galleryItems
//            }
//
//            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
//                Log.e(TAG,"Failed to fetch photos", t)
//            }
//        })
//
//        return responseLiveData
//    }

    fun fetchPhotos(): LiveData<List<GalleryItem>>{
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<PhotoDeserializer> = flickrApi.fetchPhotos()


        flickrRequest.enqueue(object : Callback<PhotoDeserializer> {
            override fun onFailure(call: Call<PhotoDeserializer>, t: Throwable){
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(
                call: Call<PhotoDeserializer>,
                response: Response<PhotoDeserializer>
            ) {
                Log.d(TAG, "Response received")
                val flickrResponse: PhotoDeserializer? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems
            }
        })
        return  responseLiveData
    }
}