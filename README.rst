|badge1| |badge2| |badge3|

.. |badge1| image:: https://travis-ci.org/twitter/Serial.svg?branch=master
    :target: https://travis-ci.org/twitter/Serial

.. |badge2| image:: https://img.shields.io/maven-central/v/com.twitter.serial/serial.svg
    :target: https://repo1.maven.org/maven2/com/twitter/serial/serial/

.. |badge3| image:: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
    :target: https://raw.githubusercontent.com/twitter/Serial/master/LICENSE.txt

Twitter Serial
==============

Download
--------
Grab the latest version via Gradle from Maven Central:

.. code-block:: java

  repositories {
    mavenCentral()
  }

  dependencies {
    compile 'com.twitter.serial:serial:0.1.6'
  }

Overview
--------
Twitter Serial is a custom serialization implementation that's intended to improve performance and increase
developer visibility into and control over an object's serialization.

This framework uses Serializers to explicitly define how a class should be serialized. Some of the major advantages of this
approach include:

- more efficient serialization avoiding reflection - preliminary metrics for a large object showed

  - more than 3x faster for roundtrip serialization (5x faster to serialize, 2.5x to deserialize)
  - around 5x smaller in byte array size

- greater control over what's serialized for an object - all serialization is defined explicitly
- better debugging capabilities (see `debugging`_)


Basic Structure
---------------

To serialize an object to a byte array, use:

.. code-block:: java

  final Serial serial = new ByteBufferSerial();
  final byte[] serializedData = serial.toByteArray(object, ExampleObject.SERIALIZER)

To deserialize from a byte array back to an object, use:

.. code-block:: java

  final ExampleObject object = serial.fromByteArray(ExampleObject.SERIALIZER)

Defining Serializers
--------------------
- Instead of implementing Serializable, define a serializer for every object that needs to be serialized
- Serializers explicitly write and read each field of the object by using read/write for primitives or recursively
  calling serializers for other objects
- Serializers handle null objects for you, as does read/writeString; primitive read/write methods do not
- Serializers are stateless, so they are written as static inner classes of the object and accessed as a static
  instance variable ``SERIALIZER``

For most classes, you can create a subclass of ``ObjectSerializer`` and implement ``serializeObject`` and
``deserializeObject``

.. code-block:: java

  public static class ExampleObject {
      public static final ObjectSerializer<ExampleObject> SERIALIZER = new ExampleObjectSerializer();

      public final int num;
      public final SubObject obj;

      public ExampleObject(int num, @NotNull SubObject obj) {
          this.num = num;
          this.obj = obj;
      }

      ...

      private static final class ExampleObjectSerializer extends ObjectSerializer<ExampleObject> {
          @Override
          protected void serializeObject(@NotNull SerializationContext context, @NotNull SerializerOutput output,
                  @NotNull ExampleObject object) throws IOException {
              output
                  .writeInt(object.num) // first field
                  .writeObject(object.obj, SubObject.SERIALIZER); // second field
          }

          @Override
          @NotNull
          protected ExampleObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input,
                  int versionNumber) throws IOException, ClassNotFoundException {
              final int num = input.readInt(); // first field
              final SubObject obj = input.readObject(SubObject.SERIALIZER); // second field
              return new ExampleObject(num, obj);
          }
      }
  }

For classes that are constructed using builders, or have optional fields added (see `updating-serializers`_), you
can use a ``BuilderSerializer``, in which you implement the methods ``createBuilder`` (which just returns a new builder
object for that class) and ``deserializeToBuilder`` (where you populate the builder with the deserialized fields)

.. code-block:: java

  public static class ExampleObject {
      ...

      public ExampleObject(@NotNull Builder builder) {
          this.num = builder.mNum;
          this.obj = builder.mObj;
      }

      ...

      public static class Builder extends ModelBuilder<ExampleObject> {
          ...
      }

      private static final class ExampleObjectSerializer extends BuilderSerializer<ExampleObject, Builder> {
          @Override
          @NotNull
          protected Builder createBuilder() {
              return new Builder();
          }

          @Override
          protected void serializeObject(@NotNull SerializationContext context, @NotNull SerializerOutput output,
                  @NotNull ExampleObject object) throws IOException {
              output.writeInt(object.num)
                  .writeObject(object.obj, SubObject.SERIALIZER);
          }

           @Override
          protected void deserializeToBuilder(@NotNull SerializationContext context, @NotNull SerializerInput input,
                  @NotNull Builder builder, int versionNumber) throws IOException, ClassNotFoundException {
              builder.setNum(input.readInt())
                  .setObj(input.readObject(SubObject.SERIALIZER));
          }
      }
  }

Serialization Utility Methods
-----------------------------
- ``CoreSerializers`` and ``CollectionSerializers`` contain serializers for boxed primitives and have helper methods
  to serialize objects like collections, enums and comparators.

  - For example, to serialize a list of Strings, you can use:

    .. code-block:: java

      CollectionSerializers.getListSerializer(Serializers.STRING);

- In order to serialize an object as its base class, you can construct a base class serializer from the subclass's
  serializers using the getBaseClassSerializer in ``Serializers``

  - For example, if you have ClassB and ClassC that both extend ClassA, and you want to serialize the objects as
    ClassA objects, you can create a serializer in ClassA using the serializers of the subclasses:

    .. code-block:: java

      final Serializer<ClassC> SERIALIZER = Serializers.getBaseClassSerializer(
          SerializableClass.create(ClassA.class, new ClassA.ClassASerializer()),
          SerializableClass.create(ClassB.class, new ClassB.ClassBSerializer()));

  .. note::
    You must create new instances of ClassA and B serializers rather than using the static object defined in
    those classes. Since ClassC is initialized as part of its subclasses, using static objects of its subclasses
    in its initialization will create a cyclic dependency that will likely lead to a cryptic NPE.

.. _updating-serializers:

Updating Serializers
--------------------
If you add or remove a field for an object that's being stored as serialized data, there are a few ways to handle it:

``OptionalFieldException``
~~~~~~~~~~~~~~~~~~~~~~~~~~
If you add a field to the end of an object, your new serializer will reach the end of an old object when trying to
read the new field, which will cause it to throw an ``OptionalFieldException``.

``BuilderSerializer`` handles ``OptionalFieldExceptions`` for you by just ignoring that field in the builder,
stopping deserialization, and building the rest of the object as is. If you're using a regular Serializer instead,
you can explicitly catch the OptionalFieldException and set the remaining field(s) to default values as appropriate.

- Say, for example, you wanted to add a String 'name' to the end of the ExampleObject above

  - For both serializer types, you could simply add ``.writeString(obj.name)`` to ``serializeObject``
  - For the BuilderSerializer, to deserialize you would add ``.setName(input.readString())`` to the end of
    ``deserializeToBuilder``. In the case where an older object without the name field is being deserialized, an
    ``OptionalFieldException`` would be thrown and caught when reading the String, causing the object to be built
    as is without the name field explicitly set.
  - For the regular Serializer, you would change ``deserializeObject`` as follows:

    .. code-block:: java

      @Override
      @NotNull
      protected ExampleObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input,
              int versionNumber) throws IOException, ClassNotFoundException {
          final int num = input.readInt();
          final SubObject obj = input.readObject(SubObject.SERIALIZER);
          final String name;
          try {
              name = input.readString();
          } catch (OptionalFieldException e) {
              name = DEFAULT_NAME;
          }
          return new ExampleObject(num, obj, name);
      }

Version numbers
~~~~~~~~~~~~~~~
Another option is to increase the version number of the serializer, and define the deserialization behavior for
older versions. To do this, pass the version number into the constructor of the ``SERIALIZER`` object, and then
in the deserialize method you can specify what to do differently for previous versions.

- To change the above example to use version numbers, do the following:

  .. code-block:: java

    final Serializer<ExampleObject> SERIALIZER = new ExampleObjectSerializer(1);
    ...

    @Override
    @NotNull
    protected ExampleObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
            throws IOException, ClassNotFoundException {
        final int num = input.readInt();
        final SubObject obj = input.readObject(SubObject.SERIALIZER);
        final String name;
        if (versionNumber < 1) {
            name = DEFAULT_NAME;
        } else {
            name = input.readString();
        }
        return new ExampleObject(num, obj, name);
    }

If you remove a field from the middle of an object, you need to ignore the whole object during deserialization by
using the ``skipObject`` method in ``SerializationUtils``. This way you don't need to keep the serializer if you
are removing the object all together.

- Say in the above example you also wanted to remove the obj field and delete ``SubObject``:

  .. code-block:: java

    @Override
    @NotNull
    protected ExampleObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
            throws IOException, ClassNotFoundException {
        final int num = input.readInt();
        if (versionNumber < 1) {
            SerializationUtils.skipObject()
            name = DEFAULT_NAME;
        } else {
            name = input.readString();
        }
        return new ExampleObject(num, name);
    }

Another option is to call input.peekType(), which allows you to check the type of the next field before reading the object.
This is especially helpful if you hadn't updated the version before making a change and don't want to wipe the database,
since it allows you to differentiate between the two versions without a version number. Note that this only works if the
two types are different.

.. code-block:: java

    @Override
    @NotNull
    protected ExampleObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
            throws IOException, ClassNotFoundException {
        final int num = input.readInt();
        if (input.peekType() == SerializerDefs.TYPE_START_OBJECT) {
            SerializationUtils.skipObject();
            name = DEFAULT_NAME;
        } else {
            name = input.readString();
        }
        return new ExampleObject(num, name);
    }

Value Serializers
-----------------
Some objects are so simple that do not require support for versioning: ``Integer``, ``String``, ``Size``, ``Rect``...
Using an ``ObjectSerializer`` with these objects adds an envelope of 2-3 bytes around the serialized data, which can
add significant overhead. When versioning is not required, ``ValueSerializer`` is a better choice:

.. code-block:: java

  public static final Serializer<Boolean> BOOLEAN = new ValueSerializer<Boolean>() {
      @Override
      protected void serializeValue(@NotNull SerializationContext context, @NotNull SerializerOutput output, @NotNull Boolean object) throws IOException {
          output.writeBoolean(object);
      }

      @NotNull
      @Override
      protected Boolean deserializeValue(@NotNull SerializationContext context, @NotNull SerializerInput input) throws IOException {
          return input.readBoolean();
      }
  };

This is just a simpler version of ``ObjectSerializer`` that handles ``null``, otherwise, just writes the values into
the stream.

.. note::
  ``ValueSerializer`` writes ``null`` to the stream when given a ``null`` value. As a result, the first field written
  into the stream by ``serializeValue`` can't be ``null``, since it would be ambiguous. ``ValueSerializer`` detects
  this as an error and throws an exception.

.. caution::
  Value serializers should *only* be used when their format is known to be fixed, since they do not support any form
  of backwards compatibility.

Debugging
---------
``serial`` also contains methods to help with debugging:

- ``dumpSerializedData`` will create a string log of the data in the serialized byte array
- ``validateSerializedData`` ensures that the serialized object has a valid structure (e.g. every object start header
  has a matching end header)

Exceptions now contain more information about the serialization failure, specifically information about the expected
type to be deserialized and the type that was found, based on headers written for each value.
