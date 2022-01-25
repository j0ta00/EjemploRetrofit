package es.iesnervion.juanjomz.ejemplo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import es.iesnervion.juanjomz.ejemplo.databinding.ActivityMainBinding
import es.iesnervion.juanjomz.ejemplo.databinding.ActivityMainBinding.inflate
import es.iesnervion.juanjomz.ejemplo.databinding.ItemDogsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(),android.widget.SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter:DogAdapter
    private val dogImage= mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.svDogs.setOnQueryTextListener(this)
        initRecyclerView()
    }

    private fun initRecyclerView(){
        adapter= DogAdapter(dogImage)
        binding.rcvDogs.layoutManager=LinearLayoutManager(this)
        binding.rcvDogs.adapter=adapter

    }
    override fun onQueryTextSubmit(query: String?): Boolean {
        if(!query.isNullOrEmpty()){
            searchByName(query.lowercase())
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }



    private fun getRetrofit(): Retrofit{
        return Retrofit.Builder().baseUrl("https://dog.ceo/api/breed/").
        addConverterFactory(GsonConverterFactory.create()).build()
    }

    private fun searchByName(query:String){
        CoroutineScope(Dispatchers.IO).launch {
            var call=getRetrofit().create(APIService::class.java).getDogsByBreeds("$query/images")
            val puppies:DogsResponse?=call.body()
            runOnUiThread {
                if(call.isSuccessful) {
                    val images: List<String> = puppies?.images ?: emptyList()
                    dogImage.clear()
                    dogImage.addAll(images)
                    adapter.notifyDataSetChanged()
                }else{
                    showError()
                }
            }

        }
    }

    private class DogAdapter(val images:List<String>):RecyclerView.Adapter<DogAdapter.ViewHolderDogs>(){

        class ViewHolderDogs(view: View):RecyclerView.ViewHolder(view){
            private val binding=ItemDogsBinding.bind(view)
            fun bind(image:String){
                Picasso.get().load(image).into(binding.ivDog)
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDogs {
            val layoutInflater=LayoutInflater.from(parent.context)
             return ViewHolderDogs(layoutInflater.inflate(R.layout.item_dogs,parent,false));
        }

        override fun onBindViewHolder(holder: ViewHolderDogs, position: Int) {
            val item=images[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int=images.size


    }
    private fun showError(){

        Toast.makeText(this,"Ha ocurrido un error",Toast.LENGTH_LONG).show()

    }



}