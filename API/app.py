from flask import Flask
from flask_cors import CORS
import math
from flask import jsonify, request
import googlemaps
import os

PORT = 8000
app = Flask(__name__)
CORS(app)

@app.route('/grid_data', methods = ['POST'])
def grid_data():
    data_in = request.json['data']
    print(data_in)
    latitude = request.json['data']['lat']
    longitude = request.json['data']['lng']

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
    return jsonify(result_list)

if __name__ == '__main__':
    app.run(port=PORT, debug=False)
