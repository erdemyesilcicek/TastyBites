package com.erdemyesilcicek.foodbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.erdemyesilcicek.foodbook.databinding.RecyclerRowBinding
import com.erdemyesilcicek.foodbook.model.Food
import com.erdemyesilcicek.foodbook.view.ListFragmentDirections

class FoodAdapter(val foodList : List<Food>) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
    class FoodViewHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FoodViewHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = foodList[position].name
        holder.itemView.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToFoodFragment(info = "old", id = foodList[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
}