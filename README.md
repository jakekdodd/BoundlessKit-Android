# What is DopamineKit?

[![Bintray](https://img.shields.io/bintray/v/cuddergambino/maven/dopaminekit.svg?maxAge=2592000?style=plastic)](https://bintray.com/cuddergambino/maven/dopaminekit)

[![Maven Central](https://img.shields.io/maven-central/v/com.usedopamine/dopaminekit.svg?maxAge=2592000)](https://bintray.com/cuddergambino/maven/dopaminekit)

[![Maintenance](https://img.shields.io/maintenance/yes/2017.svg?maxAge=2592000)](mailto:team@usedopamine.com)


DopamineKit provides wrappers for accessing the DopamineAPI and expressive UI reinforcements for your app.

Get your free API key at [http://dashboard.usedopamine.com/](http://dashboard.usedopamine.com/)

Learn more at [http://usedopamine.com](http://usedopamine.com)

### Looking for an Android Example App?

A simple "To Do List" Android App is included in the [DopamineKit-Android-HelloWorld repo](https://github.com/DopamineLabs/DopamineKit-Android-HelloWorld) to demonstrate how DopamineKit may be used in your code.

## Integrating DopamineKit

  1. First, make sure you have received your API key and other credentials, which are in the configuration file __dopamineproperties.json__ automatically generated from the [Dopamine Developer Dashboard](http://dashboard.usedopamine.com).

  2. Import the DopamineKit framework into your app's `build.gradle` by using JCenter or Maven using the following line

  ```groovy
  repositories {
        jcenter() // or if you prefer mavenCentral()
    }
    
  dependencies {
        compile 'com.usedopamine:dopaminekit:4.0.4'
  	}
  ```

  3. Import the DopamineKit library

  ```java
  import com.usedopamine.dopaminekit.DopamineKit;
  ```

  4. Move __dopamineproperties.json__ into the directory _`app/src/main/res/raw`_

  ![Workspace snapshot](readme/TestApp_with_DopamineKit_and_dopamineproperties.png)
  *Shown from the left to right pane:*
    
  - *left: credentials file stored at `app/src/main/res/rawdopamineproperties.json`,*

  - *center: gradle DopamineKit dependency*

  - *right: java import of DopamineKit*
  
  5. Start using Dopamine! The main features of DopamineKit are the `DopamineKit.reinforce()` and `DopamineKit.track()` functions. These should be added into the response functions of any _action_ to be reinforced or tracked.

## Using DopamineKit

###### DopamineKit.reinforce()

  -  For example, when a user marks a task as completed in a "To Do List" app or finishes a workout in a "Fitness" app, you should call `DopamineKit.reinforce()`.

  ```java
	JSONObject metaData = new JSONObject().put("taskName", taskName);
    DopamineKit.reinforce(getBaseContext(), 
                          "taskCompleted", 
                          metaData,
                          new DopamineKit.ReinforcementCallback() {

        @Override
        public void onReinforcement(String reinforcementDecision) {
                                        
            // Multiple reinforcements can help increase the surprise factor!
            // You can also use any UI components you made like
            // this.showInspirationalQuote() or this.showFunnyMeme()

            if(reinforcementDecision.equals("stars")){
                
            }
            else if(reinforcementDecision.equals("medalStar")){
                
            }
            else if(reinforcementDecision.equals("thumbsUp")){
                            }
            else {
                // Show nothing! This is called a neutral response,
                // and builds up the good feelings for the next surprise!
            }
        }

    });

  ```  

###### DopamineKit.track()

  - The `DopamineKit.track()` function is used to track other user actions. Using `track()` gives Dopamine a better understanding of user behavior, and will make your optimization and analytics better.
  - Continuing the example, you could use `DopamineKit.track()` function to record when the user adds new tasks in your AddTaskActivity's `onCreate()` method for the  "To Do List" app, or  record `userCheckedDietHistory()` in the "Fitness" app.


  Let's track when a user adds a food item in a "Fitness" app. We will also add the calories for the item in the `metaData` field to gather richer information about user engagement in my app.

  ```java
    JSONObject metaData = new JSONObject().put("calories", "400");
    DopamineKit.track(getBaseContext(), "foodItemAdded", metaData);
   ```



## Super Users

There are additional parameters for the `track()` and `reinforce()` functions that are used to gather rich information from your app and better create a user story of better engagement.

========

#### Tracking Calls

A tracking call should be used to record and communicate to DopamineAPI that a particular action has been performed by the user, each of these calls will be used to improve the reinforcement model used for the particular user. The tracking call itself is asynchronous and non-blocking. Failed tracking calls will not return errors, but will be noted in the log.

###### General syntax

```
Dopamine.track(context, actionID, metaData)
```

###### Parameters:

 - `context: Context` - is used to get API credentials from `res/raw/dopamineproperties.json` of the context's package

 - `actionID: String` - is a unique name for the action that the user has performed

 - `metaData: @Nullable JSONObject` - is any additional data to be sent to the API

========

#### Reinforcement Calls

A reinforcement call should be used when the user has performed a particular action that you wish to become a 'habit', the reinforcement call will return the name of the feedback function that should be called to inform, delight or congratulate the user. The names of the reinforcement functions, the feedback functions and their respective pairings may be found and configured on the developer dashboard.

###### General syntax

```
Dopamine.reinforce(context, actionID, metaData, callback)
```

###### Parameters:

 - `context: Context` - is used to get API credentials from `res/raw/dopamineproperties.json` of the context's package

 - `actionID: String` - is a unique name for the action that the user has performed

 - `metaData: @Nullable JSONObject` - is any additional data to be sent to the API

 - `callback: DopamineKit.ReinforcementCallback` - is an object on which `onReinforcement(String reinforcementDecision)` is called when a response is received

========

#### dopamineproperties.json

`dopamineproperties.json ` _must_ be contained within the directory _`app/src/main/res/raw`_. This property list contains configuration variables needed to make valid calls to the API, all of which can be found on your developer dashboard:

 - `appID: String` - uniquely identifies your app, get this from your [developer dashboard](http://dev.usedopamine.com).

 - `versionID: String` -  this is a unique identifier that you choose that marks this implementation as unique in our system. This could be something like 'summer2015Implementation' or 'ClinicalTrial4'. Your `versionID` is what we use to keep track of what users are exposed to what reinforcement and how to best optimize that.

 - `inProduction: Bool` - indicates whether app is in production or development mode, when you're happy with how you're integrating Dopamine and ready to launch set this argument to `true`. This will activate optimized reinforcement and start your billing cycle. While set to `false` your app will receive dummy reinforcement, new users will not be registered with our system, and no billing occurs.

 - `productionSecret: String` - secret key for production

 - `developmentSecret: String` - secret key for development
