package com.quotum.quotum.quotum

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.quotum.quotum.quotum.localdatabase.LocalDB
import com.quotum.quotum.quotum.models.PostTripRequestModel
import com.quotum.quotum.quotum.models.PostTripResponseModel
import com.quotum.quotum.quotum.network.QuotumClient
import com.quotum.quotum.quotum.services.MultipartUtility
import com.quotum.quotum.quotum.utility.Utils
import kotlinx.android.synthetic.main.activity_post_trip.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class PostTripActivity : AppCompatActivity(), View.OnClickListener {
    private val requestPermissionCode = 1
    private var mPostTrip: Button? = null
    private var mStartFrom: EditText? = null
    private var mDestination: EditText? = null
    private var mDaysOfTrip: EditText? = null
    private var mStay: EditText? = null
    private var mTripBudget: EditText? = null
    private var mTripDate: EditText? = null
    private var mTripMonth: EditText? = null
    private var mTripYear: EditText? = null
    private var mTripTimeHour: EditText? = null
    private var mTripTimeMin: EditText? = null
    private var mTripTimeAmPm: EditText? = null
    private var mTripDetails: EditText? = null
    private var mAdultPerson: EditText? = null
    private var mChildPerson: EditText? = null
    private var mTotalPerson: EditText? = null
    private var mCarLayout: RelativeLayout? = null
    private var mBusLayout: RelativeLayout? = null
    private var mBikeLayout: RelativeLayout? = null
    private var mTrainLayout: RelativeLayout? = null
    private var mPlaneLayout: RelativeLayout? = null
    private var mBusImage: ImageView? = null
    private var mCarImage: ImageView? = null
    private var mBikeImage: ImageView? = null
    private var mTrainImage: ImageView? = null
    private var mPlaneImage: ImageView? = null
    private var mTripDateLayout: LinearLayout? = null
    private var mTripTimeLayout: LinearLayout? = null

    private var bitmap: Bitmap? = null

    private val myCalendar = Calendar.getInstance()

    private var modeOfTransport = "Bike"
    private var noOfAdults = 0
    private var noOfChild = 0

    private var imageUploadResult = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_trip)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initViews()

        enableRuntimePermission()
    }

    private fun initViews() {
        mPostTrip = findViewById(R.id.post_trip)
        mStartFrom = findViewById(R.id.ed_start_from)
        mDestination = findViewById(R.id.ed_destination)
        mCarImage = findViewById(R.id.iv_car)
        mBusImage = findViewById(R.id.iv_bus)
        mPlaneImage = findViewById(R.id.iv_plane)
        mTrainImage = findViewById(R.id.iv_train)
        mBikeImage = findViewById(R.id.iv_bike)
        mBikeLayout = findViewById(R.id.rl_bike)
        mCarLayout = findViewById(R.id.rl_car)
        mTrainLayout = findViewById(R.id.rl_train)
        mBusLayout = findViewById(R.id.rl_bus)
        mPlaneLayout = findViewById(R.id.rl_plane)
        mDaysOfTrip = findViewById(R.id.ed_days_of_trip)
        mStay = findViewById(R.id.ed_stay)
        mTripBudget = findViewById(R.id.ed_trip_budget)
        mTripDateLayout = findViewById(R.id.ll_trip_date)
        mTripTimeLayout = findViewById(R.id.ll_trip_time)
        mTripTimeHour = findViewById(R.id.ed_hour)
        mTripTimeMin = findViewById(R.id.ed_minute)
        mTripTimeAmPm = findViewById(R.id.ed_am_pm)
        mTripYear = findViewById(R.id.ed_year)
        mTripMonth = findViewById(R.id.ed_month)
        mTripDate = findViewById(R.id.ed_date)
        mTripDetails = findViewById(R.id.ed_trip_details)
        mAdultPerson = findViewById(R.id.ed_adult)
        mChildPerson = findViewById(R.id.ed_child)
        mTotalPerson = findViewById(R.id.ed_total_person)

        initListeners()
    }

    private fun initListeners() {
        ll_camera.setOnClickListener(this)
        mPostTrip!!.setOnClickListener(this)
        mCarLayout!!.setOnClickListener(this)
        mBusLayout!!.setOnClickListener(this)
        mBikeLayout!!.setOnClickListener(this)
        mTrainLayout!!.setOnClickListener(this)
        mPlaneLayout!!.setOnClickListener(this)
        mTripDateLayout!!.setOnClickListener(this)
        mTripTimeLayout!!.setOnClickListener(this)
        mTripDate!!.setOnClickListener(this)
        mTripMonth!!.setOnClickListener(this)
        mTripYear!!.setOnClickListener(this)
        mTripTimeHour!!.setOnClickListener(this)
        mTripTimeMin!!.setOnClickListener(this)
        mTripTimeAmPm!!.setOnClickListener(this)

        mAdultPerson?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setPersonValues()
            }
        })

        mChildPerson?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setPersonValues()
            }
        })

        setTimeHint()
        setDateHint()
    }

    private fun setPersonValues() {
        var child = mChildPerson?.text.toString()
        var adult = mAdultPerson?.text.toString()
        if (child.isEmpty())
            child = "0"
        if (adult.isEmpty())
            adult = "0"
        mTotalPerson?.setText((child.toInt() + adult.toInt()).toString())
    }

    private fun setTimeHint() {
        setTime(myCalendar[Calendar.HOUR_OF_DAY], myCalendar[Calendar.MINUTE], "hint")
    }

    private fun setDateHint() {
        updateDate("hint")
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                bitmap = data?.extras!!["data"] as Bitmap?
                val mDrawable: Drawable = BitmapDrawable(resources, bitmap)
                ll_camera?.background = mDrawable
            }
        }

    private fun openActivityForResult() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        resultLauncher.launch(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_camera -> {
                openActivityForResult()
            }
            R.id.post_trip -> {
                tripValidation()
            }
            R.id.rl_bike -> {
                modeOfTransport = "Bike"
                checkLayoutCondition(
                    mBikeLayout, mCarLayout, mBusLayout, mTrainLayout, mPlaneLayout
                )
                checkImageCondition(
                    mBikeImage, mCarImage, mBusImage, mTrainImage, mPlaneImage
                )
            }
            R.id.rl_car -> {
                modeOfTransport = "Car"
                checkLayoutCondition(
                    mCarLayout, mBikeLayout, mBusLayout, mTrainLayout, mPlaneLayout
                )
                checkImageCondition(
                    mCarImage, mBikeImage, mBusImage, mTrainImage, mPlaneImage
                )
            }
            R.id.rl_plane -> {
                modeOfTransport = "Air Plane"
                checkLayoutCondition(
                    mPlaneLayout, mBikeLayout, mCarLayout, mBusLayout, mTrainLayout
                )
                checkImageCondition(
                    mPlaneImage, mBikeImage, mCarImage, mBusImage, mTrainImage
                )
            }
            R.id.rl_train -> {
                modeOfTransport = "Train"
                checkLayoutCondition(
                    mTrainLayout, mBikeLayout, mCarLayout, mBusLayout, mPlaneLayout
                )
                checkImageCondition(
                    mTrainImage, mBikeImage, mCarImage, mBusImage, mPlaneImage
                )
            }
            R.id.rl_bus -> {
                modeOfTransport = "Bus"
                checkLayoutCondition(
                    mBusLayout, mBikeLayout, mCarLayout, mTrainLayout, mPlaneLayout
                )
                checkImageCondition(
                    mBusImage, mBikeImage, mCarImage, mTrainImage, mPlaneImage
                )
            }
            R.id.ed_year, R.id.ed_month, R.id.ed_date, R.id.ll_trip_date -> {
                DatePickerDialog(
                    this@PostTripActivity, date, myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
                ).show()
            }
            R.id.ll_trip_time, R.id.ed_hour, R.id.ed_minute, R.id.ed_am_pm -> {
                val mTimePicker = TimePickerDialog(
                    this@PostTripActivity,
                    time,
                    myCalendar[Calendar.HOUR_OF_DAY],
                    myCalendar[Calendar.MINUTE],
                    false
                ) //Yes 24 hour time

                mTimePicker.setTitle("Select Time")
                mTimePicker.show()
            }
        }
    }

    private fun setTime(selectedHour: Int, selectedMinute: Int, type: String) {
        try {
            val sdf = SimpleDateFormat("H:mm", Locale.getDefault())
            val dateObj = sdf.parse("$selectedHour:$selectedMinute")
            println(dateObj)
            val suffix: String
            var hour = selectedHour
            if (selectedHour > 11) {
                suffix = "PM"
                if (selectedHour > 12)
                    hour -= 12
            } else {
                suffix = "AM"
                if (selectedHour == 0)
                    hour = 12
            }
            if (type.equals("hint", ignoreCase = true)) {
                mTripTimeHour?.hint = hour.toString()
                mTripTimeAmPm?.hint = suffix
                mTripTimeMin?.hint = SimpleDateFormat("mm", Locale.getDefault()).format(dateObj!!)
            } else {
                mTripTimeMin?.setText(SimpleDateFormat("mm", Locale.getDefault()).format(dateObj!!))
                mTripTimeHour?.setText(hour.toString())
                mTripTimeAmPm?.setText(suffix)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun checkLayoutCondition(
        mLayout1: RelativeLayout?,
        mLayout2: RelativeLayout?,
        mLayout3: RelativeLayout?,
        mLayout4: RelativeLayout?,
        mLayout5: RelativeLayout?
    ) {
        mLayout1?.setBackgroundResource(R.drawable.filled_bg)
        mLayout2?.setBackgroundResource(R.drawable.outer_bg)
        mLayout3?.setBackgroundResource(R.drawable.outer_bg)
        mLayout4?.setBackgroundResource(R.drawable.outer_bg)
        mLayout5?.setBackgroundResource(R.drawable.outer_bg)
    }

    private fun checkImageCondition(
        mImage1: ImageView?,
        mImage2: ImageView?,
        mImage3: ImageView?,
        mImage4: ImageView?,
        mImage5: ImageView?
    ) {
        mImage1?.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        mImage2?.setColorFilter(
            ContextCompat.getColor(this, R.color.colorGray),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        mImage3?.setColorFilter(
            ContextCompat.getColor(this, R.color.colorGray),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        mImage4?.setColorFilter(
            ContextCompat.getColor(this, R.color.colorGray),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        mImage5?.setColorFilter(
            ContextCompat.getColor(this, R.color.colorGray),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    // Method to save an bitmap to a file
    private fun bitmapToFile(bitmap: Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }

    private fun tripValidation() {
        /* val uploader: IUnknownUploader = object : IUnknownUploader(this@PostTripActivity) {
 //            override fun onReceiveResult(result: Result?) {
 //                if (result != null) {
 //                    when (result.status) {
 //                        DOWNLOAD_ERROR -> {
 //
 //                        }
 //                        DOWNLOAD_SUCCESS -> {
 //
 //                        }
 //                    }
 //                }
 //            }
         }
         uploader.execute(bitmap?.let { bitmapToFile(it).path })*/

        uploadMediaFile()


        val source = mStartFrom?.text.toString().trim()
        val destination = mDestination?.text.toString().trim()
        val daysOfTrip = mDaysOfTrip?.text.toString().trim()
        val stay = mStay?.text.toString().trim()
        val tripBudget = mTripBudget?.text.toString().trim()
        val tripHour = mTripTimeHour?.text.toString().trim()
        val tripMin = mTripTimeMin?.text.toString().trim()
        val tripAmPm = mTripTimeAmPm?.text.toString().trim()
        val tripYear = mTripYear?.text.toString().trim()
        val tripMonth = mTripMonth?.text.toString().trim()
        val tripDate = mTripDate?.text.toString().trim()
        val tripDetails = mTripDetails?.text.toString().trim()
        val adult = mAdultPerson?.text.toString().trim()
        val child = mChildPerson?.text.toString().trim()
        val total = mTotalPerson?.text.toString().trim()
        if (source.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter source", Toast.LENGTH_SHORT).show()
            mStartFrom!!.requestFocus()
            return
        }
        if (destination.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter destination", Toast.LENGTH_SHORT)
                .show()
            mDestination!!.requestFocus()
            return
        }
        if (daysOfTrip.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter days of trip", Toast.LENGTH_SHORT)
                .show()
            mDaysOfTrip!!.requestFocus()
            return
        }
        if (stay.isEmpty()) {
            Toast.makeText(
                applicationContext, "Please enter where will you stay", Toast.LENGTH_SHORT
            ).show()
            mStay!!.requestFocus()
            return
        }
        if (tripBudget.isEmpty()) {
            Toast.makeText(
                applicationContext, "Please enter trip budget", Toast.LENGTH_SHORT
            ).show()
            mTripBudget!!.requestFocus()
            return
        }
        if (tripDate.isEmpty()) {
            Toast.makeText(
                applicationContext, "Please enter trip date", Toast.LENGTH_SHORT
            ).show()
            mTripDate!!.requestFocus()
            return
        }
        if (tripHour.isEmpty()) {
            Toast.makeText(
                applicationContext, "Please enter trip time", Toast.LENGTH_SHORT
            ).show()
            mTripTimeHour!!.requestFocus()
            return
        }
        if (total.isEmpty() || total.toInt() == 0) {
            Toast.makeText(
                applicationContext, "Please enter travelling with", Toast.LENGTH_SHORT
            ).show()
            mTotalPerson!!.requestFocus()
            return
        }
        if (tripDetails.isEmpty()) {
            Toast.makeText(
                applicationContext, "Please enter trip details", Toast.LENGTH_SHORT
            ).show()
            mTripDetails!!.requestFocus()
            return
        }

        if (!Utils.isNetworkConnected(this)) {
            Toast.makeText(
                applicationContext, "Please check your internet connection.", Toast.LENGTH_SHORT
            ).show()
            return
        }

        val model = PostTripRequestModel()

        val detailsList = listOf(tripDetails)

        model.currency = ""
        model.budget = tripBudget.toInt()
        model.destination = destination
        model.modeOfTransport = modeOfTransport
        model.source = source
        model.destination = destination
        model.startDate = getStartDate()
        model.created = getCurrentDate()
        model.endDate = getEndDate(daysOfTrip.toInt())
        model.persons?.adult = adult.toInt()
        model.persons?.children = child.toInt()
        model.location?.lat = 28.404058
        model.location?.lng = 78.109885
        model.details = detailsList

        LocalDB.getUserId(this)?.let {
            LocalDB.getUserToken(this)?.let { it1 ->
                QuotumClient.instance.postUserTrip(it, it1, model)
                    .enqueue(object : Callback<PostTripResponseModel> {
                        override fun onFailure(call: Call<PostTripResponseModel>, t: Throwable) {
                            Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(
                            call: Call<PostTripResponseModel>,
                            response: Response<PostTripResponseModel>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    applicationContext,
                                    "Trip has been post successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                when (response.code()) {
                                    500 -> Toast.makeText(
                                        applicationContext,
                                        "Server is not responding right now. Please try again later",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    else -> Toast.makeText(
                                        applicationContext,
                                        "Unknown error.Please try again later",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    })
            }
        }
    }

    private fun uploadMediaFile() {
        Thread {
            runOnUiThread {
                uploadMedia()
            }
        }.start()
    }

    private var date = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, monthOfYear)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDate("text")
    }

    private var time =
        TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            myCalendar.set(Calendar.MINUTE, selectedMinute)
            setTime(selectedHour, selectedMinute, "text")
        }

    private fun updateDate(type: String) {
        val dateFormat = "dd" //In which you need put here
        val monthFormat = "MMM" //In which you need put here
        val yearFormat = "yyyy" //In which you need put here

        val sdfDate = SimpleDateFormat(dateFormat, Locale.getDefault())
        val sdfMonth = SimpleDateFormat(monthFormat, Locale.getDefault())
        val sdfYear = SimpleDateFormat(yearFormat, Locale.getDefault())

        if (type.equals("hint", ignoreCase = true)) {
            mTripDate!!.hint = sdfDate.format(myCalendar.time)
            mTripMonth!!.hint = sdfMonth.format(myCalendar.time)
            mTripYear!!.hint = sdfYear.format(myCalendar.time)
        } else {
            mTripDate!!.setText(sdfDate.format(myCalendar.time))
            mTripMonth!!.setText(sdfMonth.format(myCalendar.time))
            mTripYear!!.setText(sdfYear.format(myCalendar.time))
        }
    }

    private fun enableRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(
                this, "CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), requestPermissionCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, result: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, result)
        when (requestCode) {
            requestPermissionCode -> if (result.isNotEmpty() && result[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(
                    this,
                    "Permission Canceled, Now your application cannot access CAMERA.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getStartDate(): String {
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

            return formatter.format(myCalendar.time)
        } catch (e: Exception) {
            Log.e("mDate", e.toString()) // this never gets called either
        }
        return ""
    }

    private fun getEndDate(day: Int): String {
        try {
            myCalendar.add(Calendar.DAY_OF_MONTH, day)
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

            return formatter.format(myCalendar.time)
        } catch (e: Exception) {
            Log.e("mDate", e.toString()) // this never gets called either
        }
        return ""
    }

    private fun getCurrentDate(): String {
        try {
            val myCalendar = Calendar.getInstance()
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

            return formatter.format(myCalendar.time)
        } catch (e: Exception) {
            Log.e("mDate", e.toString()) // this never gets called either
        }
        return ""
    }

    private fun uploadMedia() {
        try {
            val file = bitmap?.let { bitmapToFile(it) }

            val charset = "UTF-8"
            val uploadFile1 = File(file!!.path!!)
            val requestURL: String =
                QuotumClient.BASE_URL + "/api/users/uploadMedia?access_token=" + LocalDB.getUserToken(
                    this
                )
            val multipart = MultipartUtility(requestURL, charset)

//            multipart.addHeaderField("User-Agent", "CodeJava");
//            multipart.addHeaderField("Test-Header", "Header-Value");
//            multipart.addFormField("friend_id", "Cool Pictures")
//            multipart.addFormField("userid", "Java,upload,Spring")
            multipart.addFilePart("photo", uploadFile1)
            val response = multipart.finish()
            if (response.code == 200) {
                val responseBody = response.responseBody
                responseBody?.let { parseResponse(it) }
                handleSuccess()
            } else {
                handleError()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleSuccess() {

    }

    private fun handleError() {

    }

    private fun parseResponse(responseBody: String): Result {
        try {
            val responseObj = JSONObject(responseBody)
            return if (responseObj.has("result")) {
                imageUploadResult = responseObj.optString("result", "")
                Result(DOWNLOAD_SUCCESS)
            } else {
                Result(DOWNLOAD_ERROR)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return Result(DOWNLOAD_ERROR)
    }

    class Result(var status: Int)
    companion object {
        const val DOWNLOAD_SUCCESS = 2
        const val DOWNLOAD_ERROR = 3
    }
}