/**
 *  Water Pump Sensor
 *
 *  Author: Jonathan Knoll
 */
definition(
    name: "Water Pump Sensor",
    namespace: "infinityplusone",
    author: "infinityplusone",
    description: "Monitors a contact switch that is installed on a Litter Robot and when the selected number of cycles compleate it will send a notification",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
  section("Water Pump Sensor") {
    input "waterSensor", "capability.waterSensor"
  }

  section("Water Pump Sensor Virtual Device"){
    input "waterPump", "capability.waterSensor", title: "Water Pump Sensor Virtual Device"
  }

  section("Notify Every X Minutes") {
    input "minutesDelay", "number", title: "Minute Delay"
  }
    // section("Auto Shutoff Options"){
    //   input "waterSwitch","capability.switch",title:"Water Pump Sensor Switch", required:false
    // }
  
  section( "Notifications" ) {
      input("recipients", "contact", title: "Send notifications to") {
          input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
          input "phone1", "phone", title: "Send a Text Message?", required: false
      }
  }
  
}

def installed() {
  subscribe(waterSensor, "water", waterLevelHandler)
  // subscribe(waterPump, "switch", waterSwitchHandler)
}

def updated() {
  unsubscribe()
  subscribe(waterSensor, "water", waterLevelHandler)
  // subscribe(waterPump, "switch", waterSwitchHandler)

}

def waterLevelHandler(evt) {
  if (evt.value == "dry")
    {
      log.debug "Water Sensor Dry"
    runIn(3 * 60, notifyDisabled);
    }
    if (evt.value == "wet")
    {
      waterPump.wet()
      log.debug "Water Sensor Wet"
      unschedule(notifyDisabled)
      waterPump.on()
    }
}

def notifyDisabled() {
  send("Water Pump is disabled! Check on it soon!")
    runIn(minutesDelay * 60, notifyDisabled);
    waterPump.ok()
    waterPump.disabled()
}

private send(msg) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }

    log.debug msg
}