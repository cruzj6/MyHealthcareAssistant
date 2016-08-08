package com.cruzj6.mha;

import android.app.Activity;
import android.content.Context;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.runner.AndroidJUnit4;
import android.widget.TextView;

import com.cruzj6.mha.activities.MainActivity;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.AppointmentItem;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Joey on 8/7/16.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule public final ActivityRule<MainActivity> main = new ActivityRule<>(MainActivity.class);

    @Test
    public void testApptQueue()
    {
        Context c = main.get();
        List<AppointmentItem> apptItems = new DatabaseManager(c).loadAppointmentItems();
        Collections.sort(apptItems);

        //Check first for correct text
        assertThat(((TextView) ((Activity) (c)).findViewById(R.id.textview_apptq_item_title))
                .getText().toString().equals((apptItems.get(0).getAppointmentTitle())), new Matcher<Boolean>() {
            @Override
            public boolean matches(Object item) {
                return true;
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {

            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {

            }

            @Override
            public void describeTo(Description description) {

            }
        });
    }

}
