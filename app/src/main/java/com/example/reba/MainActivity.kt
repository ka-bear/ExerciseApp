package com.example.reba

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.text.SpannableString
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random

private var mAuth: FirebaseAuth? = null
public lateinit var arrayListX: ArrayList<Double>
public var improvStr = "Exercise to find improvements!"
public var lines = 0



private class PoseAnalyzer(private val poseFoundListener: (Pose) -> Unit) : ImageAnalysis.Analyzer {


    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector = PoseDetection.getClient(options);

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Accurate pose detector on static images, when depending on the pose-detection-accurate sdk

            poseDetector
                .process(image)
                .addOnSuccessListener { pose ->
                    poseFoundListener(pose)
                    imageProxy.close()
                }
                .addOnFailureListener { error ->
                    Log.d(TAG, "Failed to process the image")
                    error.printStackTrace()
                    imageProxy.close()
                }
        }
    }
}

class RectOverlay constructor(context: Context?, attributeSet: AttributeSet?) :
    View(context, attributeSet) {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val STROKE_WIDTH = 3f // has to be float
    private val drawColor = Color.RED

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    fun clear() {
        extraCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    internal fun drawLine(
        startLandmark: PoseLandmark?,
        endLandmark: PoseLandmark?
    ) {
        val start = startLandmark!!.position
        val end = endLandmark!!.position


        val xmul = 3.3f;
        val ymul = 3.3f;

        extraCanvas.drawLine(
            (start.x * xmul) - 250, start.y * ymul, (end.x * xmul) - 250, end.y * ymul, paint
        )
        invalidate();
    }

    internal fun drawNeck(
        _occhioSx: PoseLandmark?,
        _occhioDx: PoseLandmark?,
        _spallaSx: PoseLandmark?,
        _spallaDx: PoseLandmark?
    ) {

        val xmul = 3.3f;
        val ymul = 3.3f;


        val occhioSx = _occhioSx!!.position
        val occhioDx = _occhioDx!!.position
        val spallaSx = _spallaSx!!.position
        val spallaDx = _spallaDx!!.position


        val fineColloX = occhioDx.x + ((occhioSx.x - occhioDx.x) / 2);
        val fineColloY = occhioDx.y + ((occhioSx.y - occhioDx.y) / 2);

        val inizioColloX = spallaDx.x + ((spallaSx.x - spallaDx.x) / 2);
        val inizioColloY = spallaDx.y + ((spallaSx.y - spallaDx.y) / 2);

        extraCanvas.drawLine(
            (fineColloX * xmul) - 250,
            fineColloY * ymul,
            (inizioColloX * xmul) - 250,
            inizioColloY * ymul,
            paint
        )

        extraCanvas.drawLine(
            (occhioSx.x * xmul) - 250,
            occhioSx.y * ymul,
            (occhioDx.x * xmul) - 250,
            occhioDx.y * ymul,
            paint
        )
        invalidate();
    }


}

private lateinit var textToSpeech: TextToSpeech

var time = 0.0

class MainActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var textView: TextView;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        improvStr = "Exercise to find improvements!"
        lines = 0

        mAuth = FirebaseAuth.getInstance()

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.UK
            }
        }
        val voiceObj = Voice(
            "en-us-x-sfg#male_1-local", Locale.getDefault(),
            1, 1, false, null
        )
        textToSpeech.voice = voiceObj

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }


        val prefs = getSharedPreferences("REBA", Context.MODE_PRIVATE)
        val firstTime = prefs.getBoolean("g", true)
        val editor: SharedPreferences.Editor = prefs.edit()
        if (firstTime) {
            editor.putBoolean("g", false)
            editor.commit()
            Log.i("firstTime","AA")

            val spannedDesc = SpannableString("Go to home page, where you can see your progress over time and improvements")

            TapTargetView.showFor(this, TapTarget.forView(findViewById(R.id.button9), "Home Button", spannedDesc).cancelable(false)
                .drawShadow(true)
                .tintTarget(false), object : TapTargetView.Listener() {

                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)

                    val spannedDesc2 = SpannableString("This gives you a how to for the various exercises this app supports and how to use the camera feature")
                    TapTargetView.showFor(this@MainActivity, TapTarget.forView(findViewById(R.id.button10), "Help Button", spannedDesc2).cancelable(false)
                        .drawShadow(true)
                        .tintTarget(false), object : TapTargetView.Listener() {

                    })
                }
            })
        }

        // Set up the listener for take photo button
        // camera_capture_button.setOnClickListener { takePhoto() }
        Log.i("RIRTHIVARDIAN", "BLOKE")

        Firebase.database.reference.child("links").get().addOnCompleteListener {
            var randNum = Random.nextInt(0, it.result.children.count())
            val link = it.result.children.elementAt(randNum).value.toString()
            Log.i("RIRTHIVARDIAN", link)
            if (link == "") {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
            } else {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                startActivity(intent)
            }
        }

        (findViewById<Button>(R.id.button10)).setOnClickListener { _ ->
            val intent = Intent(this, helppg::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

        }

        (findViewById<Button>(R.id.button9)).setOnClickListener { _ ->
            Log.i("nigg12", "1")

            var link = "https://reba-13fed-default-rtdb.asia-southeast1.firebasedatabase.app/"

            var database = Firebase.database(link).reference

            Log.i("nigg12", "2")



            val user = mAuth?.currentUser
            Log.i("nigg12", "NAME: " + user?.email)
            Log.i("nigg12", "ID2: " + user?.uid)


            //Log.i("nigg12","3" + " " + user::class.java.typeName)

/*
            database.child(user?.uid+"/datetime").get().addOnCompleteListener {
                x = parseString(it.result.toString())
                Log.i("nigg12",x.toString())
                user.let {
                    if(x.isEmpty()){
                        database.child(it?.uid + "/datetime").setValue("0,"+time)
                    }
                    else {
                        database.child(it?.uid + "/datetime").setValue(""+database.child(mAuth?.currentUser?.uid+"/datetime").get()+"," + x.get(-2)+","+time)
                    }
                }
            }
*/

            database.child(user?.uid + "/datetime").get().addOnCompleteListener {
                Log.i("nigg12", "4")

                Log.i("resultsmaybe?", it.result.value.toString())
                arrayListX = parseString(it.result.value.toString())
                if (time != 0.0) {
                    arrayListX.add(arrayListX[arrayListX.size - 2] + 1)
                    arrayListX.add(time)
                }

                Log.i("resultsmaybe?", arrayListX.toString())

                Log.i("infoo", "HEY WHAT UP")

                time = 0.0

                user.let {
                    database.child(it?.uid + "/datetime")
                        .setValue(arrayListX.toString().subSequence(1, arrayListX.toString().length - 1))
                }

                val intent = Intent(this, homePage::class.java)
                intent.putExtra("data", arrayListX.toString().subSequence(1, arrayListX.toString().length - 1))
                intent.putExtra("improv", improvStr)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);


            }


            Log.i("nigg12", "5")


        }

        textView = findViewById(R.id.text_view_id)

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    fun getAngle(
        firstPoint: PoseLandmark,
        midPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Double {

        var result = Math.toDegrees(
            atan2(
                lastPoint.getPosition().y.toDouble() - midPoint.getPosition().y,
                lastPoint.getPosition().x.toDouble() - midPoint.getPosition().x
            )
                    - atan2(
                firstPoint.getPosition().y - midPoint.getPosition().y,
                firstPoint.getPosition().x - midPoint.getPosition().x
            )
        )
        result = Math.abs(result) // Angle should never be negative
        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }
        return result
    }

    fun getNeckAngle(orecchio: PoseLandmark, spalla: PoseLandmark): Double {

        var result = Math.toDegrees(
            atan2(
                spalla.getPosition().y.toDouble() - spalla.getPosition().y,
                (spalla.getPosition().x + 100).toDouble() - spalla.getPosition().x
            )
                    - atan2(
                orecchio.getPosition().y - spalla.getPosition().y,
                orecchio.getPosition().x - spalla.getPosition().x
            )
        )

        result = Math.abs(result) // Angle should never be negative

        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }
        return result
    }

    private fun onTextFound(pose: Pose) {
        try {


            val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
            val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
            val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
            val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
            val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
            val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
            val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
            val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
            val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
            val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
            val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

            val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
            val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
            val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
            val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
            val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
            val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
            val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
            val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
            val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
            val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

            val occhioSx = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
            val occhioDx = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);

            val orecchioDx = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR);
            val orecchioSx = pose.getPoseLandmark(PoseLandmark.LEFT_EAR);


            if (rightAnkle != null) {
                val l = arrayOf<String>(
                    "" + (convertToAngle(
                        pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).position3D,
                        pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).position3D,
                        pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).position3D
                    ) + convertToAngle(
                        pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).position3D,
                        pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).position3D,
                        pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).position3D
                    )) / 2,
                    "" + (convertToAngle(
                        pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).position3D,
                        pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).position3D,
                        pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).position3D
                    ) + convertToAngle(
                        pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).position3D,
                        pose.getPoseLandmark(PoseLandmark.LEFT_HIP).position3D,
                        pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).position3D
                    )) / 2,
                    "" + (convertToAngle(
                        pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).position3D,
                        pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).position3D,
                        pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE).position3D
                    ) + convertToAngle(
                        pose.getPoseLandmark(PoseLandmark.LEFT_HIP).position3D,
                        pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).position3D,
                        pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).position3D
                    )) / 2,
                    "" + (convertToAngle(
                        pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).position3D,
                        pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).position3D,
                        PointF3D.from(
                            pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).position3D.x,
                            pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).position3D.y,
                            pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).position3D.z + 30
                        )
                    ))
                )

                Log.i("infoo", MLPClassifier.main(l).toString())
                val e = MLPClassifier.main(l)

                val sew = (convertToAngle(
                    pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).position3D,
                    pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).position3D,
                    pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).position3D
                ) + convertToAngle(
                    pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).position3D,
                    pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).position3D,
                    pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).position3D
                )) / 2
                val shk = (convertToAngle(
                    pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).position3D,
                    pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).position3D,
                    pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).position3D
                ) + convertToAngle(
                    pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).position3D,
                    pose.getPoseLandmark(PoseLandmark.LEFT_HIP).position3D,
                    pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).position3D
                )) / 2
                val hka = (convertToAngle(
                    pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).position3D,
                    pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).position3D,
                    pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE).position3D
                ) + convertToAngle(
                    pose.getPoseLandmark(PoseLandmark.LEFT_HIP).position3D,
                    pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).position3D,
                    pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).position3D
                )) / 2

                if (e == 0) {
                    textView.setText("")
                }
                if (e == 1) {
                    textView.setText("Plank Detected")
                }
                if (e == 2) {
                    textView.setText("Wall Sits Detected")
                }

                if (e != 0) {
                    time += 1.0 / 30.0
                    Log.i("resultsmaybe?", " " + shk + " " + hka)
                    Log.i("resultsmaybe?", " " + (abs((time / 20).toInt().toDouble() * 20.0 - time)).toString())



                    if (e == 2 && abs((time /2).toInt().toDouble() * 2 - time) <= 0.05) {
                        if(improvStr.contains("Exercise to find improvements!")){
                            improvStr=""
                            lines = 0
                        }
                        if (!(shk >= 1.4 && shk <= 1.9)) {
                            Log.i(
                                "foobar",
                                "" + (((time / 20).toInt()
                                    .toDouble() * 20.0 - time) <= 0.05).toString()
                            )
                            textToSpeech.speak(
                                "Make your hip at a right angle!",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "tts1"
                            )
                            if(lines<5.0){
                                improvStr+="Wallsits: Keep your hip at a right angle\n"
                                lines++
                            } else{
                                improvStr = improvStr.substring(improvStr.indexOf("\n") + 1)
                                improvStr+="Wallsits: Keep your hip at a right angle\n"

                            }
                        } else if (!(hka >= 0.4 && hka <= 1.6)) {
                            textToSpeech.speak(
                                "Make your knees at a right angle!",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "tts1"
                            )

                            if(lines<5.0){
                                improvStr+="Wallsits: Keep your knees at a right angle\n"
                                lines++
                            } else{
                                improvStr = improvStr.substring(improvStr.indexOf("\n") + 1)
                                improvStr+="Wallsits: Keep your knees at a right angle\n"

                            }
                        }
                        else{
                            textToSpeech.speak(
                                "Correct posture detected!",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "tts1"
                            )
                            if(lines<5.0){
                                improvStr+="Correct Posture Detected\n"
                                lines++
                            } else{
                                improvStr = improvStr.substring(improvStr.indexOf("\n") + 1)
                                improvStr+="Correct Posture Detected\n"
                            }
                        }
                    }
                    if (e == 1 && abs((time / 2).toInt().toDouble() * 2.0 - time) <= 0.05) {
                        if(improvStr.contains("Exercise to find improvements!")){
                            improvStr=""
                            lines = 0
                        }
                        if (!(shk >= 2.5 && shk <= 3.3)) {
                            textToSpeech.speak(
                                "Keep your back straighter!",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "tts1"
                            )
                            if(lines<5.0){
                                improvStr+="Planks: Keep your back straight\n"
                                lines++
                            } else{
                                improvStr = improvStr.substring(improvStr.indexOf("\n") + 1)
                                improvStr+="Planks: Keep your back straight\n"
                            }

                        } else if (!(hka >= 2.5 && hka <= 3.3)) {
                            textToSpeech.speak(
                                "Keep your legs straighter!",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "tts1"
                            )
                            if(lines<5.0){
                                improvStr+="Planks: Keep your legs straight\n"
                                lines++
                            } else{
                                improvStr = improvStr.substring(improvStr.indexOf("\n") + 1)
                                improvStr+="Planks: Keep your legs straight\n"
                            }

                        }
                        else{
                            textToSpeech.speak(
                                "Correct posture detected!",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "tts1"
                            )
                            if(lines<5.0){
                                improvStr+="Correct Posture Detected\n"
                                lines++
                            } else{
                                improvStr = improvStr.substring(improvStr.indexOf("\n") + 1)
                                improvStr+="Correct Posture Detected\n"
                            }
                        }
                    }
                }

                if (time == 200.0) {
                    val builder = AlertDialog.Builder(this)
                    //set title for alert dialog
                    builder.setTitle("Too much exercise!")
                    //set message for alert dialog
                    builder.setMessage("You are mighty fit! you have exercised for a long time! In order to really benefit you should take a break and let your muscles grow")
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    //performing positive action
                    builder.setPositiveButton("Noted") { _, _ -> }

                    builder.show()

                }

            }

            val builder = StringBuilder()
            rect_overlay.clear()

            // disegno il collo come la media tra occhi e orecchie
            if (occhioSx != null && occhioDx != null && leftShoulder != null && rightShoulder != null) {
                rect_overlay.drawNeck(occhioSx, occhioDx, leftShoulder, rightShoulder);
            }

            // disegno il collo visto lateralmente da sinistra
            if (orecchioSx != null && leftShoulder != null) {
                rect_overlay.drawLine(orecchioSx, leftShoulder)
                var angoloCollo = getNeckAngle(orecchioSx, leftShoulder);
                builder.append("${90 - angoloCollo.toInt()} collo (da sx) \n")
            }

            // disegno il collo visto lateralmente da destra
            if (orecchioDx != null && rightShoulder != null) {
                rect_overlay.drawLine(orecchioDx, rightShoulder)
                var angoloCollo = getNeckAngle(orecchioDx, rightShoulder);
                builder.append("${90 - angoloCollo.toInt()} collo (da dx) \n")
            }

            // angolo busto destra
            if (rightShoulder != null && rightHip != null && rightKnee != null) {
                var angoloBusto = getAngle(rightShoulder, rightHip, rightKnee);
                builder.append("${180 - angoloBusto.toInt()} busto (da dx) \n")
            }

            // angolo busto sinistra
            if (leftShoulder != null && leftHip != null && leftKnee != null) {
                var angoloBusto = getAngle(leftShoulder, leftHip, leftKnee);
                builder.append("${180 - angoloBusto.toInt()} busto (da sx) \n")
            }


            // angolo gamba destra
            if (rightHip != null && rightKnee != null && rightAnkle != null) {
                var angoloBusto = getAngle(rightHip, rightKnee, rightAnkle);
                builder.append("${180 - angoloBusto.toInt()} gamba (da dx) \n")
            }

            // angolo gamba sinistra
            if (leftHip != null && leftKnee != null && leftAnkle != null) {
                var angoloBusto = getAngle(leftHip, leftKnee, leftAnkle);
                builder.append("${180 - angoloBusto.toInt()} gamba (da sx) \n")
            }


            if (leftShoulder != null && rightShoulder != null) {
                rect_overlay.drawLine(leftShoulder, rightShoulder)
            }

            if (leftHip != null && rightHip != null) {
                rect_overlay.drawLine(leftHip, rightHip)
            }

            if (leftShoulder != null && leftElbow != null) {
                rect_overlay.drawLine(leftShoulder, leftElbow)
            }

            if (leftElbow != null && leftWrist != null) {
                rect_overlay.drawLine(leftElbow, leftWrist)
            }

            if (leftShoulder != null && leftHip != null) {
                rect_overlay.drawLine(leftShoulder, leftHip)
            }

            if (leftHip != null && leftKnee != null) {
                rect_overlay.drawLine(leftHip, leftKnee)
            }

            if (leftKnee != null && leftAnkle != null) {
                rect_overlay.drawLine(leftKnee, leftAnkle)
            }

            if (leftWrist != null && leftThumb != null) {
                rect_overlay.drawLine(leftWrist, leftThumb)
            }

            if (leftWrist != null && leftPinky != null) {
                rect_overlay.drawLine(leftWrist, leftPinky)
            }

            if (leftWrist != null && leftIndex != null) {
                rect_overlay.drawLine(leftWrist, leftIndex)
            }

            if (leftIndex != null && leftPinky != null) {
                rect_overlay.drawLine(leftIndex, leftPinky)
            }

            if (leftAnkle != null && leftHeel != null) {
                rect_overlay.drawLine(leftAnkle, leftHeel)
            }

            if (leftHeel != null && leftFootIndex != null) {
                rect_overlay.drawLine(leftHeel, leftFootIndex)
            }

            if (rightShoulder != null && rightElbow != null) {
                rect_overlay.drawLine(rightShoulder, rightElbow)
            }

            if (rightElbow != null && rightWrist != null) {
                rect_overlay.drawLine(rightElbow, rightWrist)
            }

            if (rightShoulder != null && rightHip != null) {
                rect_overlay.drawLine(rightShoulder, rightHip)
            }

            if (rightHip != null && rightKnee != null) {
                rect_overlay.drawLine(rightHip, rightKnee)
            }

            if (rightKnee != null && rightAnkle != null) {
                rect_overlay.drawLine(rightKnee, rightAnkle)
            }

            if (rightWrist != null && rightThumb != null) {
                rect_overlay.drawLine(rightWrist, rightThumb)
            }

            if (rightWrist != null && rightPinky != null) {
                rect_overlay.drawLine(rightWrist, rightPinky)
            }

            if (rightWrist != null && rightIndex != null) {
                rect_overlay.drawLine(rightWrist, rightIndex)
            }

            if (rightIndex != null && rightPinky != null) {
                rect_overlay.drawLine(rightIndex, rightPinky)
            }

            if (rightAnkle != null && rightHeel != null) {
                rect_overlay.drawLine(rightAnkle, rightHeel)
            }

            if (rightHeel != null && rightFootIndex != null) {
                rect_overlay.drawLine(rightHeel, rightFootIndex)
            }


        } catch (e: java.lang.Exception) {
            Log.i("infoo", e.stackTraceToString())
            Toast.makeText(this@MainActivity, "Errore", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }


            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PoseAnalyzer(::onTextFound))
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        time = 0.0
        improvStr = "Exercise to find improvements!"
        lines = 0

    }

    fun convertToAngle(x1: PointF3D, x2: PointF3D, x3: PointF3D): Float {
        var v1 = listOf<Float>(x1.x - x2.x, x1.y - x2.y, x1.z - x2.z)
        var v2 = listOf<Float>(x3.x - x2.x, x3.y - x2.y, x3.z - x2.z)

        var v1mag = sqrt(v1[0] * v1[0] + v1[1] * v1[1] + v1[2] * v1[2])
        var v1norm = listOf<Float>(v1[0] / v1mag, v1[1] / v1mag, v1[2] / v1mag)

        var v2mag = sqrt(v2[0] * v2[0] + v2[1] * v2[1] + v2[2] * v2[2])
        var v2norm = listOf<Float>(v2[0] / v2mag, v2[1] / v2mag, v2[2] / v2mag)

        var res = v1norm[0] * v2norm[0] + v1norm[1] * v2norm[1] + v1norm[2] * v2norm[2]
        return acos(res)

    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

}

public fun parseString(s: String): ArrayList<Double> {
    val l = ArrayList<Double>();

    val k = s.split(",").toList()
    for (i in k) {
        l.add(i.toDouble())
    }
    return l
}