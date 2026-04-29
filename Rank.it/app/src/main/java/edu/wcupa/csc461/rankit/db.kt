import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Data classes returned when loading from Firestore
data class LoadedOption(val firestoreId: String, val name: String, val score: Int)
data class LoadedPoll(val firestoreId: String, val title: String, val filterCategory: String, val options: List<LoadedOption>)

class db private constructor(context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val prefs =
        context.getSharedPreferences(
            "votes", Context.MODE_PRIVATE
        )

    private val MAX_VOTES = 10
    private val KEY_REMAINING = "remaining_votes"
    private val KEY_RESET_TIME = "reset_time"
    private val RESET_INTERVAL_MS = 3 * 60 * 1000L
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

    fun resetVotesIfNeeded() {
        val now = System.currentTimeMillis()
        val lastReset = prefs.getLong(KEY_RESET_TIME, 0L)
        if (now - lastReset >= RESET_INTERVAL_MS) {
            prefs.edit()
                .putInt(KEY_REMAINING, MAX_VOTES)
                .putLong(KEY_RESET_TIME, now)
                .apply()
        }
    }

    fun getTimeUntilReset(): Long {
        val lastReset = prefs.getLong(KEY_RESET_TIME, 0L)
        return (RESET_INTERVAL_MS - (System.currentTimeMillis() - lastReset)).coerceAtLeast(0L)
    }

    fun getRemainingVotes(): Int {
        if (!prefs.contains(KEY_REMAINING)) {
            prefs.edit().putInt(KEY_REMAINING, MAX_VOTES).apply()
        }
        return prefs.getInt(KEY_REMAINING, MAX_VOTES)
    }
    // SECTION 1 : HANDLE WRITING TO DB
// handle calls to create a new poll. saves title and its filter category
    fun createCategory(name: String, filterCategory: String = "", onSuccess: ((String) -> Unit)? = null) {
        val data = hashMapOf(
            "name" to name,
            "filterCategory" to filterCategory,
            "createdAt" to FieldValue.serverTimestamp()
        )
// creates new document in categories collection
        firestore.collection("categories")
            .add(data)
            .addOnSuccessListener { doc ->
                Log.d("TEST", "Category created: ${doc.id}")
                onSuccess?.invoke(doc.id)
            }
            .addOnFailureListener { Log.e("TEST", "Error", it) }
    }
// saves a filter category label (Food, Sports, etc.) used for UI filtering
    fun createFilterCategory(name: String) {
        val data = hashMapOf(
            "name" to name,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("filterCategories")
            .add(data)
            .addOnSuccessListener { Log.d("TEST", "Filter category created: ${it.id}") }
            .addOnFailureListener { Log.e("TEST", "Error creating filter category", it) }
    }
// adds a new voting option to an existing poll in database
    fun addOption(categoryId: String, optionName: String, onSuccess: ((String) -> Unit)? = null) {
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
                Log.d("TEST", "Option added: ${it.id}")
                onSuccess?.invoke(it.id)
            }
            .addOnFailureListener { Log.e("TEST", "Error", it) }
    }
// manages votes placed on existing options
fun vote(categoryId: String, optionId: String, delta: Int = 1): Boolean {
    val remaining = getRemainingVotes()

    if (remaining <= 0) {
        Log.d("TEST", "No votes remaining")
        return false
    }

    if (categoryId.isBlank() || optionId.isBlank()) {
        Log.w("TEST", "Vote skipped: poll/option not yet synced to Firestore (categoryId='$categoryId', optionId='$optionId')")
        return false
    }

    firestore.collection("categories")
        .document(categoryId)
        .collection("options")
        .document(optionId)
        .update("score", FieldValue.increment(delta.toLong()))
        .addOnSuccessListener {
            Log.d("TEST", "Vote recorded")

            prefs.edit()
                .putInt(KEY_REMAINING, remaining - 1)
                .apply()
        }
        .addOnFailureListener { Log.e("TEST", "Vote failed", it) }

    return true
}

    fun listen(categoryId: String) {
        firestore.collection("categories")
            .document(categoryId)
            .collection("options")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TEST", "Listen failed", error)
                    return@addSnapshotListener
                }

                Log.d("TEST", "---- LIVE UPDATE ----")

                for (doc in snapshot!!) {
                    val name = doc.getString("name")
                    val score = doc.getLong("score")

                    Log.d("TEST", "$name -> $score")
                }
            }
    }

    // SECTION 2 : READING FROM DB

// loads all user-added filter category labels from Firestore
    fun loadFilterCategories(onResult: (List<String>) -> Unit) {
        firestore.collection("filterCategories")
            .get()
            .addOnSuccessListener { result ->
                val names = result.mapNotNull { it.getString("name") }
                onResult(names)
            }
            .addOnFailureListener {
                Log.e("TEST", "Error loading filter categories", it)
                onResult(emptyList())
            }
    }

// loads all polls and their options from Firestore, then calls onResult with the full list
    fun loadPolls(onResult: (List<LoadedPoll>) -> Unit) {
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { pollDocs ->
                if (pollDocs.isEmpty) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val polls = mutableListOf<LoadedPoll>()
                var remaining = pollDocs.size()

                for (pollDoc in pollDocs) {
                    val pollId = pollDoc.id
                    val title = pollDoc.getString("name")
                    if (title == null) {
                        remaining--
                        if (remaining == 0) onResult(polls)
                        continue
                    }
                    val filterCategory = pollDoc.getString("filterCategory") ?: ""

                    firestore.collection("categories")
                        .document(pollId)
                        .collection("options")
                        .get()
                        .addOnSuccessListener { optionDocs ->
                            val options = optionDocs.map { optDoc ->
                                LoadedOption(
                                    firestoreId = optDoc.id,
                                    name = optDoc.getString("name") ?: "",
                                    score = optDoc.getLong("score")?.toInt() ?: 0
                                )
                            }
                            polls.add(LoadedPoll(pollId, title, filterCategory, options))
                            remaining--
                            if (remaining == 0) onResult(polls)
                        }
                        .addOnFailureListener {
                            Log.e("TEST", "Error loading options for $pollId", it)
                            polls.add(LoadedPoll(pollId, title, filterCategory, emptyList()))
                            remaining--
                            if (remaining == 0) onResult(polls)
                        }
                }
            }
            .addOnFailureListener {
                Log.e("TEST", "Error loading polls", it)
                onResult(emptyList())
            }
    }

    fun getCategories() {
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val id = doc.id
                    val name = doc.getString("name")

                    Log.d("TEST", "Category: $name (id: $id)")
                }
            }
            .addOnFailureListener {
                Log.e("TEST", "Error getting categories", it)
            }
    }

    fun getOptions(categoryId: String) {
        firestore.collection("categories")
            .document(categoryId)
            .collection("options")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val id = doc.id
                    val name = doc.getString("name")
                    val score = doc.getLong("score")

                    Log.d("TEST", "Option: $name | Score: $score | id: $id")
                }
            }
            .addOnFailureListener {
                Log.e("TEST", "Error getting options", it)
            }
    }
}
