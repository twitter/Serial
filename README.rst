Twitter Cereal
==============

Overview
--------
Twitter Cereal is a custom serialization implementation that's intended to improve performance and increase
developer visibility into and control over an object's serialization.

This framework uses Serializers to explicitly define how a class should be serialized. Some of the major advantages of this
approach include:

- more efficient serialization avoiding reflection - preliminary metrics for a large object showed

  - more than 3x faster for roundtrip serialization (5x faster to serialize, 2.5x to deserialize)
  - around 5x smaller in byte array size

- greater control over what's serialized for an object - all serialization is defined explicitly
- better debugging capabilities (see :ref:`serialization-debugging`)


Basic Structure
---------------
- Instead of implementing Serializable, you must define a serializer for every object that needs to be serialized
- read/writeObject no longer exist in the same way, you must pass in the serializer for the class type or call
  serialize/deserialize explicitly using that object's serializer
- Serializers explicitly write and read each field of the object by using read/write for primitives or recursively
  calling serializers for other objects
- Serializers handle null objects for you, as does read/writeString; primitive read/write methods do not
- Serializers are stateless, so they are written as static inner classes of the object and accessed as a static
  instance variable ``SERIALIZER``

To serialize an object to a byte array, use:

.. code-block:: java

  final byte[] serializedData = SerializationUtils.toByteArray(object, ExampleObject.SERIALIZER)

To deserialize from a byte array back to an object, use:

.. code-block:: java

  final ExampleObject object = SerializationUtils.fromByteArray(ExampleObject.SERIALIZER)

Defining Serializers
--------------------
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

      private static final ExampleObjectSerializer extends ObjectSerializer<ExampleObject> {
          @Override
          protected void serializeObject(@NotNull SerializerOutput output,
                  @NotNull ExampleObject object) throws IOException {
              output.writeInt(object.num)
                  .writeObject(object.obj, SubObject.SERIALIZER);
          }

          @Override
          @NotNull
          protected ExampleObject deserializeObject(@NotNull SerializerInput input,
                  int versionNumber) throws IOException, ClassNotFoundException {
              final int num = input.readInt();
              final SubObject obj = input.readObject(SubObject.SERIALIZER);
              return new ExampleObject(num, obj);
          }
      }
  }

For classes that are constructed using builders, or have optional fields added (see :ref:`updating-serializers`), you
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

      public static Builder extends ModelBuilder<ExampleObject> {
          ...
      }

      private static final ExampleObjectSerializer extends BuilderSerializer<ExampleObject, Builder> {
          @Override
          @NotNull
          protected Builder createBuilder() {
              return new Builder();
          }

          @Override
          protected void serializeObject(@NotNull SerializerOutput output,
                  @NotNull ExampleObject object) throws IOException {
              output.writeInt(object.num)
                  .writeObject(object.obj, SubObject.SERIALIZER);
          }

           @Override
          protected void deserializeToBuilder(@NotNull SerializerInput input,
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
      protected ExampleObject deserializeObject(@NotNull SerializerInput input,
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
in the deserialize method you can specify what to do differenlty for previous versions.

- To change the above example to use version numbers, do the following:

  .. code-block:: java

    final Serializer<ExampleObject> SERIALIZER = new ExampleObjectSerializer(1);
    ...

    @Override
    @NotNull
    protected ExampleObject deserializeObject(@NotNull SerializerInput input, int versionNumber)
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
    protected ExampleObject deserializeObject(@NotNull SerializerInput input, int versionNumber)
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

Value Serializers
-----------------
Some objects are so simple that do not require support for versioning: ``Integer``, ``String``, ``Size``, ``Rect``...
Using an ``ObjectSerializer`` with these objects adds an envelope of 2-3 bytes around the serialized data, which can
add significant overhead. When versioning is not required, ``ValueSerializer`` is a better choice:

.. code-block:: java

  public static final Serializer<Boolean> BOOLEAN = new ValueSerializer<Boolean>() {
      @Override
      protected void serializeValue(@NotNull SerializerOutput output, @NotNull Boolean object) throws IOException {
          output.writeBoolean(object);
      }

      @NotNull
      @Override
      protected Boolean deserializeValue(@NotNull SerializerInput input) throws IOException {
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

.. _serialization-debugging:

Saved State Serialization
-------------------------

We can use Serializers to serialize and save state before an Activity is destroyed, and then deserialize and load
state when the Activity is recreated. We often want to do this in Activities, Fragments, and Views, etc. Using the
``@AutoSaveState`` and ``@SaveState`` annotation, we can automate most of this process.

Usage
~~~~~
- Annotate the class with ``@AutoSaveState``.

  .. code-block:: java

    @AutoSaveState
    public class MyView {
    }

- Annotate the fields to save and restore class with ``@SaveState``. These must be at least package private
  (not private), as they need to be visible to the generated class, and must be non-final as they are overwritten
  on state restoration.

  .. code-block:: java

    @SaveState int position;
    @SaveState float score;

- Create an instance of the automatically generated ``<YourClass>SavedState`` class, and pass in an instance of
  ``YourClass``. This constructor copies all fields annotated with ``@SaveState`` into the internal state of the
  new ``<YourClass>SavedState`` object. This class is a ``Parcelable`` (and a ``StateSaver<T>``), so you can now use
  this class as you would any ``Parcelable``.

  .. code-block:: java

    new MyViewSavedState<>(this)

- Call the ``restoreState`` method of a ``<YourClass>SavedState`` object, and pass in an instance of ``YourClass``.
  This method restores all fields annotated with ``@SaveState``, by copying to them from its internal state.

  .. code-block:: java

    myViewSavedState.restoreState(this);

Full example using Dagger injection
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: java

  @AutoSaveState
  public class MyViewBinder implements HasSavedState<StateSaver<MyViewBinder>> {
      public static final String SAVED_STATE_ID = "STATE_MY_VIEW_BINDER";

      @SaveState int score;

      public MyViewBinder(@NotNull StateSaver<MyViewBinder> savedState) {
          savedState.restoreState(this);
          // this.score now has its old value
      }

      ...

      @Nullable
      @Override
      public StateSaver<MyViewBinder> getSavedState() {
          return new MyViewBinderSavedState<>(this);
      }

      @NotNull
      @Override
       public String getId() {
          return SAVED_STATE_ID;
      }

      ...
  }

You would then create an instance of this class using the ``SavedStates`` you get from the ``onCreateViewComponent``
method from the ``Injected*``' class:

.. code-block:: java

  StateSaver<MyViewBinder> savedState = savedStates.<StateSaver<MyViewBinder>>getSavedState(MyViewBinder.SAVED_STATE_ID);
  MyViewBinder binder = new MyViewBinder(savedState);

You could also inject the StateSaver like so:

.. code-block:: java

    @Provides
    @Scoped(InjectionScope.VIEW)
    @NotNull
    StateSaver<MyViewBinder> provideMyViewBinderState() {
        return mSavedStates.<StateSaver<MyViewBinder>>getSavedState(MyViewBinder.SAVED_STATE_ID);
    }

Full example with standard Android Fragments
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: java

  @AutoSaveState
  public class MyFragment extends AbsFragment {

      @SaveState int score;
      @SaveState List<Long> userIds;

      @Override
      public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          MyFragmentSavedState.restoreFromBundle(this, savedInstanceState);
      }

      @Override
      public void onSaveInstanceState(Bundle outState) {
          super.onSaveInstanceState(outState);
          new MyFragmentSavedState<>(this).saveToBundle(outState);
      }

      ...
  }

Debugging
---------
``SerializationUtils`` also contains methods to help with debugging:

- ``dumpSerializedData`` will create a string log of the data in the serialized byte array
- ``validateSerializedData`` ensures that the serialized object has a valid structure (e.g. every object start header
  has a matching end header)

Exceptions now contain more information about the serialization failure, specifically information about the expected
type to be deserialized and the type that was found, based on headers written for each value.

Android Considerations
----------------------
.. note::
  There is an auto-generator for Android Studio available as a plugin. With this
  you can generate a serializer from within a class by using Code -> Generate in Android Studio. You can also do
  this when you add new fields to serialize.

