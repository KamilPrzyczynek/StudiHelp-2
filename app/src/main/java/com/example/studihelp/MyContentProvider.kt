package com.example.studihelp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class MyContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.studihelp.mycontentprovider"
        const val ITEMS = 1
        const val ITEM_ID = 2
        val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            sUriMatcher.addURI(AUTHORITY, "items", ITEMS)
            sUriMatcher.addURI(AUTHORITY, "items/#", ITEM_ID)
        }
    }

    override fun onCreate(): Boolean {
        // Inicjalizacja dostawcy treści
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        // Implementacja zapytań do bazy danych
        return null
    }

    override fun getType(uri: Uri): String? {
        // Implementacja określenia MIME type
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Implementacja wstawiania nowych danych
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // Implementacja usuwania danych
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        // Implementacja aktualizacji danych
        return 0
    }
}
