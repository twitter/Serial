# Publishing Serial

First update the version in the `README.md` and the `serialization/gradle.properties`.

To publish Serial, we first publish to Bintray, then from there we sync to Maven Central.

The publish command is:

    ./gradlew clean build bintrayUpload -PbintrayUser=<your_bintray_username> -PbintrayKey=<your_bintray_key> -PdryRun=false
