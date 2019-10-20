from flask import Flask
from flask_cors import CORS
import math
from flask import jsonify, request
import googlemaps
import os

PORT = 8000
API_KEY = os.environ['API_KEY1']
print(API_KEY)
app = Flask(__name__)
CORS(app)

@app.route('/grid_data', methods = ['POST'])
def grid_data(AK=API_KEY):
    gmaps = googlemaps.Client(key = AK)
    data_in = request.json['data']
    print(data_in)
    place = gmaps.places(data_in)
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
    return jsonify(result_list)

if __name__ == '__main__':
    app.run(port=PORT, debug=False)
