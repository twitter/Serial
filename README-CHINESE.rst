|badge1| |badge2| |badge3|

.. |badge1| image:: https://travis-ci.org/twitter/Serial.svg?branch=master
    :target: https://travis-ci.org/twitter/Serial

.. |badge2| image:: https://img.shields.io/maven-central/v/com.twitter.serial/serial.svg
    :target: https://repo1.maven.org/maven2/com/twitter/serial/serial/

.. |badge3| image:: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
    :target: https://raw.githubusercontent.com/twitter/Serial/master/LICENSE.txt

Twitter Serial
==============

English Doc : https://github.com/twitter/Serial/blob/master/README.rst

依赖
--------
从Maven Central上下载最新的依赖：

.. code-block:: java

  repositories {
    mavenCentral()
  }

  dependencies {
    implementation 'com.twitter.serial:serial:0.1.6'
  }
 
简介
--------

Serial是Twitter出品的高性能序列化方案，它力求帮助开发者实现高性能和高可控的序列化过程。

这个序列化框架提供了一个名叫Serializers的序列化器，开发者可以通过它来明确地定义对象的序列化过程。

Serial方案的主要优点如下：

- 相比起传统的反射序列化方案更加高效（没有使用反射）

  - 性能相比传统方案提升了3倍 （序列化的速度提升了5倍，反序列化提升了2.5倍）
  - 序列化生成的数据量（byte[]）大约是之前的1/5

- 开发者对于序列化过程的控制较强，可定义哪些object、field需要被序列化
- 有很强的debug能力，可以调试序列化的过程（详见：`调试`_）

**译者注：**

Serializer是本库中的关键类，这个类提供了序列化和反序列化的能力，序列化的定义和流程都是通过它来实现的。

基础结构
---------------

将一个对象序列化为byte[]：

.. code-block:: java

  final Serial serial = new ByteBufferSerial();
  final byte[] serializedData = serial.toByteArray(object, ExampleObject.SERIALIZER)

将对象从byte[]反序列化为object：

.. code-block:: java

  final ExampleObject object = serial.fromByteArray(serializedData, ExampleObject.SERIALIZER)

**译者注：**

目前库中默认提供的序列化实现类是ByteBufferSerial，它的产物是byte[]。使用者也可以自行更换实现类，不用拘泥于byte[]。


定义Serializer
--------------------

- 于之前的实现Serializable接口不同，这里需要给每个被序列化的对象单独定义一个Serializer
- Serializers中需要给每个field明确的定义write和read操作，对于有继承关系的序列化类，需要被递归的进行定义
- Serializers已经为使用者处理了空对象问题，就像read/writeString一样，记住不要使用原始的方法
- Serializers是无状态的，所以我们可以将其写为object的内部类，并通过 ``SERIALIZER`` 作为名称来访问它

对于大多数类，你可以建立一个继承自 ``ObjectSerializer`` 的子类，然后实现 ``serializeObject`` 方法和 ``deserializeObject`` 方法：

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

**译者注：**

这个内部类和 ``parcelable`` 中的 ``Parcelable.Creator`` 极为相似，都是按顺序对变量进行读写操作。为了方便理解，可以和Parcelable.Creator做下类比：

.. code-block:: java

  public static final Parcelable.Creator<Person> CREATOR = new Creator<Person>() {

      @Override
      public Person createFromParcel(Parcel source) {
          Person person = new Person();
          person.mName = source.readString();
          person.mSex = source.readString();
          person.mAge = source.readInt();
          return person;
      }

      //供反序列化本类数组时调用的方法
      @Override
      public Person[] newArray(int size) {
          return new Person[size];
      }
  };

对于那些通过builder模式构建的类或是有多个构造方法的类（详见：`更新Serializers`_ ），你可以使用 ``BuilderSerializer`` 来做序列化。

你只需要继承 ``BuilderSerializer`` ，并实现 ``createBuilder`` 方法（仅return当前class的builder即可）和 ``deserializeToBuilder`` 方法（在这个方法中可以得到builder对象，这里将那些反序列化完毕的参数重新设置给builder）

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

序列化工具方法
-----------------------------
- ``CoreSerializers`` and ``CollectionSerializers`` 提供了一些静态方法来方便collections，enums，comparators对象被序列化。

  - 比如，当我们序列化一个string列表的时候可以这么做:

    .. code-block:: java

      CollectionSerializers.getListSerializer(Serializers.STRING);

- 对于那些有基类的对象，你可以用 ``CoreSerializers`` 中的getBaseClassSerializer()，它会通过子类serializers构造出基类的serializer。

  - 举个例子，比如ClassA和ClassB都继承自ClassC。你想要将当前对象序列化为ClassC的类型，你可以在ClassC中通过子类的serializer方法来建立一个SERIALIZER。

    .. code-block:: java

      final Serializer<ClassC> SERIALIZER = CoreSerializers.getBaseClassSerializer(
            SerializableClass.create(ClassA.class, new ClassA.ClassASerializer()),
            SerializableClass.create(ClassB.class, new ClassB.ClassBSerializer()));

  .. 
  注意::
    这里必须new出ClassA和ClassB的serializer对象（new ClassA.ClassASerializer()、ClassB.ClassBSerializer()），而不是直接使用在ClassA和ClassB中定义的静态serializer。


更新Serializers
--------------------
如果你在新版本App中添加或删除了之前已经被序列化的对象的field，那么在反序列化老版本数据的时候可能会碰到一些问题。

下面有几种方案可以来处理这种情况：

``OptionalFieldException``
~~~~~~~~~~~~~~~~~~~~~~~~~~

当你在新版本的ExampleObject添加了一个新的字段，这时反序列化老版本ExampleObject就会出问题。Serializer默认会依次的读取所有的field，此时抛出 ``OptionalFieldException`` 异常。

``BuilderSerializer`` 已经为你处理好了 ``OptionalFieldExceptions`` 。当它捕获到这个异常时会终止序列化过程并忽略你这个新加的field，立刻返回一个没有这个field的对象。如果你使用的是普通的Serializer，那么你可以通过try-catch来处理这个问题。

- 举个例子：比如你想要给ExampleObject的最后增加一个叫 ``name`` 的字段（原先的ExampleObject仅有num和SubObject这两个字段）

  - 对于都是serializer类型的情况，只需简单的添加 ``.writeString(obj.name)`` 到 ``serializeObject`` 中即可
  - 对于BuilderSerializer，只需要在 ``deserializeToBuilder`` 的最后添加 ``.setName(input.readString())`` 即可
  - 对于普通的Serializer，你必须要修改像下面一样修改 ``deserializeObject`` 方法:

    .. code-block:: java

      @Override
      @NotNull
      protected ExampleObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber) throws IOException, ClassNotFoundException {
          final int num = input.readInt();
          final SubObject obj = input.readObject(SubObject.SERIALIZER);
          final String name;
          try {
              name = input.readString();
          } catch (OptionalFieldException e) {
              name = DEFAULT_NAME; // 老版本中没有这个字段，给它一个默认值
          }
          return new ExampleObject(num, obj, name);
      }

版本号
~~~~~~~~~~~~~~~
你可以给你的serializer添加一个版本号，这样当你在反序列化的过程中就可以通过这个版本号来进行复杂的处理了。添加版本号十分简单，只需要在 ``SERIALIZER`` 的构造函数中传入数字即可。

- 我们来修改一下上面的代码，通过版本号这个字段来处理新老版本的问题：

  .. code-block:: java

    final Serializer<ExampleObject> SERIALIZER = new ExampleObjectSerializer(1);
    ...

    @Override
    @NotNull
    protected ExampleObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber) throws IOException, ClassNotFoundException {
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

如果你删除了序列化对象中部的某个field，比如ExampleObject中间的 ``SubObject`` 。你可能需要用 ``SerializationUtils.skipObject()`` 来终止整个反序列化过程。如果你已经把 ``SubObject`` 完全移除了，那么可以不用保留 ``SubObject`` 中的serializer对象。

- 比方说，你可能在新版本中删除了 ``SubObject`` ，而老版本的数据中含有这个对象，你可以进行下面的处理:

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

另一个方法是调用input.peekType()。这个方法可以让你在读取object对象前进行下一个参数的类型检查，它提供了一个除判断版本号之外的解决新老数据的问题的方案。当你不愿意升级版本号或是不愿意擦除数据库的时候，这个方法会十分有用。

需要注意的是：这个方法仅仅适用于两个对象类型不同的情况。因为这里obj类型是 ``SubObject`` ，name类型是 ``String`` ，所以可以进行如下处理：

.. code-block:: java

    @Override
    @NotNull
    protected ExampleObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber) throws IOException, ClassNotFoundException {
        final int num = input.readInt();
        if (input.peekType() == SerializerDefs.TYPE_START_OBJECT) {
            SerializationUtils.skipObject();
            name = DEFAULT_NAME;
        } else {
            name = input.readString();
        }
        return new ExampleObject(num, name);
    }

简单参数的序列化
-----------------
像 ``Integer`` 、 ``String`` 、 ``Size``、``Rect`` 等对象本身就十分简单，所以无需进行版本控制。而使用 ``ObjectSerializer`` 会让这些对象添加2-3字节的信息。所以，当不需要版本控制的时候，使用 ``ValueSerializer`` 是一个最佳选择：

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

这仅仅是 ``ObjectSerializer`` 的简单版本，它处理了 ``null`` 的情况。否则，只需将值写入到流中。

.. 
说明::

让值为null的时候 ``ValueSerializer`` 会将 ``null`` 写入到流中。这就导致第一个通过 ``serializeValue`` 写入到流中的参数（field）不能为 ``null`` ，否则就会引起歧义。在这种情况下， ``ValueSerializer`` 会认为这是一个错误，并且抛出异常。

..
警告:: 

  ValueSerializers“仅仅”能被用于对象格式已知的情况下，也就是说它不能向后兼容。

调试
---------
``serial`` 同样也提供了方便debug的相关方法：

- ``dumpSerializedData`` 会根据序列化后的byte[]数据产生string类型的log
- ``validateSerializedData`` 确保了序列化后的对象有有效的结构(比如每个对象都有开头和结尾)

Serial的异常信息中会包含很多序列化失败的原因，比如期望的类型和实际类型不匹配这种常见错误。
