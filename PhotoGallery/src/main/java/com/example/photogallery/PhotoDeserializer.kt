package com.example.photogallery

import com.example.photogallery.api.PhotoResponse
import com.google.gson.*
import java.lang.reflect.Type

class PhotoDeserializer : JsonDeserializer<PhotoResponse> {
    lateinit var photos: PhotoResponse

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): PhotoResponse {
//        val jsonObject: JsonObject = json?.asJsonObject!!
//        val jsonArray = jsonObject.getAsJsonArray("photo")
//        val photoResponse:PhotoResponse = PhotoResponse()
//        val photos:MutableList<GalleryItem> = mutableListOf()
//        jsonArray.forEach {photo ->
//            val photoElement = photo.asJsonObject
//            val galleryItem:GalleryItem = GalleryItem(
//                photoElement.get("title").asString,
//                photoElement.get("id").asString,
//                photoElement.get("url_s").asString
//            )
//            photos.add(galleryItem)
//        }
//        photoResponse.galleryItems = photos
//        return photoResponse

        val jsonObject: JsonObject = json?.asJsonObject!!

        photos = Gson().fromJson(jsonObject, PhotoResponse::class.java)

        return photos
    }

}