import network
import socket
import time
import machine
import onewire
import ds18x20
import dht

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

def webpage(temperature):
    html = f"""
        <!DOCTYPE html>
        <html>
        <head>
        <title>Pico DHT22</title>
        </head>
        <body>
            <h1>Pico W - DHT22</h1>
            <font size="+2">
            <p>Temperature: {temperature}C</p>
            </font>
        </body>
        </html>
        """
    return str(html)

def connect():
    wlan.connect(ssid, password)
    while wlan.status() == 1: #STAT_CONNECTING
        print('Waiting for connection...')
        time.sleep(1)
    if wlan.status() != 3:
        print('Failed. Retry')
        return connect()
    ip = wlan.ifconfig()[0]
    print(f'Connected on {ip}')
    return ip

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
    return temperature

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
    while not wlan.isconnected():
        led_R_pin.low()
        connect()

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

            # display the webpage for the customer
            html = webpage(temperature)
            cl.send('HTTP/1.0 200 OK\r\nContent-type: text/html\r\n\r\n')
            cl.send(html)
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
#         except Exception as e:
#             print(e)
#             pass
