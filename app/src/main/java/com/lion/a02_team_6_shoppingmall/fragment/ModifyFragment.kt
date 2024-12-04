package com.lion.a02_team_6_shoppingmall.fragment

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
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
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lion.a02_team_6_shoppingmall.MainActivity
import com.lion.a02_team_6_shoppingmall.R
import com.lion.a02_team_6_shoppingmall.databinding.FragmentModifyBinding
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

class ModifyFragment : Fragment() {
    lateinit var fragmentModifyBinding: FragmentModifyBinding
    lateinit var mainActivity: MainActivity
    lateinit var albumLauncher: ActivityResultLauncher<Intent>
    lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    var selectedUri:Uri? = null
    var cameraImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentModifyBinding = FragmentModifyBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity
        createAlbumLauncher()
        createCameraLauncher()
        settingToolbar()
        settingBasicInfo()
        settingModifyImage()
        return fragmentModifyBinding.root
    }

    fun settingToolbar(){
        fragmentModifyBinding.materialToolbarModify.apply {
            title = "책 정보 수정"
            isTitleCentered = true

            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                mainActivity.removeFragment(FragmentName.MODIFY_FRAGMENT)
            }

            setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menuComplete -> {
                        modifyDone()
                    }
                }
                true
            }
        }
    }

    fun settingBasicInfo(){
        fragmentModifyBinding.apply {
            val bookIdx = arguments?.getInt("bookIdx")

            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO){
                    BookRepository.selectBookDataByIdx(mainActivity, bookIdx!!)
                }
                val bookViewModel = work1.await()

                when(bookViewModel.bookType){
                    BookType.BOOK_LITERATURE -> bookGroupModify.check(R.id.buttonLiteratureModify)
                    BookType.BOOK_HUMANITIES -> bookGroupModify.check(R.id.buttonHumanitiesModify)
                    BookType.BOOK_NATURE -> bookGroupModify.check(R.id.buttonNatureModify)
                    BookType.BOOK_ETC -> bookGroupModify.check(R.id.buttonEtcModify)
                    else -> {}
                }

                textFieldTitleModify.editText?.setText(bookViewModel.bookTitle)
                textFieldNameModify.editText?.setText(bookViewModel.bookName)
                textFieldCountModify.editText?.setText(bookViewModel.bookCount.toString())

                // 내부 저장소 URI 설정
                selectedUri = bookViewModel.bookImage.toUri()
                // 저장된 URI가 없으면
                if (selectedUri.toString() == "") {
                    // 기본 이미지 설정
                    imageViewBookModify.setImageResource(R.drawable.empty_file)
                } else {
                    imageViewBookModify.setImageURI(selectedUri)
                }
            }
        }
    }

    // 이미지 변경
    fun settingModifyImage(){
        fragmentModifyBinding.apply {
            buttonImageModifyCamera.setOnClickListener {
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
                cameraLauncher.launch(cameraIntent)
            }
            buttonImageModifyGallery.setOnClickListener {
                val albumIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
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

    fun createCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
                        fragmentModifyBinding.imageViewBookModify.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // 런처를 생성하는 메서드
    fun createAlbumLauncher() {
        albumLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK && it.data != null) {
                it.data?.data?.let { uri ->
                    selectedUri = uri

                    // Android 10 이상
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val source = ImageDecoder.createSource(mainActivity.contentResolver, uri)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        fragmentModifyBinding.imageViewBookModify.setImageBitmap(bitmap)
                    } else {
                        // Android 9 이하에서도 InputStream 방식으로 처리
                        try {
                            val inputStream = mainActivity.contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            fragmentModifyBinding.imageViewBookModify.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    // 외부 저장소의 이미지를 내부 저장소로 복사하는 메서드
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

    fun modifyDone(){
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(mainActivity)
        materialAlertDialogBuilder.setTitle("책 정보 수정")
        materialAlertDialogBuilder.setMessage("수정하면 이전 정보로 복원 할 수 없습니다.\n" +
                "수정하시겠습니까?")
        materialAlertDialogBuilder.setNeutralButton("취소", null)
        materialAlertDialogBuilder.setPositiveButton("수정"){ dialogInterface: DialogInterface, i: Int ->
            val bookIdx = arguments?.getInt("bookIdx")!!
            val bookType = when (fragmentModifyBinding.bookGroupModify.checkedButtonId) {
                R.id.buttonLiteratureModify -> BookType.BOOK_LITERATURE
                R.id.buttonHumanitiesModify -> BookType.BOOK_HUMANITIES
                R.id.buttonNatureModify -> BookType.BOOK_NATURE
                else -> BookType.BOOK_ETC
            }

            val bookTitle = fragmentModifyBinding.textFieldTitleModify.editText?.text.toString()
            val bookName = fragmentModifyBinding.textFieldNameModify.editText?.text.toString()
            val bookCount = fragmentModifyBinding.textFieldCountModify.editText?.text.toString().toInt()

            // 내부 저장소에 이미지 복사 및 URI 설정
            val bookImage = selectedUri?.let { uri ->
                copyImageToInternalStorage(uri, mainActivity)?.toString() ?: ""
                // 선택된 이미지가 없으면 빈 문자열 처리
            } ?: ""

            val bookViewModel = BookViewModel(bookIdx, bookType, bookTitle, bookName, bookCount, bookImage)

            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO){
                    BookRepository.modifyBookData(mainActivity, bookViewModel)
                }
                work1.join()
                mainActivity.removeFragment(FragmentName.MODIFY_FRAGMENT)
            }
        }
        materialAlertDialogBuilder.show()
    }
}