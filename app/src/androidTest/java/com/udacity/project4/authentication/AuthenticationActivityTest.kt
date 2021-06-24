package com.udacity.project4.authentication

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.udacity.project4.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class AuthenticationActivityTest {
    @Test
    fun test_IsActivityInView(){
        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)

        onView(withId(R.id.auth_main)).check(matches(isDisplayed()))
    }
}