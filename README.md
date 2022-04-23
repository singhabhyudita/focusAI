<h1 align="center">Focus AI</h1>
<p align="center">
</p>

<a href="https://hack36.com"> <img src="https://cutt.ly/BuiltAtHack36" height=24px> </a>

Note: There are two branches in this project. The code of the Android application is in branch android_app. 
## Introduction:
  When considering the dangers of alcohol and car accidents, most people think of drunk driving. However, a large percentage of vehicle accidents involve pedestrians who have been drinking.
A substantial part of the pedestrian accident problem arises from intoxicated pedestrians.
Many people don’t realize just how dangerous it is to walk drunk. 
When you’re walking, and you’re involved in a crash, you don’t have the same protection as those in a motor vehicle.
Additionally, motor vehicles have lights that make them more visible to drivers on the road, and a pedestrian is much smaller than a vehicle. 
All of these factors mean that pedestrians who walk drunk are putting their safety in jeopardy. 
Similar issues can be found in the case of distracted walking.
The distracted phone‐use behaviours among pedestrians, like Texting, Game Playing and Phone Calls, have caused increasing fatalities and injuries.  
It is desired to improve both the driving and pedestrian safety by automatically discovering the phone‐related pedestrian distracted behaviours. 
We propose an AI based solution to the above two problems which will be an add on to the phone's security measures. In this method, the linear accelerometer of the person's phone presents a path using the continuous x, y and z coordinates which is passed through our trained model which describes the person's state. This can be: 

- Normal
- Drunk
- Distracted


In the past 36 hours, we've made a dataset consisting of 22k datapoints, and trained a convolutional neural network to get an accuracy of around 80 percentage.  
When a person is distracted while using his phone, he will be notified on his phone itself, and this system would be more sensitive when the person is near areas of traffic or danger using the person's location.
Similarly, when a person is heavily drunk, the model detects the unstable motion and after a certain threshold would generate an SOS call which will notify the specified person and provide the intoxicated person's location. Outside the 36 hours, we've decided to add sobriety test to prevent rare false alarms before sending the SOS.
  
## Demo Video Link:
  <a href="https://drive.google.com/file/d/14Ar0FxXDizvDio_Xh6Xq36WklLWJELIU/view">Click here...</a>
  
## Presentation Link:
  <a href="https://drive.google.com/file/d/1Og-rWnAioFsySGSuhTSswYFdiT1rg9vz/view"> PPT link here </a>

## Technology Stack:
  1) Native Android in Java.
  2) Tensorflow
  3) Tensorflow Lite

## Contributors:

Team Name: Hasbulla FC

* [Abhyudita Singh](https://github.com/singhabhyudita)
* [Harsh Gyanchandani](https://github.com/harshh3010)
* [Saurabh Singh](https://github.com/mrdinosaurabh)
* [Utkarsh Rai](https://github.com/utr491)


### Made at:
<a href="https://hack36.com"> <img src="https://cutt.ly/BuiltAtHack36" height=24px> </a>
