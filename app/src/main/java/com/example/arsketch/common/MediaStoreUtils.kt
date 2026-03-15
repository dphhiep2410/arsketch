package com.example.arsketch.common

import android.content.Context
import android.provider.MediaStore
import com.example.arsketch.data.model.GroupImageModel
import com.example.arsketch.data.model.ImageModel
import java.io.File
import java.util.Locale

object MediaStoreUtils {
    fun getListGroupItem(context: Context): List<GroupImageModel> {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
        )
        val folders = mutableListOf<GroupImageModel>()
        val selection = MediaStore.Files.FileColumns.SIZE + " > 0"

        try {
            context.queryCursor(uri, projection, selection) { cursor ->
                try {
                    val name = cursor.getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    if (name.startsWith(".")) {
                        return@queryCursor
                    }
                    val fullMimetype = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)
                        ?.lowercase(Locale.getDefault()) ?: return@queryCursor
                    val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                    if (size == 0L) {
                        return@queryCursor
                    }

                    val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)

                    val mimetype = fullMimetype.substringBefore("/")
                    if (mimetype == "image") {
                        val parentFolderPath = File(path).parentFile?.path
                        val parentFolder = File(path).parentFile?.name ?: "No_name"
                        val parentPath = File(path).parentFile?.path.toString()
                        val filterList = parentPath.split("/")
                        for (element in filterList) {
                            if (element.startsWith(".")) {
                                return@queryCursor
                            }
                        }
                        if (!folders.any { it.folderUri == parentFolderPath }) {
                            folders.add(
                                GroupImageModel(
                                    parentFolder,
                                    parentFolderPath.toString(),
                                    mutableListOf(path)
                                )
                            )
                        }else{
                            folders.find { it.folderUri == parentFolderPath }?.thumbUrl?.add(path)
                        }


                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return folders
    }

    fun getAllImage(context: Context): List<ImageModel> {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
        )
        val folders = mutableListOf<ImageModel>()
        val selection = MediaStore.Files.FileColumns.SIZE + " > 0"

        try {
            context.queryCursor(uri, projection, selection) { cursor ->
                try {
                    val name = cursor.getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    if (name.startsWith(".")) {
                        return@queryCursor
                    }
                    val fullMimetype = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)
                        ?.lowercase(Locale.getDefault()) ?: return@queryCursor
                    val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                    if (size == 0L) {
                        return@queryCursor
                    }

                    val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)

                    val mimetype = fullMimetype.substringBefore("/")
                    if (mimetype == "image") {
                        val filterList = path.split("/")
                        for (element in filterList) {
                            if (element.startsWith(".")) {
                                return@queryCursor
                            }
                        }
                        folders.add(
                            ImageModel(
                                path,
                            )
                        )
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return folders
    }


    fun getChildImageFromPath(context: Context, path: String): MutableList<ImageModel> {
        try {
            val listImageChild = mutableListOf<ImageModel>()
            val uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,

                )
            val selection =
                MediaStore.Files.FileColumns.DATA + " LIKE ? AND " + MediaStore.Files.FileColumns.DATA + " NOT LIKE ? AND " + MediaStore.Files.FileColumns.SIZE + " > 0"
            val selectionArgs = arrayOf("$path/%", "$path/%/%")

            context.queryCursor(uri, projection, selection, selectionArgs) { cursor ->
                val childPath = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)

                val fullMimetype = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)
                    ?.lowercase(Locale.getDefault()) ?: return@queryCursor
                val mimetype = fullMimetype.substringBefore("/")

                if (mimetype == "image" && childPath.isNotBlank() && childPath != path) {
                    listImageChild.add(
                        ImageModel(childPath)
                    )
                }
            }
            return listImageChild
        } catch (e: Exception) {
            e.printStackTrace()
            return mutableListOf()
        }

    }


}
