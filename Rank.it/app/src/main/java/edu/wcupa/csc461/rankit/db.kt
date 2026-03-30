import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class db private constructor(context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("votes", Context.MODE_PRIVATE)
// manage db's singleton instance
    companion object {
        private var instance: db? = null

        fun getInstance(context: Context): db {
            if (instance == null) {
                instance = db(context.applicationContext)
            }
            return instance!!
        }
    }
// handle calls to create a new voting category. takes one argument for name
    fun createCategory(name: String) {
        val category = hashMapOf(
            "name" to name,
            "createdAt" to FieldValue.serverTimestamp()
        )
// creates new collection in database
        firestore.collection("categories")
            .add(category)
            .addOnSuccessListener { doc ->
                Log.d("TEST", "Category created: ${doc.id}")
            }
            .addOnFailureListener { Log.e("TEST", "Error", it) }
    }
// adds a new voting option to an existing collection in database
    fun addOption(categoryId: String, optionName: String) {
        val option = hashMapOf(
            "name" to optionName,
            "imageUrl" to "https://via.placeholder.com/150",
            "score" to 0
        )
// handles interaction with Firestore to add a new option
        firestore.collection("categories")
            .document(categoryId)
            .collection("options")
            .add(option)
            .addOnSuccessListener {
                Log.d("TEST", "Option added")
            }
            .addOnFailureListener { Log.e("TEST", "Error", it) }
    }
// manages votes placed on existing options
    fun vote(categoryId: String, optionId: String) {
        if (prefs.getBoolean(optionId, false)) {
            Log.d("TEST", "Already voted")
            return
        }

        firestore.collection("categories")
            .document(categoryId)
            .collection("options")
            .document(optionId)
            .update("score", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("TEST", "Vote recorded")
                prefs.edit().putBoolean(optionId, true).apply()
            }
            .addOnFailureListener { Log.e("TEST", "Vote failed", it) }
        }

        fun listen(categoryId: String) {
            firestore.collection("categories")
                .document(categoryId)
                .collection("options")
                }
}
