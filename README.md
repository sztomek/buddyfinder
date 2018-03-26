# BuddyFinder
The project consists of two projects - and Android app and a firebase functions app.
The android app is a reference app fully written in Kotlin using CLEAN architecture.
The base idea is to let users build their social connections and request other users location.
The firebase functions app is a thin backend which provides HTTPS endpoints for the client.

# Technologies/frameworks
* Android SDK + Kotlin
* Firebase:
-- Firestore
-- Auth
-- Cloud Functions
-- Cloud Messaging
* Retrofit + OkHttp3
* RxJava2
* Dagger2
* Picasso
* Android Architecture Components
-- LiveData
-- Lifecycle
-- ViewModel
* CLEAN architecture
-- MVVM for presentation
* Node.js + TypeScript + npm [Cloud functions project]

# How does this work?
The Cloud Functions app is a thin backend sitting on firebase modules like auth and firestore database. It contains triggers:
-- Auth trigger to store user in database automatically
-- Database triggers to send push notifications and update data
and HTTPS endpoints:
-- all write operation is done via HTTPS endpoints. The validation logic is hosted in the Cloud Functions app.
The Android app reads data from the database via the Firestore library, but the write operations are relayed to the Cloud Functions app.

# Screens
* Login
![Login](https://preview.ibb.co/k9Nzpn/login.png)
Using Firebase-Auth-UI (https://github.com/firebase/FirebaseUI-Android)
* Landing
![Landing](https://preview.ibb.co/gdXzpn/landing.png)
After a successful login, the user finds him/herself on the landing page. 
The main navigation items are available via the NavigationDrawer. 
The first element is selected by default (Discover).
The drawer also displays the current user and shows a Spinner to select location mode:
-- offline: won't upload live location data
-- online: continuously uploads live location to the server
Live location is public and accessible to all users in the connection network of the current user.
* Discover
This screen displays a MapView, showing the current location of the device with a blue marker and the connected users' locations - if they are accessible.
* Search
![Search](https://preview.ibb.co/bUJHaS/search.png)
Search is the place when the user can grow his/her social network by searching for people who may know by their names.
The list items are heterogeneus, showing different row layouts for different connection states:
-- not connected - the current user can send connection request
-- has pending incoming request - the other user has recently sent a connection request to the current user. The current user can accept or decline the request.
-- has pending outgoing request - the current user has sent a a connection request, but the other user hasn't accepted/declined it yet. The current user can cancel his request.
-- connected - there is a connection between the two users. The current user can request the other user's location.
Each row can be clicked - the selected user profile will be displayed
```TODO: saerch feature currently has limitations due to the lack of full text search from the Firebase Realtime Database library. ```
* My Connections
![Outgoing](https://preview.ibb.co/gDFKpn/outgoing.png)
This screen lists the pending and active connections on 3 tabs. Each tab is filterable by user name and has its own rown type with the proper action(s).
* Profile Details
![Profile](https://preview.ibb.co/j4noh7/profile.png)
Shows the selected user's profile and displays other actions according to the current connection state between the two users. 
 
# Wanna try it out?
First you'll need to create a Firebase project and enable the following features/APIs:
* Auth
-- Facebook (don't forget to configure)
-- Google (don't forget to configure)
-- Email 
-- Twitter (don't forget to configure)
* Firestore
* Cloud Messaging
* Cloud functions

Add an Android app, setup package name (don't forget to rename) and add your SHA1 key.
Download google-services.json and copy it to ```android/app/``` folder.
Install Node.js and npm.
Setup the Firebase CLI (https://github.com/firebase/firebase-tools) and create your own Firebase Functions project.
Then navigate to ```backend/functions/``` folder. Open command line and type ```npm run build && npm run deploy```
The script will deploy the app to the cloud. At the end of the script, an URL will be displayed which provides HTTPS API access. You'll need to copy this URL to ```hu.sztomek.wheresmybuddy.data.di.NetworkModule#provideRetrofit(OkHttpClient)``` function.
Build and enjoy.
