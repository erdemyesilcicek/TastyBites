package com.erdemyesilcicek.foodbook.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.erdemyesilcicek.foodbook.adapter.FoodAdapter
import com.erdemyesilcicek.foodbook.databinding.FragmentListBinding
import com.erdemyesilcicek.foodbook.model.Food
import com.erdemyesilcicek.foodbook.roomdb.FoodDAO
import com.erdemyesilcicek.foodbook.roomdb.FoodDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var db : FoodDatabase
    private lateinit var foodDao: FoodDAO
    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(), FoodDatabase::class.java, "Foods").build()
        foodDao = db.foodDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { floatingActionButtonClicked(it) }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        getInfo()
    }

    private fun getInfo(){
        mDisposable.add(
            foodDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))
    }
    private fun handleResponse(foods : List<Food>){
        val adapter = FoodAdapter(foods)
        binding.recyclerView.adapter = adapter
    }

    fun floatingActionButtonClicked(view: View){
        val action = ListFragmentDirections.actionListFragmentToFoodFragment(info= "new", id=0)
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}