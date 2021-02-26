/**
 *  Copyright 2018 AdamV
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *  Version 1.0
 *  Author: AdamV
 *  Date: 2020-02-27
 *  Designed to enable buttons without hold functionality to virualise a hold function
 *
 */
 
metadata {
	definition (name: "Virtual Button Device", namespace: "my Devices", author: "AdamV") {

		capability "PushableButton"
		capability "HoldableButton"
		capability "DoubleTapableButton"
		capability "ReleasableButton"
        
        attribute "waiting", "bool"
        
        command "push"
        command "release"
        command "hold"
        command "doubleTap"
  }
    
        preferences{
        input name: "numberOfButtons", type: "number", title: "Enter number of buttons:", defaultValue: 4, description: ""
        input name: "calcHeld", type: "bool", title: "Enable held events", defaultValue: false, description: "When on held events will be calculated for this device. This impacts hub performance and device response lag"
        input name: "calcHeldTime", type: "number", title: "Enter number of milliseconds to wait for hold:", defaultValue: 550, description: ""
        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true, description: ""
    }

}

void installed(){
    log.warn "installed..."
    sendEvent(name:"numberOfButtons",value: numberOfButtons)
}

void updated() {
    log.info "updated..."
    log.warn "description logging is: ${txtEnable == true}"
    sendEvent(name:"numberOfButtons",value: numberOfButtons)
}

def push(value){
    String descriptionText = "${device.displayName} button ${value} was pushed"
    switch (calcHeld) {
        case true:
        runInMillis(calcHeldTime, waitHold, [data: value])
        state.waiting = true
        sendEvent(name: "waiting", value: true)
        break
        case false:
            if (txtEnable) log.info "${descriptionText}"
            sendEvent(name: "pushed", value: value, isStateChange:true)
        break
    }
}

def release(value){
    String descriptionText = "${device.displayName} button ${value} was released"
        switch (calcHeld) {
        case true:
        state.waiting = false
        sendEvent(name: "waiting", value: false)
        if (txtEnable) log.info "${descriptionText}"
        sendEvent(name: "released", value: value, isStateChange:true)
        break
        case false:
        if (txtEnable) log.info "${descriptionText}"
        sendEvent(name: "released", value: value, isStateChange:true)
        break
    }
}

def hold(value){
    String descriptionText = "${device.displayName} button ${value} was held"
    if (txtEnable) log.info "${descriptionText}"
    sendEvent(name: "held", value: value, isStateChange:true)
}

def doubleTap(value){
    String descriptionText = "${device.displayName} button ${value} was double tapped"
    switch (calcHeld) {
        case true:
        log.debug("$device.lastEvent")    
        break
        case false:
            if (txtEnable) log.info "${descriptionText}"
            sendEvent(name: "doubleTapped", value: value, isStateChange:true)
        break
    }
}

def waitHold(value){
        switch (state.waiting) {
        case true: 
        hold(value)
        state.waiting = false
        sendEvent(name: "waiting", value: false)
        break
        case false:
        String descriptionText = "${device.displayName} button ${value} was pushed"
        if (txtEnable) log.info "${descriptionText}"
        sendEvent(name: "pushed", value: value, isStateChange:true)
        break 
    }
}

def sendFillerPush() {
	sendEvent(name: "pushed", value: 0)
} 

def sendFillerHold() {
	sendEvent(name: "held", value: 0)
}

def sendFillerDoubleTapped() {
	sendEvent(name: "doubleTapped", value: 0)
} 

def sendFillerRelease() {
	sendEvent(name: "released", value: 0)
}

def sendneutraliser() {
	if (state.pushed > 0){
		sendEvent(name: "pushed", value: 0)
	//	log.debug("pushed neutralised")
	}
	if (state.doubleTapped > 0){
		sendEvent(name: "doubleTapped", value: 0)
	//	log.debug("doubleTapped neutralised")
	}
	if (state.held > 0){
		sendEvent(name: "held", value: 0)
	//	log.debug("held neutralised")
	}
	if (state.released > 0){
		sendEvent(name: "released", value: 0)
	//	log.debug("released neutralised")
	}
}





