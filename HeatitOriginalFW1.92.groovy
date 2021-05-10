/**
 *  Copyright 2020 AdamV
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
 * V1.0 14/01/2020
 *
 *
 * Changelog:
 * 
 * 2.0.0 - Updated for 1.92 with no security
 * 1.0.0 - Dashboard control of Floor Mode + Alexa trickery + other clean ups and improvements
 * 0.9.4 - Added Temperature Control Hysteresis + code clean up
 * 0.9.3 - Corrected the energySaveHeat Command so that you can specifically activate this from CoRE rules
 * 0.9.2 - Fixed an issue preventing some commands being fired when they are not triggered from the DTH UI
 */
 
 preferences {
            
            def myOptions = ["F - Floor temperature mode", "A - Room temperature mode", "AF - Room mode w/floor limitations", "A2 - Room temperature mode (external)", "P - Power regulator mode", "FP - Floor mode with minimum power limitation", "A2F - Room mode (external) w/floor limitations"]
			input "tempSenseMode", 
            "enum", 
            title: "Select Temperature Sensor Mode",
           // description: "F - Floor mode: Regulation is based on the floor temperature sensor reading \nA - Room temperature mode: Regulation is based on the measured room temperature using the internal sensor (Default) \nAF - Room mode w/floor limitations: Regulation is based on internal room sensor but limited by the floor temperature sensor (included) ensuring that the floor temperature stays within the given limits (FLo/FHi) \nA2 - Room temperature mode: Regulation is based on the measured room temperature using the external sensor \nP (Power regulator): Constant heating power is supplied to the floor. Power rating is selectable in 10% increments ( 0% - 100%) \nFP - Floor mode with minimum power limitation: Regulation is based on the floor temperature sensor reading, but will always heat with a minimum power setting (PLo)",
           	defaultValue: "A - Room temperature mode",
            required: true, 
            options: myOptions, 
            displayDuringSetup: true
            
            input "FLo",
        	"number",
            range: "5..40",
            title: "FLo: Floor min limit",
            description: "Minimum Limit for floor sensor (5°-40°)",
			defaultValue: 5,
			required: false,
            displayDuringSetup: false
            
            input "FHi",
        	"number",
            range: "5..40",
            title: "FHi: Floor max limit",
            description: "Maximum Limit for floor sensor  (5°-40°)",
			defaultValue: 40,
			required: false,
            displayDuringSetup: false
            
            def sensOptions = ["10k ntc (Default)", "12k ntc", "15k ntc", "22k ntc", "33k ntc", "47k ntc"]
			input "sensorType", 
            "enum", 
            title: "Select Floor Sensor Type",
            //description: "",
            defaultValue: "10k ntc (Default)",
            required: false, 
            options: sensOptions, 
            displayDuringSetup: false
            
            input "ALo",
        	"number",
            range: "5..40",
            title: "ALo: Air min limit",
            description: "Minimum Limit for Air sensor (5°-40°)",
			defaultValue: 5,
			required: false,
            displayDuringSetup: false
            
            input "AHi",
        	"number",
            range: "5..40",
            title: "AHi: Air max limit",
            description: "Maximum Limit for Air sensor  (5°-40°)",
			defaultValue: 40,
			required: false,
            displayDuringSetup: false
            
            input "PLo",
        	"number",
            range: "0..9",
            title: "PLo: FP-mode P setting",
            description: "FP-mode P setting (0 - 9)",
			defaultValue: 0,
			required: false,
            displayDuringSetup: false
            
            def pOptions = ["0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"]
            input "PSet",
        	"enum",
            //range: "0..100",
            title: "PSetting",
            description: "Power Regulator setting (0 - 100%)",
			defaultValue: "20%",
			required: false,
            options: pOptions,
            displayDuringSetup: false
            
            input "DIFF",
        	"number",
            range: "2..30",
            title: "DIFF l: Temperature control Hysteresis",
            description: "Hysteresis (0.2 - 3.0) [type in as 2 - 30]",
			required: false,
            displayDuringSetup: false
           
            input "tempReportInterval",
        	"number",
            range: "0..86400",
            title: "Reporting Interval",
            description: "Time interval between consecutive temperature reports (0 - 86400) (seconds). Reporting of temperatures disabled if set to 0.",
			defaultValue: 60,
			required: false,
            displayDuringSetup: false
     
            input "calibration",
        	"number",
            range: "-40...40",
            title: "Internal sensor Calibration",
            description: "(-4.0° --> 4.0° - put in as -40 --> 40)",
			defaultValue: 0,
			required: false,
            displayDuringSetup: false
     
            input "extcalibration",
        	"number",
            range: "-40...40",
            title: "External sensor Calibration",
            description: "(-4.0° --> 4.0° - put in as -40 --> 40)",
			defaultValue: 0,
			required: false,
            displayDuringSetup: false
     
            input name: "debugOutput",   type: "bool", title: "<b>Enable debug logging?</b>",   description: "<br>", defaultValue: true  
}
    
def updated() {

    configure()
}

metadata {
	definition (name: "heatit Thermostat (1.92FW) [not secure paired]", namespace: "heatit", author: "AdamV") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
        capability "Thermostat Mode"
        capability "Thermostat Heating Setpoint"
        capability "Thermostat Setpoint"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
        capability "Switch"
		capability "ThermostatOperatingState"
        capability "Refresh"
        
        attribute "tempSenseMode", "string", ["A", "AF", "F", "A2", "P", "FP", "A2F"]
        attribute "intAirTemperature", "number"
        attribute "extAirTemperature", "number"
        attribute "floorTemperature", "number"

        command "quickSetecoHeat", [[type: "number"]]
        command "setAssociation"
        command "pollTempSensors"
        command "temperatureMode", [[name:"Temperature Mode", type: "ENUM", description: "Pick an option", constraints: ["A", "AF", "F", "A2", "P", "FP", "A2F"] ] ]
        command "getCurrentConfiguration"
        command "energySaveHeat"
        
		fingerprint deviceId: "0x0806"
		fingerprint inClusters: "0x5E, 0x43, 0x31, 0x86, 0x40, 0x59, 0x85, 0x73, 0x72, 0x5A, 0x70"
	}
    
}

	
def parse(String description) {
	def results = []
    // log.debug("RAW command: $description")
	if (description.startsWith("Err")) {
		logDebug("An error has occurred")
		} 
    else {
       
       	def cmd = zwave.parse(description.replace("98C1", "9881"), [0x98: 1, 0x20: 1, 0x84: 1, 0x80: 1, 0x60: 3, 0x2B: 1, 0x26: 1])
        logDebug "Parsed Command: $cmd"
        if (cmd) {
       	results = zwaveEvent(cmd)
		}
    }
}

def setAssociation() {
    
   delayBetween([

		zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId).format(),
        zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: zwaveHubNodeId).format(),
        zwave.associationV2.associationRemove(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format(),
        zwave.associationV2.associationRemove(groupingIdentifier: 4, nodeId: zwaveHubNodeId).format(),
        zwave.associationV2.associationRemove(groupingIdentifier: 5, nodeId: zwaveHubNodeId).format(),
        zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]),
        zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 2, nodeId: [zwaveHubNodeId]),
       zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 3, nodeId: [zwaveHubNodeId]),
       zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 4, nodeId: [zwaveHubNodeId]),
        zwave.associationV2.associationGet(groupingIdentifier: 1).format(),
        zwave.associationV2.associationGet(groupingIdentifier: 2).format(),
        zwave.associationV2.associationGet(groupingIdentifier: 3).format(),
        zwave.associationV2.associationGet(groupingIdentifier: 4).format(),
        zwave.associationV2.associationGet(groupingIdentifier: 5).format()
       

     ], 1000)
}

def describeAttributes(payload) {
    	payload.attributes = [
       	[ name: "tempSenseMode",    type: "string",    options: ["A", "AF", "F", "A2", "P", "FP"], momentary: true ],
        ]
    	return null
		}	

def zwaveEvent(hubitat.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
	logDebug("setpoint rep: $cmd")
    
   	if (cmd.setpointType == 1){
        def heating = cmd.scaledValue
        sendEvent(name: "heatingSetpoint", value: heating)
    }
    if (cmd.setpointType == 3){
    	def energyHeating = cmd.scaledValue
        sendEvent(name: "ecoHeatingSetpoint", value: energyHeating)
        state.ecoheatingSetpoint = energyHeating
    }
   	
   // log.debug(heatingSetpoint)
   // state.heatingSetpoint = heatingSetpoint
    
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	//map
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    
    logDebug("Switch Binary Report received: $cmd")
    def map = [:]
	switch (cmd.value) {
		case 0:
			map.value = "idle"
			break
		case 255:
			map.value = "heating"
			break
	}
	map.name = "thermostatOperatingState"
	map
    
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv2.SwitchBinarySet cmd) {
    
  logDebug("Switch Binary Report received: $cmd")
    def map = [:]
	switch (cmd.switchValue) {
		case 0:
			map.value = "idle"
			break
		case 255:
			map.value = "heating"
			break
	}
	map.name = "thermostatOperatingState"
	map
    
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    
    def map = [:]
	switch (cmd.value) {
		case 0:
			map.value = "idle"
			break
		case 255:
			map.value = "heating"
			break
	}
	map.name = "thermostatOperatingState"
	map
    
 }

def zwaveEvent(hubitat.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
	//logDebug("Sensor report: $cmd")
    
    //def precision = cmd.precision / 10
   // log.debug("Precision: $precision °C")
    if (cmd.scale == 0){
   // log.debug("Scale is Celcius")
    }
    logDebug("Temp ... could be from any of the 3 sensors is: $cmd.scaledSensorValue °C")
   // sendEvent(name: "temperature", value: cmd.scaledSensorValue) 

}

def zwaveEvent(hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd)
{
	
	logDebug("operating rep: $cmd")
	def map = [:]
	switch (cmd.operatingState) {
		case hubitat.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_VENT_ECONOMIZER:
			map.value = "energySaveHeat"
			break
	}
	map.name = "thermostatOperatingState"
	map
}

def zwaveEvent(hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	logDebug("thermostat reprt: $cmd")
	def map = [:]
	switch (cmd.mode) {
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			sendEvent(name: "switch", value: "off")
            map.value = "off"
			sendEvent(name: "thermostatMode", value: "off")
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			sendEvent(name: "switch", value: "on")
            map.value = "heat"
			sendEvent(name: "thermostatMode", value: "heat")
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergency heat"
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
			break
        case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_ENERGY_SAVE_HEAT:
			map.value = "energySaveHeat"
			sendEvent(name: "thermostatMode", value: "energySaveHeat")
			sendEvent(name: "switch", value: "on")
			break
	}
	map.name = "thermostatMode"
	map
}

def zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    def intAirTemperature = 0
    def extAirTemperature = 0
    def floorTemperature = 0
    def sensemode = ""
    
    sensemode = state.tempSenseMode
    
    if (cmd.commandClass == 49) {
       
        switch (cmd.sourceEndPoint) {
		case 2:
                intAirTemperature = binaryToDegrees(cmd.parameter[2], cmd.parameter[3])
               // log.debug("Internal Air Temp Sensor is $intAirTemperature")
              //  state.intAirTemperature = intAirTemperature
                sendEvent(name: "intAirTemperature", value: intAirTemperature)
                if (sensemode == "A"){
                    sendEvent(name: "temperature", value: intAirTemperature)
                }
                if (sensemode == "AF"){
                    sendEvent(name: "temperature", value: intAirTemperature)
                }
                if (sensemode == "P"){
                    sendEvent(name: "temperature", value: intAirTemperature)
                }
			break
		case 3:
                extAirTemperature = binaryToDegrees(cmd.parameter[2], cmd.parameter[3])
             //   log.debug("External Air Temp is $extAirTemperature")
             //   state.extAirTemperature = extAirTemperature
                sendEvent(name: "extAirTemperature", value: extAirTemperature)
                if (sensemode == "A2"){
                    sendEvent(name: "temperature", value: extAirTemperature)
                }
                if (sensemode == "A2F" ){
                    sendEvent(name: "temperature", value: extAirTemperature)
                }
			break
        case 4:
			    floorTemperature = binaryToDegrees(cmd.parameter[2], cmd.parameter[3])
            //    log.debug("Floor Temp is $floorTemperature")
            //    state.floorTemperature = floorTemperature
                sendEvent(name: "floorTemperature", value: floorTemperature)
                if (sensemode == "FP" ){
                    sendEvent(name: "temperature", value: floorTemperature)
                }
                if (sensemode == "F"){
                    sendEvent(name: "temperature", value: floorTemperature)
                }
			break
	    }
    }
    

    
}

def zwaveEvent(hubitat.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	logDebug("support reprt: $cmd")
    def supportedModes = ""
	if(cmd.off) { supportedModes += "off " }
	if(cmd.heat) { supportedModes += "heat " }
	if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergency heat " }
	if(cmd.cool) { supportedModes += "cool " }
	if(cmd.auto) { supportedModes += "auto " }
    if(cmd.energySaveHeat) { supportedModes += "energySaveHeat " }

	state.supportedModes = supportedModes
}

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
   logDebug("full command is: $cmd")
    if (cmd.parameterNumber == 1){
    	if (cmd.configurationValue == [0]){
            logDebug("Current Mode is Off")
            sendEvent(name: "thermostatMode", value: "off")
			sendEvent(name: "switch", value: "off")

        }
        else if (cmd.configurationValue == [1]){
            logDebug("Current Mode is CO Heat")
            sendEvent(name: "thermostatMode", value: "heat")
			sendEvent(name: "switch", value: "on")

        }
        else if (cmd.configurationValue == [2]){
            logDebug("Current Mode is Cool")
        }
        else if (cmd.configurationValue == [11]){
            logDebug("Current Mode is ECO Heat")
            sendEvent(name: "thermostatMode", value: "energySaveHeat")
			sendEvent(name: "switch", value: "on")

        }
	}
    if (cmd.parameterNumber == 2){
    	if (cmd.configurationValue == [0]){
            logDebug("Temperature Sensor F - Floor mode: Regulation is based on the floor temperature sensor reading")
        	sendEvent(name: "tempSenseMode", value: "F")
            sendEvent(name: "thermostatFanMode", value: "F")
            state.tempSenseMode = "F"
        }
        else if (cmd.configurationValue == [1]){
            logDebug("Temperature Sensor A - Room temperature mode: Regulation is based on the measured room temperature using the internal sensor (Default)")
        	sendEvent(name: "tempSenseMode", value: "A")
            sendEvent(name: "thermostatFanMode", value: "A")
            state.tempSenseMode = "A"
        }
        else if (cmd.configurationValue == [2]){
            logDebug("Temperature Sensor AF - Room mode w/floor limitations: Regulation is based on internal room sensor but limited by the floor temperature sensor (included) ensuring that the floor temperature stays within the given limits (FLo/FHi")
        	sendEvent(name: "tempSenseMode", value: "AF")
            sendEvent(name: "thermostatFanMode", value: "AF")
            state.tempSenseMode = "AF"
        }
        else if (cmd.configurationValue == [3]){
           logDebug("Temperature Sensor 2 - Room temperature mode: Regulation is based on the measured room temperature using the external sensor")
        	sendEvent(name: "tempSenseMode", value: "A2")
            sendEvent(name: "thermostatFanMode", value: "A2")
            state.tempSenseMode = "A2"
        }
        else if (cmd.configurationValue == [4]){
          logDebug("Temperature Sensor P (Power regulator): Constant heating power is supplied to the floor. Power rating is selectable in 10% increments ( 0% - 100%)")
        	sendEvent(name: "tempSenseMode", value: "P")
            sendEvent(name: "thermostatFanMode", value: "P")
            state.tempSenseMode = "P"
        }
         else if (cmd.configurationValue == [5]){
            logDebug("Temperature Sensor FP - Floor mode with minimum power limitation: Regulation is based on the floor temperature sensor reading, but will always heat with a minimum power setting (PLo)")
        	sendEvent(name: "tempSenseMode", value: "FP")
            sendEvent(name: "thermostatFanMode", value: "FP")
             state.tempSenseMode = "FP"
        }
        else if (cmd.configurationValue == [6]){
            logDebug("Temperature Sensor FP - Floor mode with minimum power limitation: Regulation is based on the floor temperature sensor reading, but will always heat with a minimum power setting (PLo)")
        	sendEvent(name: "tempSenseMode", value: "A2F")
            sendEvent(name: "thermostatFanMode", value: "A2F")
            state.tempSenseMode = "A2F"
        }
	}
    if (cmd.parameterNumber == 3){
    	if (cmd.configurationValue == [0]){
        logDebug("Floor sensor type 10k ntc (Default)")
        }
        else if (cmd.configurationValue == [1]){
        logDebug("Floor sensor type 12k ntc")
        }
        else if (cmd.configurationValue == [2]){
        logDebug("Floor sensor type 15k ntc")
        }
        else if (cmd.configurationValue == [3]){
        logDebug("Floor sensor type 22k ntc")
        }
        else if (cmd.configurationValue == [4]){
        logDebug("Floor sensor type 33k ntc")
        }
         else if (cmd.configurationValue == [5]){
        logDebug("Floor sensor type 47k ntc")
        }
	}
    if (cmd.parameterNumber == 4){
    	
       def val = cmd.configurationValue[0]

        if (val == 0){
            def newHys = 0
        }
        else{
       def newHys = val / 10
       logDebug("DIFF l. Temperature control Hysteresis is $newHys °C")
	}
    }
    if (cmd.parameterNumber == 5){
    	
        def valX = cmd.configurationValue[0]
       	def valY = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX, valY)
       
       logDebug("FLo: Floor min limit is $diff °C")
	}
    if (cmd.parameterNumber == 6){
    	
     	def valX1 = cmd.configurationValue[0]
       	def valY1 = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX1, valY1)

      logDebug("FHi: Floor max limit is $diff °C")
	}
    if (cmd.parameterNumber == 7){
    	
        def valX = cmd.configurationValue[0]
       	def valY = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX, valY)
       logDebug("ALo: Air min limit is $diff °C")
	}
    if (cmd.parameterNumber == 8){
    	
        def valX1 = cmd.configurationValue[0]
       	def valY1 = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX1, valY1)
       logDebug("AHi: Air max limit is $diff °C")
	}
    if (cmd.parameterNumber == 9){
    	
       def val = cmd.configurationValue
       logDebug("PLo: Min temperature in Power Reg Mode is $val °C")
	}
    if (cmd.parameterNumber == 10){
    	
        def valX = cmd.configurationValue[0]
       	def valY = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX, valY)
        state.heatingSetpoint = diff
        sendEvent(name: "heatingSetpoint", value: diff)
       logDebug("CO mode setpoint is $diff °C")
        
	}
    if (cmd.parameterNumber == 11){
    	
        def valX = cmd.configurationValue[0]
       	def valY = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX, valY)
       logDebug("ECO mode setpoint is $diff °C")
       sendEvent(name: "ecoHeatingSetpoint", value: diff)
        
	}
    if (cmd.parameterNumber == 12){
    	
       def val = cmd.configurationValue[0]
       def diff = val * 10
       logDebug("P (Power regulator) is $diff %")
	}
    if (cmd.parameterNumber == 14){
    	
       def val = cmd.configurationValue[0]
       def cal = val / 10
       logDebug("Internal Sensor Calibration difference is $cal °C")
	}
    if (cmd.parameterNumber == 16){
    	
       def val = cmd.configurationValue[0]
       def cal = val / 10
       logDebug("External Sensor Calibration difference is $cal °C")
	} 
    if (cmd.parameterNumber == 17){
    	
        def interval = binarytoSeconds(cmd.configurationValue[0],cmd.configurationValue[1])
       logDebug("Auto temperature report interval is $interval seconds")

	}
    if (cmd.parameterNumber == 18){
    	
       logDebug("Raw temperature report change is $cmd")
	}
}

	
def pressUp(){
	//log.debug("pressed Up")
	def currTemp = device.latestValue("heatingSetpoint")
    //log.debug(" pressed up currently $currTemp")
    def newTemp = currTemp + 0.5
    //log.debug(" pressed up new temp is $newTemp")
	quickSetHeat(newTemp)
}

def pressDown(){
	//log.debug("pressed Down")
	def currTemp = device.latestValue("heatingSetpoint")
    def newTemp = currTemp - 0.5
	quickSetHeat(newTemp)
}

def roundToHalf(d) {
    
    def math = Math.round(d * 2) / 2.0

    return math
}

def quickSetHeat(degrees) {
	
	setHeatingSetpoint(degrees, 1000)
}

def setHeatingSetpoint(degrees, delay = 30000) {
	setHeatingSetpoint(degrees.toDouble(), delay)
}

def setHeatingSetpoint(Double degrees, Integer delay = 30000) {
	//log.trace "setHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    //if (locationScale == "C" && deviceScaleString == "F") {
    //	convertedDegrees = celsiusToFahrenheit(degrees)
    //} else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    //} else {
    	convertedDegrees = roundToHalf(degrees) as int
  //  log.debug(degrees)
    //}
//	runIn(3, poll)
	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
	], 750)
}

def quickSetecoHeat(degrees) {
	
	setecoHeatingSetpoint(degrees, 1000)
}

def setecoHeatingSetpoint(degrees, delay = 30000) {
	setecoHeatingSetpoint(degrees.toDouble(), delay)
}

def setecoHeatingSetpoint(Double degrees, Integer delay = 30000) {
//	log.trace "setecoHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    //if (locationScale == "C" && deviceScaleString == "F") {
    //	convertedDegrees = celsiusToFahrenheit(degrees)
    //} else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    //} else {
    	convertedDegrees = roundToHalf(degrees) as int
        formatedDegrees = degreestoBinary(degrees)
        formatedDegrees[0] = formatedDegrees[0] as int
        formatedDegrees[1] = formatedDegrees[1] as int
        
          
	def cmds = []
        
      	cmds +=  zwave.configurationV2.configurationSet(configurationValue: [formatedDegrees[0],formatedDegrees[1]], parameterNumber:11, size: 2).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber:11).format()
    
    if (cmds) return delayBetween(cmds, 750)

}

def poll() {
	
        
    def cmds = []
	
        cmds +=  zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
		cmds +=  zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()
        cmds +=  zwave.thermostatModeV2.thermostatModeGet().format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()

    if (cmds) return delayBetween(cmds, 800)
	
}

def refresh(){
    
		runIn(1,pollTempSensors)
        runIn(4,poll)
	}

def configure() {

//Floor Minimum Temperature Limit    
    def floorMinY = 0
    def floorMinX = 0
    def floorMin = 0
    if (FLo){
    	floorMin = FLo
        if (floorMin <= 25.5){
        	floorMinX = 0 
        	floorMinY = floorMin*10 
        }
        else if (floorMin > 25.5){
            floorMinX = 1
        	floorMinY = floorMin*10 - 256
        }
    }    
//Floor Maximum Temperature Limit    
    def floorMax = 0
    def floorMaxY = 0
    def floorMaxX = 0
    if (FHi){
    		floorMax = FHi
    	    if (floorMax <= 25.5){
        	floorMaxX = 0
        	floorMaxY = floorMax*10 
        }
        else if (floorMax > 25.5){
            floorMaxX = 1 
        	floorMaxY = floorMax*10 - 256
        }
    }
//Air Minimum Temperature Limit    
    def AirMin = 0
    def AirMinY = 0
    def AirMinX = 0
    if (ALo){
    		AirMin = ALo
    	    if (AirMin <= 25.5){
        	AirMinX = 0
        	AirMinY = AirMin*10
        }
        else if (AirMin > 25.5){
            AirMinX = 1
        	AirMinY = AirMin*10 - 256
        }
    }
//Air Maximum Temperature Limit      
    def AirMax = 0
    def AirMaxY = 0
    def AirMaxX = 0
    if (AHi){
    		AirMax = AHi
    	    if (AirMax <= 25.5){
        	AirMaxX = 0
        	AirMaxY = AirMax*10
        }
        else if (AirMax > 25.5){
            AirMaxX = 1
        	AirMaxY = AirMax*10 - 256
        }
    }
//Temperature Sensor Mode
    def tempSensorMode1 = ""
    def tempModeParam = 1
    if (tempSenseMode){
    tempSensorMode1 = tempSenseMode
        if (tempSensorMode1 == "F - Floor temperature mode"){
        tempModeParam = 0
        }
        if (tempSensorMode1 == "A - Room temperature mode"){
        tempModeParam = 1
        }
        if (tempSensorMode1 == "AF - Room mode w/floor limitations"){
        tempModeParam = 2
        }
        if (tempSensorMode1 == "A2 - Room temperature mode (external)"){
        tempModeParam = 3
        }
       	if (tempSensorMode1 == "P - Power regulator mode"){
        tempModeParam = 4
        }
        if (tempSensorMode1 == "FP - Floor mode with minimum power limitation"){
        tempModeParam = 5
        }
        if (tempSensorMode1 == "A2F - Room mode (external) w/floor limitations"){
        tempModeParam = 6
        }
    }
//Floor Sensor Type  	
    def floorSensor = ""
    def floorSensParam = 0
    	if (sensorType){
        floorSensor = sensorType
            if (floorSensor == "10k ntc (Default)"){
            floorSensParam = 0
            }
            if (floorSensor == "12k ntc"){
            floorSensParam = 1
            }
            if (floorSensor == "15k ntc"){
            floorSensParam = 2
            }
            if (floorSensor == "22k ntc"){
            floorSensParam = 3
            }
            if (floorSensor == "33k ntc"){
            floorSensParam = 4
            }
            if (floorSensor == "47k ntc"){
            floorSensParam = 5
            }
        }
//FP Mode P setting	
   	def powerLo = 0
    	if (PLo){
        	powerLo = PLo  
        }
    def powerSet = 0
    def powerSetPer = ""
    	if (PSet){
        powerSetPer = PSet
            if (powerSetPer == "0%"){
        	powerSet = 0
       		}
            if (powerSetPer == "10%"){
        	powerSet = 1
       		}
            if (powerSetPer == "20%"){
        	powerSet = 2
       		}
            if (powerSetPer == "30%"){
        	powerSet = 3
           // log.debug("powerset 3")
       		}
            if (powerSetPer == "40%"){
        	powerSet = 4
       		}
            if (powerSetPer == "50%"){
        	powerSet = 5
       		}
            if (powerSetPer == "60%"){
        	powerSet = 6
       		}
            if (powerSetPer == "70%"){
        	powerSet = 7
       		}
            if (powerSetPer == "80%"){
        	powerSet = 8
       		}
            if (powerSetPer == "90%"){
        	powerSet = 9
       		}
            if (powerSetPer == "100%"){
        	powerSet = 10
       		}
          
   def Hyster = 5
    		if (DIFF){
        	Hyster = (DIFF / 10) * 15
            }
        }
    
   def reportIntConfig = []
        if (tempReportInterval){
    
        reportIntConfig[0] =  secondstoBinary(tempReportInterval)[0] as int
        reportIntConfig[1] =  secondstoBinary(tempReportInterval)[1] as int
            
    }
//Calibration
   def calibrationVal = 0
        if (calibration){
    
        calibrationVal = calibration
        logDebug("cal = $calibrationVal")
    }
   def extcalibrationVal = 0
    if (extcalibration){
        extcalibrationVal = extcalibration
        logDebug("cal = $calibrationVal")
    }
    
    
    sendEvent(name: "supportedThermostatModes", value: ["off", "heat", "energySaveHeat"])

    def x1 = 0
    def x2 = 1
    def y1 = 60
    def y2 = 45
    
    x1 = floorMinX as int
    x2 = floorMaxX as int
    y1 = floorMinY as int
    y2 = floorMaxY as int

	def cmds = []
        
        cmds +=  zwave.configurationV2.configurationSet(configurationValue: [tempModeParam], parameterNumber: 2, size: 1).format()
      	cmds +=  zwave.configurationV2.configurationSet(configurationValue: [floorSensParam], parameterNumber: 3, size: 1).format()
       	cmds +=  zwave.configurationV2.configurationSet(configurationValue: [x1, y1], parameterNumber: 5, size: 2).format()
       	cmds +=  zwave.configurationV2.configurationSet(configurationValue: [x2, y2], parameterNumber: 6, size: 2).format()
        cmds +=  zwave.configurationV2.configurationSet(configurationValue: [AirMinX, AirMinY], parameterNumber: 7, size: 2).format()
        cmds +=  zwave.configurationV2.configurationSet(configurationValue: [AirMaxX, AirMaxY], parameterNumber: 8, size: 2).format()
        cmds +=  zwave.configurationV2.configurationSet(configurationValue: [powerLo], parameterNumber: 9, size: 1).format()
        cmds +=  zwave.configurationV2.configurationSet(configurationValue: [powerSet], parameterNumber: 12, size: 1).format()
        cmds +=  zwave.configurationV2.configurationSet(configurationValue: [calibrationVal], parameterNumber: 14, size: 1).format()
        cmds +=  zwave.configurationV2.configurationSet(configurationValue: [extcalibrationVal], parameterNumber: 16, size: 1).format()
        cmds +=  zwave.configurationV2.configurationSet(configurationValue: reportIntConfig, parameterNumber: 17, size: 2).format()
    
    if (cmds) return delayBetween(cmds, 750)
	
	
}

def modes() {
	["off", "heat", "energySaveHeat"]
}

def getCurrentConfiguration(){
    
    zwave.configurationV2.configurationGet().format()
		def cmds = []
	    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 1).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 3).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 4).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 5).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 6).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 7).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 8).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 9).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 10).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 11).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 12).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 14).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 15).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 17).format()
        cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 18).format() 

    if (cmds) return delayBetween(cmds, 900)
}

def pollTempSensors() {
	
    def cmds = []
    
        cmds += zwave.multiChannelV4.multiChannelCmdEncap(sourceEndPoint: 1, destinationEndPoint: 2, commandClass:49, command: 4).format()
        cmds += zwave.multiChannelV4.multiChannelCmdEncap(sourceEndPoint: 1, destinationEndPoint: 3, commandClass:49, command: 4).format()
        cmds += zwave.multiChannelV4.multiChannelCmdEncap(sourceEndPoint: 1, destinationEndPoint: 4, commandClass:49, command: 4).format()
    
    if (cmds) return delayBetween(cmds, 800)

}


def temperatureMode(mode){
    
    def tempSensorMode = ""
    def tempModeParam = 1
    tempSensorMode = mode
        if (tempSensorMode == "F"){
            
        tempModeParam = 0
        }
        if (tempSensorMode == "A"){
        tempModeParam = 1
        }
        if (tempSensorMode == "AF"){
        tempModeParam = 2
        }
        if (tempSensorMode == "A2"){
        tempModeParam = 3
        }
       	if (tempSensorMode == "P"){
        tempModeParam = 4
        }
        if (tempSensorMode == "FP "){
        tempModeParam = 5
        }
        if (tempSensorMode == "A2F"){
        tempModeParam = 6
        }
    

    def cmds = []
    cmds +=  zwave.configurationV2.configurationSet(configurationValue: [tempModeParam], parameterNumber: 2, size: 1).format()
    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
    if (cmds) return delayBetween(cmds, 1000)

}

def binaryToDegrees(x, y) {
	def degrees = (x*25.6) + (y/10)
    
    return degrees
}

def degreestoBinary(x) {
    def factored = x * 10
    def one = Math.floor(factored / 256)
    def two = factored - (one * 256)
    
    one = one.round()
    two = two.round()
    
    def binary = []
    binary[0] = one
    binary[1] = two
    
    return binary
}

def binarytoSeconds(x,y) {
    
    def seconds = 0
    
    if (x == 0){
        seconds = y*10
    }
    else {
        seconds = (x*2560) + (y*10)        
    }
    
    return seconds
}

def secondstoBinary(x) {
    
    def factored = x / 10
    def one = Math.floor(factored / 256)
    def two = factored - (one * 256)
    
    one = one.round()
    two = two.round()
    
    def binary = []
    binary[0] = one
    binary[1] = two
    
    return binary
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getModeMap() { [
	"off": 0,
	"heat": 1,
	"energySaveHeat": 11
]}

def setThermostatMode(String value) {
    
    delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def setThermostatFanMode(fanmode) {
    sendEvent(name: "supportedThermostatFanModes", value: ["F: Floor temperature mode", "A: Room temperature mode", "AF: Room mode w/floor limitations", "A2: Room temperature mode (external)", "P: Power regulator mode", "FP: Floor mode with minimum power limitation", "A2F: Room mode (external) w/floor limitations"])
	def tempSensorMode = ""
    tempSensorMode = fanmode
 //log.debug(tempSensorMode)
 //   "F: Floor temperature mode", "A: Room temperature mode", "AF: Room mode w/floor limitations", "A2: Room temperature mode (external)", "P: Power regulator mode", "FP: Floor mode with minimum power limitation", "A2F: Room mode (external) w/floor limitations"])
        if (tempSensorMode == "F: Floor temperature mode"){
            runIn(2, poll)    
            def cmds = []
                    cmds +=  zwave.configurationV2.configurationSet(configurationValue: [0, 0], parameterNumber: 2, size: 2).format()
                    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
                if (cmds) return delayBetween(cmds, 1000)
        }
        if (tempSensorMode == " A: Room temperature mode"){
            runIn(2, poll) 
               def cmds = []
                    cmds +=  zwave.configurationV2.configurationSet(configurationValue: [0, 1], parameterNumber: 2, size: 2).format()
                    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
                if (cmds) return delayBetween(cmds, 1000)
        }
        if (tempSensorMode == " AF: Room mode w/floor limitations"){
            runIn(2, poll) 
                def cmds = []
                    cmds +=  zwave.configurationV2.configurationSet(configurationValue: [0, 2], parameterNumber: 2, size: 2).format()
                    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
                if (cmds) return delayBetween(cmds, 1000)
        }
        if (tempSensorMode == " A2: Room temperature mode (external)"){
            runIn(2, poll) 
                def cmds = []
                    cmds +=  zwave.configurationV2.configurationSet(configurationValue: [0, 3], parameterNumber: 2, size: 2).format()
                    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
                if (cmds) return delayBetween(cmds, 1000)
        }
       	if (tempSensorMode == " P: Power regulator mode"){
            runIn(2, poll) 
                def cmds = []
                    cmds +=  zwave.configurationV2.configurationSet(configurationValue: [0, 4], parameterNumber: 2, size: 2).format()
                    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
                if (cmds) return delayBetween(cmds, 1000)
        }
        if (tempSensorMode == " FP: Floor mode with minimum power limitation"){
            runIn(2, poll)
                def cmds = []
                    cmds +=  zwave.configurationV2.configurationSet(configurationValue: [0, 5], parameterNumber: 2, size: 2).format()
                    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
                if (cmds) return delayBetween(cmds, 1000)
        }
        if (tempSensorMode == " A2F: Room mode (external) w/floor limitations"){
            runIn(2, poll)
                def cmds = []
                    cmds +=  zwave.configurationV2.configurationSet(configurationValue: [0, 6], parameterNumber: 2, size: 2).format()
                    cmds +=  zwave.configurationV2.configurationGet(parameterNumber: 2).format()
                if (cmds) return delayBetween(cmds, 1000)
        }
    
}

def off() {
	
    setThermostatMode(off)
    
}

def on() {
	
    setThermostatMode(heat)
    
}

def heat() {
	    sendEvent(name: "thermostatMode", value: "heat")
        sendEvent(name: 'switch', value: "on" as String)
		delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        zwave.configurationV2.configurationGet(parameterNumber: 10).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 11).format()
	], 1000)
}

def energySaveHeat() {
	    sendEvent(name: "thermostatMode", value: "energySaveHeat")
        sendEvent(name: 'switch', value: "on" as String)
		poll()
        delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 11).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        zwave.configurationV2.configurationGet(parameterNumber: 10).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 11).format()
	], 1000)
	
}

/*
def cool() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 2).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}
*/
def auto() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 3).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

private getStandardDelay() {
	1000
}

private secureSequence(commands, delay=200) {
	//log.debug "$commands"
	delayBetween(commands.collect{ secure(it) }, delay)
	
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}
