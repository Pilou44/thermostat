import network
import time
from picozero import pico_temp_sensor, pico_led
import machine
import onewire
import ds18x20
import dht
import ufirebase as firebase
import ujson
import urequests
import gc

uniqueId = ""

wlan = network.WLAN(network.STA_IF)

ssid = 'WIFI_BN'
password = 'wF4y4H2Urr3M'

dht11_pin = machine.Pin(27)
ds18b20_pin = machine.Pin(28)

ds18b20_connected = False
dht11_connected = False

dht11_sensor = dht.DHT11(dht11_pin)

ds18b20_sensor = ds18x20.DS18X20(onewire.OneWire(ds18b20_pin))
ds18b20_rom = 0

def connect():
    wlan.connect(ssid, password)
    while wlan.isconnected() == False:
        print('Waiting for connection...')
        time.sleep(1)
    ip = wlan.ifconfig()[0]
    print(f'Connected on {ip}')
    return ip

def getDay(val):
    if val == 0:
        return 'monday'
    elif val == 1:
        return 'tuesday'
    elif val == 2:
        return 'wednesday'
    elif val == 3:
        return 'thursday'
    elif val == 4:
        return 'friday'
    elif val == 5:
        return 'saturday'
    elif val == 6:
        return 'sunday'

def initTemperature():
    global dht11_connected
    global ds18b20_connected
    
    try:
        dht11_sensor.measure()
        print("DHT11 connected")
        dht11_connected = True
    except OSError as e:
        print("DHT11 not connected")
        dht11_connected = False
    
    global ds18b20_rom
    roms = ds18b20_sensor.scan()
    if roms:
        ds18b20_rom = roms[0]
        print("DS18B20 connected")
        ds18b20_connected = True
    else:
        print("DS18B20 not connected")
        ds18b20_connected = False
    
    return ds18b20_connected or dht11_connected

def getTemperature():
#     global dht11_connected
    global ds18b20_connected
    
    ds18b20_sensor.convert_temp()
    time.sleep_ms(750)
    temperature = ds18b20_sensor.read_temp(ds18b20_rom)
    print(f'18b20: {temperature}')
    dht11_sensor.measure()
    temperature = dht11_sensor.temperature()
    print(f'dht11: {temperature}')
    print(f"Humidite    : {dht11_sensor.humidity():.1f}")
    return temperature
    
#     if ds18b20_connected:
#         ds18b20_sensor.convert_temp()
#         time.sleep_ms(750)
#         return ds18b20_sensor.read_temp(ds18b20_rom)
# #     elif dht11_connected:
# #         dht11_sensor.measure()
# #         # Récupère les mesures du capteur
# #         temperature = dht11_sensor.temperature()
# #         print(f"Humidite    : {dht11_sensor.humidity():.1f}")
# #         return temperature
#     else:
#         return NaN

def initFirebase():
    firebase.setURL("https://thermostat-4211f-default-rtdb.europe-west1.firebasedatabase.app/")

def getId():
    id = machine.unique_id()
    stringId = ""
    for b in id:
        stringId += hex(b)[2:]
    return stringId

def saveTemperature(temperature):
    print("Save current temperature")
    path = f'thermostats/{uniqueId}/temperature'
    firebase.put(path, temperature, bg=False, id=1)

def setStatusOn(on):
    print(f"Set status on: {on}")
    path = f'thermostats/{uniqueId}/on'
    firebase.put(path, on, bg=False, id=1)
    
def saveTime():
    currentTime = time.localtime()
    hours = currentTime[3]
    minutes = currentTime[4]
    day = currentTime[6]
    print(f'Time: {hours}:{minutes}')
    print(f'Day: {getDay(day)}')
    path = f'thermostats/{uniqueId}/time'
    value = f'{day}-{hours}-{minutes}'
    firebase.put(path, value, bg=False, id=1)

def isOn():
    path = f'commands/{uniqueId}/powerOn'
    firebase.get(path, "VAR1", limit=True, bg=False)
    return firebase.readValue("VAR1")

def init():
    global uniqueId
    uniqueId = getId()
    print(f'Unique id: {uniqueId}')
    has_sensor = initTemperature()
    if not has_sensor:
        print("No temperature sensor")
        return False
    wlan.active(True)
    initFirebase()
    return True

def getMode():
    path = f'commands/{uniqueId}/mode'
    firebase.get(path, "VAR1", limit=True, bg=False)
    return firebase.readValue("VAR1")

def getManualTemperature():
    path = f'commands/{uniqueId}/manualTemperature'
    firebase.get(path, "VAR1", limit=True, bg=False)
    return firebase.readValue("VAR1")

def getDayTemperature():
    path = f'commands/{uniqueId}/automaticTemperatureDay'
    firebase.get(path, "VAR1", limit=True, bg=False)
    return firebase.readValue("VAR1")

def getNightTemperature():
    path = f'commands/{uniqueId}/automaticTemperatureNight'
    firebase.get(path, "VAR1", limit=True, bg=False)
    return firebase.readValue("VAR1")

def getAutomaticTemperature():
    currentTime = time.localtime()
    hour = currentTime[3]
    day = currentTime[6]
    path = f'commands/{uniqueId}/automaticTemperatures/{day}/{hour}'
    firebase.get(path, "VAR1", limit=True, bg=False)
    isDay = firebase.readValue("VAR1")
    if isDay:
        return getDayTemperature()
    else:
        return getNightTemperature()

def getNeededTemperature():
    mode = getMode()
    print(f'Mode: {mode}')
    if mode == "MANUAL":
        return getManualTemperature()
    else:
        return getAutomaticTemperature()

def getSwitches():
    path = f'switches'
    firebase.get(path, "VAR1", limit=False, bg=False)
    switches = firebase.readValue("VAR1")
    filtered = []
    for address in switches:
        switch = switches[address]
        switchType = switch['type']
        paired = switch['pairedDeviceId'] == uniqueId
        if paired:
            filtered.append(f"""{{"address":"{address.replace("-", ".")}", "type":"{switchType}"}}""")
        return filtered

def switchShellyP1On(address, on):
    print(f'Switch on Shelly @ {address}')
    try:
        response = urequests.get(f"http://{address}/rpc/Switch.GetStatus?id=0")
        parsed = ujson.loads(response.text)
        print(f'parsed: {parsed}')
        response.close()
        output = parsed['output']
        source = parsed['source']
        print(f'Current ouput: {output}')
        if output == True and source == "switch":
            print(f'Forced on by switch')
            return
        elif output != on:
            print(f'Toggle switch to {on}')
            if on:
                value = "true"
            else:
                value = "false"
            response = urequests.get(f"http://{address}/rpc/Switch.Set?id=0&on={value}")
            print(f'response: {response.text}')
            response.close()
        else:
            print(f'Keep status {on}')
    except OSError:
        print('Connection error')

def turnOn(on):
    switches = getSwitches()
    for switch in switches:
        print(f'switch: {switch}')
        parsed = ujson.loads(switch)
        if parsed['type'] == 'SHELLY_PLUS_1':
            address = parsed['address']
            switchShellyP1On(address, on)
    setStatusOn(on)

def run():
    while not wlan.isconnected():
        connect()
    while wlan.isconnected():
        print()
        gc.collect()
        print(gc.mem_free())
        print()
        currentTemperature = getTemperature()
        print(f'Sensor temperature: {currentTemperature}')
        saveTemperature(currentTemperature)
        if isOn():
            neededTemperature = getNeededTemperature()
        else:
            neededTemperature = 5
        print(f'Needed temperature = {neededTemperature}')
        if currentTemperature < neededTemperature:
            print('Turn on')
            turnOn(True)
        else:
            print('Turn off')
            turnOn(False)
        saveTime()
        coreTemperature = pico_temp_sensor.temp
        print(f'Core temperature is {coreTemperature}')
        time.sleep(1)
    run()

try:
    if init():
        run()
except KeyboardInterrupt:
    machine.reset()
