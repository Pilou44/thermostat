import network
import socket
import time
import time
import struct
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
led_R_pin = machine.Pin(26, mode=machine.Pin.OUT, value=1)
led_G_pin = machine.Pin(22, mode=machine.Pin.OUT, value=1)

ds18b20_connected = False
dht11_connected = False

dht11_sensor = dht.DHT11(dht11_pin)

ds18b20_sensor = ds18x20.DS18X20(onewire.OneWire(ds18b20_pin))
ds18b20_rom = 0

NTP_DELTA = 2208988800
host = "fr.pool.ntp.org"
zone = "Europe/Paris"
key = "WX0HCD6C4R8W"

def get_time_offset():
    print(f'get_time_offset')
    try:
        response = urequests.get(f"http://api.timezonedb.com/v2.1/get-time-zone?key={key}&format=json&by=zone&zone={zone}")
        parsed = ujson.loads(response.text)
        response.close()
        offset = parsed['gmtOffset']
        print(f'Time offset in s: {offset}')
        return offset
    except OSError:
        print('Connection error')
        return 0

def set_time():
    global hour
    offset = get_time_offset()
    NTP_QUERY = bytearray(48)
    NTP_QUERY[0] = 0x1B
    addr = socket.getaddrinfo(host, 123)[0][-1]
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.settimeout(1)
        res = s.sendto(NTP_QUERY, addr)
        msg = s.recv(48)
    finally:
        s.close()
    val = struct.unpack("!I", msg[40:44])[0]
    t = val - NTP_DELTA + offset
    tm = time.gmtime(t)
    print(f'set_time {tm}')
    machine.RTC().datetime((tm[0], tm[1], tm[2], tm[6] + 1, tm[3], tm[4], tm[5], 0))
    currentTime = time.localtime()
    hour = currentTime[3]

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

def readTemperature():
    if dht11_connected:
        dht11_sensor.measure()
        temperature = dht11_sensor.temperature()
        humidity =  dht11_sensor.humidity()
    elif ds18b20_connected:
        ds18b20_sensor.convert_temp()
        time.sleep_ms(750)
        temperature = ds18b20_sensor.read_temp(ds18b20_rom)
        humidity = float(-1)
    else:
        temperature = float(-1)
        humidity = float(-1)
    
    saveTemperature(temperature)
    saveHumidity(humidity)
    return temperature

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

def saveHumidity(humidity):
    print("Save current humidity")
    path = f'thermostats/{uniqueId}/humidity'
    firebase.put(path, humidity, bg=False, id=1)

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
    led_R_pin.low()
    global uniqueId
    uniqueId = getId()
    print(f'Unique id: {uniqueId}')
    has_sensor = initTemperature()
    if not has_sensor:
        print("No temperature sensor")
        return False
    wlan.active(True)
    initFirebase()
    led_G_pin.low()
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
        led_R_pin.low()
        connect()
    set_time()
    while wlan.isconnected():
        led_R_pin.high()
        currentTemperature = readTemperature()
        print(f'Sensor temperature: {currentTemperature}')
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
        gc.collect()
        gc.mem_free()
        time.sleep(1)
    run()

if init():
    while True:
        try:
            run()
        except KeyboardInterrupt:
            machine.reset()
        except:
            pass
