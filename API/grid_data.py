import googlemaps
import pprint
import math

API_KEY = "AIzaSyC5zveAthZA233R3pZgUHFhGo68ND6AHUA"
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
        result = {
            'gid' : count,
            'lat' : new_latitude,
            'lng' : new_longitude
        }
        result_list.append(result)

pprint.pprint(result_list)