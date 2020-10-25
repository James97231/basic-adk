package com.beautifulapp.basis_adk.helper.services

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

//"AIzaSyDkeAy9g6XAiteSU0yKRyA_OdX_iIO_l4Y"
//Place.Field.VIEWPORT
object LocationAutoComplete {
    val TAG = "LocationAutoComplete"
    lateinit var placesClient: PlacesClient
    lateinit var geocoder: Geocoder
    val token: AutocompleteSessionToken by lazy {
        AutocompleteSessionToken.newInstance()
    }
    val bounds = RectangularBounds.newInstance(LatLng(43.153691, -1.264649), LatLng(49.451767, 6.370557))
    val mapPlace = HashMap<String, Place>()

    fun initialize(context: Context) {
        Places.initialize(context, "AIzaSyDkeAy9g6XAiteSU0yKRyA_OdX_iIO_l4Y")
        placesClient = Places.createClient(context)
        geocoder = Geocoder(context)
    }

    private fun fetchPlace(placeId: String, placeFields: List<Place.Field> = listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS), callback: (Place?, Exception?) -> Unit) {
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            mapPlace[placeId] = response.place
            callback(response.place, null)
        }.addOnFailureListener { exception ->
            callback(null, exception)
        }
    }

    private fun fetchPlace(placeId: String, placeFields: List<Place.Field>?, callback: FetchCallback) {
        fetchPlace(
            placeId, placeFields
                ?: listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
        ) { place, exception ->
            callback.onCompleted(place, exception)
        }
    }


    fun fetchPlaceFromNameOrId(
        placeName: String,
        placeId: String,
        placeFields: List<Place.Field> = listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.VIEWPORT),
        callback: (Place?, Exception?) -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        thread(start = true) {
            try {
                /*val address = geocoder.getFromLocationName(placeName, 1)
                if (address.size > 0) {
                    handler.post { callback(addressToPlace(address[0]), null) }
                } else {
                    handler.post { fetchPlace(placeId, placeFields, callback) }
                }*/
                handler.post {
                    if (mapPlace.containsKey(placeId))
                        callback(mapPlace[placeId], null)
                    else
                        fetchPlace(
                            placeId,
                            placeFields,
                            callback
                        )

                    Log.e(TAG, " c'est ok:    placeId: ${placeId}")
                }
            } catch (e: Exception) {
                Log.e(TAG, " dans le catch: ${e.message}")
                e.printStackTrace()
                handler.post { callback(null, e) }
            }
        }
    }

    fun fetchPlaceFromNameOrId(placeName: String, placeId: String, placeFields: List<Place.Field>?, callback: FetchCallback) =
        fetchPlaceFromNameOrId(
            placeName, placeId, placeFields
                ?: listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
        ) { place, exception -> callback.onCompleted(place, exception) }


    interface FetchCallback {
        fun onCompleted(place: Place?, exception: Exception?)
    }

    fun predict(query: String, callback: (List<AutocompletePrediction>) -> Unit) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(bounds)
            //.setLocationRestriction(bounds)
            //.setCountry("au")
            .setTypeFilter(TypeFilter.GEOCODE)
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            callback(response.autocompletePredictions)
        }.addOnFailureListener { exception ->
            callback(ArrayList())
        }
    }

    fun predict(query: String, callback: AutocompleteCallback) {
        predict(
            query
        ) {
            callback.onCompleted(it)
        }
    }

    interface AutocompleteCallback {
        fun onCompleted(predictions: List<AutocompletePrediction>)
    }

    private fun addressToPlace(address: Address): Place {
        with(address) {
            /*Place.builder().apply {
                setAddress(getAddressLine(0))
                setLatLng(LatLng(latitude, longitude))
            }*/
            return object : Place() {
                override fun getUtcOffsetMinutes(): Int? {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun getAddressComponents(): AddressComponents? {
                    return AddressComponents.newInstance(arrayListOf())
                }

                override fun writeToParcel(dest: Parcel?, flags: Int) {
                    kotlin.TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun describeContents(): Int {
                    return 0
                }

                override fun getUserRatingsTotal(): Int? {
                    return null
                }

                override fun getBusinessStatus(): BusinessStatus? {
                    TODO("Not yet implemented")
                }

                override fun getName(): String? {
                    return getAddressLine(0)
                }

                override fun getOpeningHours(): OpeningHours? {
                    return null
                }

                override fun getId(): String? {
                    return null
                }

                override fun getPhotoMetadatas(): MutableList<PhotoMetadata> {
                    return ArrayList()
                }

                override fun getWebsiteUri(): Uri? {
                    return null
                }

                override fun getPhoneNumber(): String? {
                    return phone
                }

                override fun getRating(): Double? {
                    return null
                }

                override fun getPriceLevel(): Int? {
                    return null
                }

                override fun getAttributions(): MutableList<String> {
                    return ArrayList()
                }

                override fun getAddress(): String? {
                    return getAddressLine(0)
                }

                override fun getPlusCode(): PlusCode? {
                    return null
                }

                override fun getTypes(): MutableList<Type> {
                    return ArrayList()
                }

                override fun getViewport(): LatLngBounds? {
                    return null
                }

                override fun getLatLng(): LatLng? {
                    return LatLng(latitude, longitude)
                }
            }
        }
    }

}