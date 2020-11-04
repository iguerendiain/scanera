package nacholab.scanera.app

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import nacholab.scanera.Scanera
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object{
        private const val SCANERA_REQUEST_CODE = 1234
    }

    private val photoFile by lazy {File(
        getExternalFilesDir(null),
        "scaneratest.jpg"
    ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val runButton = findViewById<Button>(R.id.run)
        runButton.setOnClickListener {
            val scaneraIntent = Intent(this, Scanera::class.java)
            scaneraIntent.putExtra(Scanera.EXTRAS_FILE, photoFile)
            startActivityForResult(scaneraIntent, SCANERA_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SCANERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            findViewById<ImageView>(R.id.image_result).setImageBitmap(
                BitmapFactory.decodeFile(photoFile.absolutePath)
            )
        }else{
            Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}