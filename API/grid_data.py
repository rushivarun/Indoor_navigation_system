import googlemaps
import pprint
import math
import os

API_KEY = os.environ['API_KEY1']
gmaps = googlemaps.Client(key = API_KEY)

place = gmaps.places('SMV, VIT University')
latitude = place['results'][0]['geometry']['location']['lat']
longitude = place['results'][0]['geometry']['location']['lng']

r_earth = 6378
count = 0
result_list = []
for i in range(-30, 30, 2):
    for j in range(-30, 30, 2):
        new_latitude  = latitude  + (i/1000 / r_earth) * (180 / math.pi)
        new_longitude = longitude + (j/1000 / r_earth) * (180 / math.pi) / math.cos(latitude * math.pi/180)
        count += 1
        if new_latitude > latitude and new_longitude > longitude:
            quad = 1
        elif new_latitude < latitude and new_longitude > longitude:
            quad = 2
        elif new_latitude < latitude and new_longitude < longitude:
            quad = 3
        elif new_longitude > latitude and new_longitude < longitude:
            quad = 4
        result = {
            'gid' : count,
            'lat' : new_latitude,
            'lng' : new_longitude,
            'quad' : quad
        }
        result_list.append(result)

pprint.pprint(result_list)
