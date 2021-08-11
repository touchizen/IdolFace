package com.touchizen.idolface.ui.idolgallery

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.touchizen.idolface.model.IdolImage
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.utils.IdolUtils
import kotlinx.coroutines.tasks.await

class IdolGalleryPagingSource(
    val db: FirebaseFirestore,
    val idolProfile:MutableLiveData<IdolProfile>
) : PagingSource<QuerySnapshot, IdolImage>() {

    val LIMIT_COUNT:Long = 5

    fun getQuery(
        galleryCollection: CollectionReference,
        idolProfile:MutableLiveData<IdolProfile>
    ): Query {
        return galleryCollection
            .whereEqualTo("idolId", idolProfile.value!!.id)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(LIMIT_COUNT)
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, IdolImage> {
        return try {
            val galleryCollection = db.collection(IdolUtils.IDOLGALLERY_COLLECTION)
            val currentPage = params.key ?: getQuery(galleryCollection, idolProfile)
                .get()
                .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]
            val nextPage = getQuery(galleryCollection, idolProfile)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()

            LoadResult.Page(
                data = currentPage.toObjects(IdolImage::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<QuerySnapshot, IdolImage>): QuerySnapshot? {
        return null
    }
}