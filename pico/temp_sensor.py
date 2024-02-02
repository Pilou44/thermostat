import network
import socket
import time
import machine
import onewire
import ds18x20
import dht
import json

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

def connect():
    wlan.connect(ssid, password)
    while wlan.status() == 1: #STAT_CONNECTING
        print('Waiting for connection...')
        time.sleep(1)
    if wlan.status() != 3:
        print('Failed.')
        time.sleep(1)
        return False
    ip = wlan.ifconfig()[0]
    print(f'Connected on {ip}')
    return True

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
    temperature = float(-1)
    if ds18b20_connected:
        ds18b20_sensor.convert_temp()
        time.sleep_ms(750)
        return ds18b20_sensor.read_temp(ds18b20_rom)
    elif dht11_connected:
        temp = dht11_sensor.measure()
        return dht11_sensor.temperature()
    else:
        return float(-1)

def readHumidity():
    if dht11_connected:
        return dht11_sensor.humidity()
    else:
        return -1

def getId():
    id = machine.unique_id()
    stringId = ""
    for b in id:
        stringId += hex(b)[2:]
    return stringId

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
    led_G_pin.low()
    return True

def run():
    print('Start')
    led_R_pin.low()
    while not wlan.isconnected():
        connect()

    led_R_pin.high()

    # Open a socket
    addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]

    s = socket.socket()
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(addr)
    s.listen(1)

    # Display your IP address
    print('listening on', addr)

    while wlan.isconnected():
        try:
            cl, addr = s.accept()
            print('client connected from', addr)
            request = cl.recv(1024)
            request = str(request)
            print(request)

            #Get the measurements from the sensor
            temperature = readTemperature()
            print(f"Temperature: {temperature}°C")
            humidity = readHumidity()
            print(f"Humidity: {humidity}%")

            # prep the data to send to Home Assistant as type Json
            if humidity == -1:
                data = { "temp": temperature }
            else:
                data = { "temp": temperature, "hum": humidity }
            JsonData = json.dumps(data)

            # Send headers notifying the receiver that the data is of type Json for application consumption 
            cl.send('HTTP/1.0 200 OK\r\nContent-type: application/json\r\n\r\n')
            # Send the Json data
            cl.send(JsonData)
            # Close the connection
            cl.close()

        except OSError as e:
            cl.close()
            print('connection closed')

    run()

if init():
    while True:
        try:
            run()
        except KeyboardInterrupt:
            machine.reset()
        except Exception as e:
            print(e)
            pass
