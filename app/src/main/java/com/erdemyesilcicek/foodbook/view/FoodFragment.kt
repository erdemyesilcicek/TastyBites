package com.erdemyesilcicek.foodbook.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.erdemyesilcicek.foodbook.databinding.FragmentFoodBinding
import com.erdemyesilcicek.foodbook.model.Food
import com.erdemyesilcicek.foodbook.roomdb.FoodDAO
import com.erdemyesilcicek.foodbook.roomdb.FoodDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var selectedPhoto : Uri? = null
    private var selectedBitmap : Bitmap? = null
    private val mDisposable = CompositeDisposable()
    private var selectedFood : Food? = null

    private lateinit var db : FoodDatabase
    private lateinit var foodDao: FoodDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(), FoodDatabase::class.java, "Foods").build()
        foodDao = db.foodDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener { photoClicked(it) }
        binding.saveButton.setOnClickListener { saveButtonClicked(it) }
        binding.deleteButton.setOnClickListener { deleteButtonClicked(it) }

        arguments?.let {
            val info = FoodFragmentArgs.fromBundle(it).info

            if(info == "new"){
                selectedFood = null
                binding.deleteButton.isEnabled = false
                binding.saveButton.isEnabled = true
                binding.nameText.setText("")
                binding.contentsText.setText("")
            }else{
                binding.deleteButton.isEnabled = true
                binding.saveButton.isEnabled = false
                val id = FoodFragmentArgs.fromBundle(it).id
                mDisposable.add(
                    foodDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )
            }
        }
    }
    private fun handleResponse(food : Food){
        val bitmap = BitmapFactory.decodeByteArray(food.photo,0,food.photo.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.nameText.setText(food.name)
        binding.contentsText.setText(food.content)
        selectedFood = food
    }

    fun photoClicked(view: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin yok, almamız lazım
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    //snackbar kullan
                    Snackbar.make(view, "Galeriye erişebilmek için izin vermeniz lazım!",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver",
                        View.OnClickListener {
                            //izin iste
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }else{
                    //izin iste direkt
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                //galeriye gidiş serbest
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }else{
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin yok, almamız lazım
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //snackbar kullan
                    Snackbar.make(view, "Galeriye erişebilmek için izin vermeniz lazım!",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver",
                        View.OnClickListener {
                            //izin iste
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                }else{
                    //izin iste direkt
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                //galeriye gidiş serbest
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    fun saveButtonClicked(view: View){
        val name = binding.nameText.text.toString()
        val content = binding.contentsText.text.toString()

        if(selectedBitmap != null){
            val smallBitmap = smallBitmapCreate(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            val food = Food(name, content, byteArray)

            //RxJava
            mDisposable.add(
                foodDao.insert(food)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert))
        }

    }

    fun deleteButtonClicked(view: View){
        if(selectedFood != null){
            mDisposable.add(
                foodDao.delete(food = selectedFood!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert))
        }
    }
    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    selectedPhoto = intentFromResult.data
                    try {
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedPhoto!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPhoto)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    }catch(e: Exception){
                        println(e.localizedMessage)
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(requireContext(),"izin verilmedi!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun smallBitmapCreate(userSelectedBitmap : Bitmap, maxSize : Int) : Bitmap{
        var width = userSelectedBitmap.width
        var height = userSelectedBitmap.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if(bitmapRatio >= 1){
            //görsel yatay
            width = maxSize
            val smallHeight = width / bitmapRatio
            height = smallHeight.toInt()
        }else{
            //görsel dikey
            height = maxSize
            val smallWidth = height * bitmapRatio
            width = smallWidth.toInt()
        }
        return Bitmap.createScaledBitmap(userSelectedBitmap,width,height,true)
    }

    private fun handleResponseForInsert(){
        val action = FoodFragmentDirections.actionFoodFragmentToListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}