package com.twitter.serial.sample;

import com.twitter.serial.serializer.BuilderSerializer;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.serializer.Serializer;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Person {
    public static final Serializer<Person> SERIALIZER = new PersonSerializer();

    @NotNull public final String firstName;
    @NotNull public final String lastName;
    public final int age;

    private Person(@NotNull Builder builder) {
        assert builder.firstName != null;
        assert builder.lastName != null;

        firstName = builder.firstName;
        lastName = builder.lastName;
        age = builder.age;
    }

    public static final class Builder implements com.twitter.serial.object.Builder<Person> {
        @Nullable private String firstName;
        @Nullable private String lastName;
        private int age;

        @NotNull
        public Builder setFirstName(@Nullable String firstName) {
            this.firstName = firstName;
            return this;
        }

        @NotNull
        public Builder setLastName(@Nullable String lastName) {
            this.lastName = lastName;
            return this;
        }

        @NotNull
        public Builder setAge(int age) {
            this.age = age;
            return this;
        }

        @NotNull
        @Override
        public Person build() {
            return new Person(this);
        }
    }

    private static final class PersonSerializer extends BuilderSerializer<Person, Builder> {
        @Override
        protected void serializeObject(@NotNull SerializationContext context, @NotNull SerializerOutput output,
                @NotNull Person person) throws IOException {
            output.writeString(person.firstName);
            output.writeString(person.lastName);
            output.writeInt(person.age);
        }

        @NotNull
        @Override
        protected Builder createBuilder() {
            return new Builder();
        }

        @Override
        protected void deserializeToBuilder(@NotNull SerializationContext context, @NotNull SerializerInput input,
                @NotNull Builder builder, int versionNumber) throws IOException, ClassNotFoundException {
            builder.setFirstName(input.readString());
            builder.setLastName(input.readString());
            builder.setAge(input.readInt());
        }
    }
}
