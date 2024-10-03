 package com.codinghits.firebase
 import android.os.Bundle
 import android.view.View
 import android.widget.Button
 import android.widget.EditText
 import android.widget.TextView
 import androidx.appcompat.app.AppCompatActivity
 import com.google.firebase.firestore.FirebaseFirestore
 import com.google.firebase.firestore.ktx.firestore
 import com.google.firebase.ktx.Firebase
 import kotlin.random.Random

 class MainActivity : AppCompatActivity() {

     private lateinit var questionInput: EditText
     private lateinit var answerInput: EditText
     private lateinit var addFlashcardButton: Button
     private lateinit var startQuizButton: Button
     private lateinit var scoreTextView: TextView

     private val db: FirebaseFirestore by lazy { Firebase.firestore }
     private var score = 0
     private var currentFlashcards: List<Flashcard> = emptyList()
     private var currentIndex = 0

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)

         questionInput = findViewById(R.id.questionInput)
         answerInput = findViewById(R.id.answerInput)
         addFlashcardButton = findViewById(R.id.addFlashcardButton)
         startQuizButton = findViewById(R.id.startQuizButton)
         scoreTextView = findViewById(R.id.scoreTextView)

         addFlashcardButton.setOnClickListener { addFlashcard() }
         startQuizButton.setOnClickListener { startQuiz() }
     }

     private fun addFlashcard() {
         val question = questionInput.text.toString().trim()
         val answer = answerInput.text.toString().trim()

         if (question.isNotEmpty() && answer.isNotEmpty()) {
             val flashcard = Flashcard(question, answer)
             db.collection("flashcards")
                 .add(flashcard)
                 .addOnSuccessListener {
                     questionInput.text.clear()
                     answerInput.text.clear()
                 }
         }
     }

     private fun startQuiz() {
         db.collection("flashcards").get()
             .addOnSuccessListener { result ->
                 currentFlashcards = result.map { document ->
                     document.toObject(Flashcard::class.java)
                 }
                 score = 0
                 currentIndex = 0
                 scoreTextView.text = "Score: $score"
                 if (currentFlashcards.isNotEmpty()) {
                     showQuestion()
                 }
             }
     }

     private fun showQuestion() {
         if (currentIndex < currentFlashcards.size) {
             val flashcard = currentFlashcards[currentIndex]
             // Display question using an AlertDialog
             val builder = android.app.AlertDialog.Builder(this)
             builder.setTitle(flashcard.question)

             val answerInputDialog = EditText(this)
             builder.setView(answerInputDialog)

             builder.setPositiveButton("Submit") { _, _ ->
                 checkAnswer(answerInputDialog.text.toString(), flashcard.answer)
             }
             builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
             builder.show()
         } else {
             scoreTextView.text = "Final Score: $score"
         }
     }

     private fun checkAnswer(userAnswer: String, correctAnswer: String) {
         if (userAnswer.trim().equals(correctAnswer, ignoreCase = true)) {
             score++
         }
         currentIndex++
         showQuestion()
     }
 }

 data class Flashcard(val question: String = "", val answer: String = "")
