from flask import Flask
from math import sin, cos, atan2, sqrt, pi
from flask import jsonify, request
import googlemaps
import os
import pandas as pd

PORT = 8000
app = Flask(__name__)
r_earth = 6378
def get_distance(slat, slng, vlat, vlng):

    dlon = vlng - slng
    dlat = vlat - slat
    a = (sin(dlat/2))**2 + cos(slat) * cos(vlat) * (sin(dlon/2))**2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    distance = r_earth * c
    
    return distance

@app.route('/grid_data', methods = ['POST'])
def grid_data():
    data_in = request.json['data']
    print(data_in)
    latitude = request.json['data']['lat']
    longitude = request.json['data']['lng']
    count = 0
    result_list = []
    for i in range(-30, 30, 2):
        for j in range(-30, 30, 2):
            new_latitude  = latitude  + (i/1000 / r_earth) * (180 / pi)
            new_longitude = longitude + (j/1000 / r_earth) * (180 / pi) / cos(latitude * pi/180)
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
    return jsonify(result_list)

@app.route('/grid_id', methods = ["POST"])
def grid_id():
    data_in = request.json['data']
    user_lat = request.json['data']['user_lat']
    user_lng = request.json['data']['user_lng']
    home_lat = request.json['data']['home_lat']
    home_lng = request.json['data']['home_lng']
    count = 0
    result_list = []
    for i in range(-30, 30, 2):
        for j in range(-30, 30, 2):
            new_latitude  = home_lat  + (i/1000 / r_earth) * (180 / pi)
            new_longitude = home_lng + (j/1000 / r_earth) * (180 / pi) / cos(home_lat * pi/180)
            count += 1
            if new_latitude > home_lat and new_longitude > home_lng:
                quad = 1
            elif new_latitude < home_lat and new_longitude > home_lng:
                quad = 2
            elif new_latitude < home_lat and new_longitude < home_lng:
                quad = 3
            elif new_longitude > home_lat and new_longitude < home_lng:
                quad = 4
            result = {
                'gid' : count,
                'lat' : new_latitude,
                'lng' : new_longitude,
                'quad' : quad
            }
            result_list.append(result)
    get_df = pd.DataFrame(result_list) 
    if user_lat > home_lat and user_lng > home_lng:
        quad = 1
    elif user_lat < home_lat and user_lng > home_lng:
        quad = 2
    elif user_lat < home_lat and user_lng < home_lng:
        quad = 3
    elif user_lat > home_lat and user_lng < home_lng:
        quad = 4
    user_range = get_df.loc[get_df['quad'] == quad]
    user_range = user_range.reset_index()
    user_range = user_range.drop(['index'], axis = 1)
    min_distance = {
    "gid" : 0,
    "distance" : 10000
    }
    for i in range(user_range.shape[0]):
        slat = user_range['lat'][i]
        slng = user_range['lng'][i]
        gid = user_range['gid'][i]
        dist = get_distance(slat, slng, user_lat, user_lng)
        if min_distance['distance'] > dist:
            min_distance = {
                "gid" : gid,
                "distance" : dist
            }
    return jsonify(str(min_distance))




if __name__ == '__main__':
    app.run(port=PORT, debug=True)
