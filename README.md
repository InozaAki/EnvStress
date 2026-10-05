# EnvStress - Voice and Enviromental Stress Detection

![Android Studio Badge](https://img.shields.io/badge/Android%20Studio-3DDC84?logo=androidstudio&logoColor=fff&style=for-the-badge)
![Java Badge](https://img.shields.io/badge/Java-ED8B00?logo=java&logoColor=fff&style=for-the-badge)
![Gradle Badge](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=fff&style=for-the-badge)
![License Badge](https://img.shields.io/badge/License-MIT-000000?style=for-the-badge)

---

Inspired by the paper [StressSense: Detecting stress in unconstrained acoustic environments using smartphones](https://dl.acm.org/doi/10.1145/2370216.2370270), this project aims to detect stress levels in individuals based on their voice and environmental factors using a native Android application.

The application leverages in algorithms such as the FFT and the MFCC to analyze audio signals and extract relevant features that can indicate stress levels. The app also considers environmental factors such as noise levels and ambient conditions to provide a comprehensive assessment of stress.

Also using the [RAVDESS Emotional Speech Audio](https://doi.org/10.34740/KAGGLE/DSV/256618) to fine-tune the stress detection model and improve its accuracy in real-world scenarios.


Check the [Documentation](docs/) for more information.


## Enviroment Setup

### Prerequisites

**Nice to have (Don't worry if you don't have them):**
- **Android Studio**: The official IDE for Android development. You can download it from [here](https://developer.android.com/studio).

Check [Compiling and Running](#compiling-and-running) section for more information on how to run the project with or without Android Studio.

**Required:**

- **Java Development Kit (JDK)**: Ensure you have JDK installed. You can download it from [here](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html).
- **Gradle**: Gradle is included with Android Studio, but you can also install it separately if needed. More information can be found [here](https://gradle.org/install/).

If you don't know want to use the Oracle SDK, **you can use OpenJDK.** You can download it from [here](https://openjdk.org/install/). **Or either way use SDKMAN!** to install the JDK. You can find more information about SDKMAN! [here](https://sdkman.io/install).

Either case, make sure the **minimun version of the JDK is 17**. You can check your JDK version by running the following command in your terminal:

```bash
java -version
```

## Compiling and Running

I'll be using both Android Studio and Gradle to compile and run the projects. You can choose either method based on your preference and setup.

### With Android Studio

1. **Clone the repository to your local machine using Git:**

   ```bash
   git clone https://github.com/InozaAki/EnvStress.git
   ```

2. **Open Android Studio and select "Open an existing project." Navigate to the cloned repository and open it.**

3. **Select the desired Hands-on project from the folder structure in Android Studio.**

4. **Click on the "Run" button (green play icon) in Android Studio to build and run the project on an emulator or a connected Android device.**

### Without Android Studio

1. **Clone the repository to your local machine using Git:**
   ```bash
   git clone https://github.com/InozaAki/EnvStress.git
   ```
2. **Navigate to the desired Hands-on project directory in the terminal.**
3. **Enable developer mode and USB debugging on your Android device.** (Used to save resources and time, but you can also use an emulator if you prefer.)
4. **Use Gradle to build and run the project on your connected Android device. You can use the following command:**

   ```bash
   ./gradlew installDebug
   ```

5. **After the build is complete, you can launch the app on your Android device. Since Gradle handles the installation, you can find the app in your device's app drawer.**

**To make a clean build, you can use the following command:**

```bash
./gradlew clean installDebug
```

If you encounter any issues during the setup or running of the projects, please refer to the official documentation of Android Studio, Java, and Gradle for troubleshooting. Or you can reach out to me for assistance.


## Authors

**Axel Antonio Espinosa Espinoza**
  - GitHub: [@InozaAki](https://github.com/InozaAki)
  - Email: [axelespinoza887@gmail.com](mailto:axelespinoza887@gmail.com)


**Bryan Julian Mendez Ambriz**
  - GitHub: [@Bjma1507](https://github.com/Bjma1507)
  - Email: [bryan.mendez6956@alumnos.udg.mx](mailto:bryan.mendez6956@alumnos.udg.mx)


## References 

[1] lu, Hong & Frauendorfer, Denise & Rabbi, Mashfiqui & Mast Marianne & Chittaranjan, Gokul & Campbell, Andrew & Gatica-Perez, Daniel & Choudhury, Tanzeem. (2012). StressSense: Detecting stress in unconstrained acoustic environments using smartphones. UbiComp'12 - Proceedings of the 2012 ACM Conference on Ubiquitous Computing. 351-360. 10.1145/2370216.2370270.

[2] Steven R. Livingstone, and Frank A. Russo. (2019). RAVDESS Emotional speech audio [Dataset]. Kaggle. https://doi.org/10.34740/KAGGLE/DSV/256618

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details