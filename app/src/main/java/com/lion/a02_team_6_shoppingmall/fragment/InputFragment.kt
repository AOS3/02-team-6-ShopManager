package com.lion.a02_team_6_shoppingmall.fragment

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.graphics.drawable.toBitmap
import com.lion.a02_team_6_shoppingmall.MainActivity
import com.lion.a02_team_6_shoppingmall.R
import com.lion.a02_team_6_shoppingmall.databinding.FragmentInputBinding
import com.lion.a02_team_6_shoppingmall.repository.BookRepository
import com.lion.a02_team_6_shoppingmall.util.BookType
import com.lion.a02_team_6_shoppingmall.util.FragmentName
import com.lion.a02_team_6_shoppingmall.viewmodel.BookViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class InputFragment : Fragment() {
    lateinit var fragmentInputBinding: FragmentInputBinding
    lateinit var mainActivity: MainActivity
    lateinit var albumLauncher: ActivityResultLauncher<Intent>
    lateinit var basicCameraLauncher: ActivityResultLauncher<Intent>
    var selectedUri: Uri? = null
    var cameraImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentInputBinding = FragmentInputBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity
        settingToolbar()
        createBasicCameraLauncher()
        createAlbumLauncher()
        settingImage()

        return fragmentInputBinding.root
    }

    fun settingToolbar(){
        fragmentInputBinding.materialToolbarInput.apply {
            title = "책 정보 등록"
            isTitleCentered = true

            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                mainActivity.removeFragment(FragmentName.INPUT_FRAGMENT)
            }

            setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menuComplete -> {
                        inputDone()
                    }
                }
                true
            }
        }
    }

    // 이미지 변경
    fun settingImage(){
        fragmentInputBinding.apply {
            buttonImageCamera.setOnClickListener {
                // 카메라 촬영을 위한 저장 URI 생성
                val filename = "IMG_${System.currentTimeMillis()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MyApp")
                    }
                }
                cameraImageUri = mainActivity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri) // URI에 저장하도록 설정
                }
                basicCameraLauncher.launch(cameraIntent)
            }

            buttonImageGallery.setOnClickListener {
                val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    // 이미지 타입을 설정한다.
                    type = "image/*"
                    // 선택할 파일의 타입을 지정(안드로이드 OS가 사전 작업을 할 수 있도록)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                }
                // 액티비티 실행
                albumLauncher.launch(albumIntent)
            }
        }
    }

    fun createBasicCameraLauncher() {
        basicCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                cameraImageUri?.let { uri ->
                    selectedUri = uri // URI를 선택된 이미지 URI로 설정
                    try {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val source = ImageDecoder.createSource(mainActivity.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(mainActivity.contentResolver, uri)
                        }

                        // 이미지뷰에 표시
                        fragmentInputBinding.imageViewBook.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun createAlbumLauncher() {
        albumLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK && it.data != null) {
                it.data?.data?.let { uri ->
                    selectedUri = uri

                    // Android 10 이상
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val source = ImageDecoder.createSource(mainActivity.contentResolver, uri)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        fragmentInputBinding.imageViewBook.setImageBitmap(bitmap)
                    } else {
                        // Android 9 이하에서도 InputStream 방식으로 처리
                        try {
                            val inputStream = mainActivity.contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            fragmentInputBinding.imageViewBook.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    fun copyImageToInternalStorage(uri: Uri, context: Context): Uri? {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream = when (uri.scheme) {
                "content" -> contentResolver.openInputStream(uri) // Content URI 처리
                "file" -> File(uri.path!!).inputStream() // File URI 처리
                else -> {
                    return null
                }
            }

            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            val file = File(context.filesDir, "book_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun inputDone() {
        fragmentInputBinding.apply {
            val bookType = when (bookGroup.checkedButtonId) {
                R.id.buttonLiterature -> BookType.BOOK_LITERATURE
                R.id.buttonHumanities -> BookType.BOOK_HUMANITIES
                R.id.buttonNature -> BookType.BOOK_NATURE
                else -> BookType.BOOK_ETC
            }
            // 데이터 저장 및 Fragment 전환
            val bookTitle = textFieldTitle.editText?.text.toString()
            val bookName = textFieldName.editText?.text.toString()
            val bookCount = textFieldCount.editText?.text.toString().toInt()

            val bookImageUri = selectedUri?.let { uri ->
                copyImageToInternalStorage(uri, mainActivity)?.toString() ?: ""
            } ?: ""

            val bookViewModel = BookViewModel(0, bookType, bookTitle, bookName, bookCount, bookImageUri)

            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO) {
                    BookRepository.insertBookData(mainActivity, bookViewModel)
                }
                work1.await()

                // MainFragment로 이동
                mainActivity.removeFragment(FragmentName.INPUT_FRAGMENT)
            }
        }
    }
}