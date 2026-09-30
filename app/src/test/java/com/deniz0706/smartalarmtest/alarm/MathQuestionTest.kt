package com.deniz0706.smartalarmtest.alarm

import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Test

class MathQuestionTest {
    @Test fun beginnerQuestionsNeverHaveNegativeAnswersAndUseAllOperators() {
        val questions = List(300) { MathQuestion.beginner(Random(it)) }
        assertTrue(questions.all { it.answer >= 0 })
        assertTrue(questions.any { "+" in it.prompt })
        assertTrue(questions.any { "−" in it.prompt })
        assertTrue(questions.any { "×" in it.prompt })
    }
}
