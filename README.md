# AI-Powered Mobile App for Nuclear Cataract Detection

This Android mobile application is part of the research described in the accompanying scientific article.  
Author: Alicja Ignatowicz
Supervision: Tomasz Marciniak

It implements a deep learning–based classifier to support the assessment of cataracts using ocular images.

>The study utilizes the [The Nuclear Cataract Database for Biomedical and Machine Learning Applications](https://data.mendeley.com/datasets/6wv33nbcvv/2) classified using LOCS III and proposes a mobile inference pipeline that incorporates pupil marking, preprocessing, and classification into one user-friendly tool.

<b> Authors </b> : Alicja Ignaczowicz, Tomasz Marciniak, Elżbieta Marciniak

## Dataset 
To achieve the highest possible accuracy of neural network models, images classified from 1 to 6 the NC grade, consistent with the LOCS III scale, were used for training and evaluation. Due to insufficient cataract visibility, image blurriness, or poor illumination of the pathological area, a specialist selected images that accurately represented the degree of advancement.

<b>Number of images from the Nuclear Cataract Database compared to a number of images used in the experiment.</b>
<p align="center">
  <img src="table/dataset .png" width="500"/>
</p>

## Extraction of region-of-interest (ROI) and image preprocessing

Preprocessing steps: 
- manual extraction of pupil region,
- cropping, ensuring proper centering,
- resized 224x224.


## Anomaly detection using autoencoder

The first stage of classification involved binary classification of images into cataract and non-cataract categories. Images from the Nuclear Cataract database classified as the IOL, were used for the non-cataract class.
Before implementing the autoencoder, the data underwent preprocessing, outlined in the previous section. The autoencoder model features a convolutional encoder that consists of four convolutional layers, each followed by a ReLU activation function. Similarly, the decoder is composed of four deconvolutional layers, with each layer also incorporating a ReLU activation function.


## Selection of Neural Network 

Selected dataset was split into 80\% training set, 10\% validation set and 10\% testing set. Additionally, to ensure the network learns from all classes, the dataset splitting is stratified, thereby preserving the class proportions within each subset.
Furthermore, data augmentation techniques were employed, including random horizontal flip with a probability of 0.5, random rotation from -15 to +15 degrees, and color jitter randomly adjusting brightness and contrast by a factor of up to 0.2. Subsequently, the images were converted to tensors and normalised using ImageNet statistics.

## Neural network model conversion to TensorFlow Lite

The trained neural network model was saved in the .pth format from PyTorch. It was first exported to the ONNX format, then converted to a TensorFlow model, and finally transformed into the .tflite format for compatibility with mobile devices.
VGG11 achieved the best classification quality metrics and highest accuracy in the TFLite format among all analyzed models. Consequently, this model was selected and implemented in the mobile application for nuclear cataract classification.



## Android-Based Application

The app allows users to:

1. Select from the gallery or capture an eye image.
2. Mark the pupil region using a draggable ellipse with control points.
3. Automatically extract the pupil area and perform classification using an embedded deep learning model (`vgg11.tflite`).
4. Display the classification result and classification time on-screen.

The application performs all inference **locally on-device** (no internet required), and supports real-time testing of the proposed method in the article.


## User interface

<table>
  <tr>
    <td align="center">
      <img src="screenshots/Screenshot_imagepicker.png" width="200"/><br>
      <b>Image selection (gallery or camera)</b>
    </td>
    <td align="center">
      <img src="screenshots/Screenshot_markpupil_info.png" width="200"/><br>
      <b>Instruction shown before pupil marking</b>
    </td>
    <td align="center">
      <img src="screenshots/Screenshot_markpupil.png" width="200"/><br>
      <b>After pupil marking using ellipse</b>
    </td>
  </tr>
    <td align="center">
      <img src="screenshots/Screenshot_classification_result_2.png" width="200"/><br>
      <b>Classification result (Cataract)</b>
    </td>
    <td align="center">
      <img src="screenshots/Screenshot_classification_result_1.png" width="200"/><br>
      <b>Classification result (Non-Cataract)</b>
    </td>
      <td align="center">
      <img src="screenshots/Screenshot_classification_photo.png" width="200"/><br>
      <b>Classification result after taking a photo to classify </b>
    </td>
  </tr>
</table>


## Download APK 

> ⚠️ Disclaimer - v1.0.0 - This is an early prototype of the application for academic research only. It's not optimized for production, and the APK file size is large (~700 MB) because it includes an uncompressed deep learning model.
v1.0.0 - [ Download AICataractDetector.apk](https://www.dropbox.com/scl/fi/14u5210c25b0urh24adnx/AICataractDetector.apk?rlkey=40nl7ml86db169dkjybrdbmd0&st=z117w112&dl=0)

To install the app on Android, allow installations from unknown sources in settings.


## License and Disclaimer

This application is intended for research and academic demonstration purposes only. It is **not a certified medical diagnostic tool** and should not be used for clinical decision-making.

## Citation
Ignatowicz A.;Marciniak T.; Marciniak E., AI-Powered Mobile App for Cataract Detection - <b>To be published</b>
