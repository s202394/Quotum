package com.quotum.quotum.quotum.ui.searchTrip

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quotum.quotum.quotum.R
import com.quotum.quotum.quotum.models.GetTripLocationResponseModel
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(var data: GetTripLocationResponseModel) :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>() {


    var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_trip, parent, false)
        mContext = parent.context
        return TripViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.textViewFrom.text = data.getResult()?.get(position)?.getSource()
        holder.textViewDestination.text = data.getResult()?.get(position)?.getDestination()
        holder.textViewCost.text =
            data.getResult()?.get(position)?.getBudget().toString() + data.getResult()
                ?.get(position)?.getCurrency().toString()
        holder.textViewVehicle.text =
            data.getResult()?.get(position)?.getModeOfTransport().toString()
        holder.textViewDate.text =
            getDate(data.getResult()?.get(position)?.getStartDate().toString())
        holder.textViewTime.text = data.getResult()?.get(position)?.getDays().toString() + "days"
        Picasso.get()
            .load(data.getResult()?.get(position)?.getPicture()?.get(0)).fit()
            .centerInside()
            .rotate(90F)
            .error(R.drawable.quotum)
            .into(holder.imageViewBHackground)
    }

    private fun getDate(input: String): String {
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.getDefault())

            val date = formatter.parse(input)

            val targetDateFormat =
                SimpleDateFormat("EEE, dd MMM, yyyy | hh:mm a", Locale.getDefault())
            println(targetDateFormat.format(date!!))
            return formatter.format(date)
        } catch (e: Exception) {
            Log.e("mDate", e.toString()) // this never gets called either
        }
        return input
    }

    override fun getItemCount(): Int {
        return data.getResult()?.size!!
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewFrom = itemView.findViewById(R.id.text_view_from) as TextView
        val textViewDestination = itemView.findViewById(R.id.text_view_destination) as TextView
        val textViewCost = itemView.findViewById(R.id.text_view_cost) as TextView
        val textViewVehicle = itemView.findViewById(R.id.textview_vehicle) as TextView
        val textViewDate = itemView.findViewById(R.id.textview_date) as TextView
        val textViewTime = itemView.findViewById(R.id.textview_time) as TextView
        val imageViewBHackground = itemView.findViewById(R.id.image_view_background) as ImageView

    }
}