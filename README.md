# MaybeLibrary
Maybe Library is under development and NOT ready for use!

# Usage
## building without an IDE
For building this project without an IDE, run this command in terminal:

```bash
./gradlew assembleDebug
```
You will get a file in the path :
```
app/build/outputs/aar/app-debug.aar
```

The ```app-debug.aar``` is the 'maybe' library. You can use this ```aar``` file in other Android projects.


## building with an IDE
This project only supports Android Studio.

1. open the folder in Android Studio
2. open Run/Debug Configurations, Toolbar -> Run -> Edit Configurations...
3. add Gradle task, Gradle projects: ```app/build.gradle```,; Task: ```assembleDebug```
4. click Run button
