package com.twitter.serial.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.twitter.serial.stream.Serial;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @NotNull private static final String[] FIRST_NAMES =
            {"Amandeep", "César", "Ali", "Shoab", "Jingwei", "Max", "Mike", "Eric", "Alexandr"};
    @NotNull private static final String[] LAST_NAMES =
            {"Grewal", "Puerta", "Fauci", "Ahmed", "Hao", "Borghino", "Evans", "Frohnhoefer", "Naberezhnov"};

    @NotNull private static final String TAG = "MainActivity";
    @NotNull private static final String KEY_PERSON = "person";

    @NotNull final Serial serial = new ByteBufferSerial();

    private TextView fullNameView;
    private TextView ageView;

    private Person person;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fullNameView = findViewById(R.id.name);
        ageView = findViewById(R.id.age);
        findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPerson();
            }
        });

        if (savedInstanceState != null) {
            try {
                person = serial.fromByteArray(savedInstanceState.getByteArray(KEY_PERSON), Person.SERIALIZER);
                setTextViews();
            } catch (IOException | ClassNotFoundException e) {
                Log.d(TAG, "Person could not be deserialized", e);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putByteArray(KEY_PERSON, serial.toByteArray(person, Person.SERIALIZER));
        } catch (IOException e) {
            Log.d(TAG, "Person could not be serialized", e);
        }
    }

    private void setTextViews() {
        if (person != null) {
            fullNameView.setText(String.format("%s %s", person.firstName, person.lastName));
            ageView.setText(String.valueOf(person.age));
        }
    }

    private void createPerson() {
        person = new Person.Builder()
                .setFirstName(getName(FIRST_NAMES))
                .setLastName(getName(LAST_NAMES))
                .setAge((int) (20 + 60 * Math.random()))
                .build();

        setTextViews();
    }

    @NotNull
    private String getName(@NotNull String[] names) {
        return names[(int) (Math.random() * names.length)];
    }
}
