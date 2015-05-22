# MaybeLibrary
Maybe Library, under develop, NOT ready for use!

# Usage
## build without IDE
You can build this project without IDE. Run this command in terminal:

```bash
./gradlew assembleDebug
```
Then you can get a file:
```
app/build/outputs/aar/app-debug.aar
```

The ```app-debug.aar``` is maybe library. You can use this ```aar``` file in other Android project.


## build with IDE
This project only support Android Studio.

1. open the folder in Android Studio
2. open Run/Debug Configurations, Toolbar -> Run -> Edit Configurations...
3. add Gradle task, Gradle projects: ```app/build.gradle```,; Task: ```assembleDebug```
4. click Run button
