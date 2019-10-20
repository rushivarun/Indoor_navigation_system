import random
import pandas
import numpy
c = 0
c_list = []
mac = []
for i in range(1000):

    c = c+1
    c_list.append(c)
    mac.append(float(("d3:e4:{}{}:{}{}:{}{}:{}{}").format(random.randint(1,9),random.randint(1,9),random.randint(1,9),random.randint(1,9),
    random.randint(1,9),random.randint(1,9),random.randint(1,9),random.randint(1,9))))

d = {"mac":mac, "grid_id":c_list}
df = pandas.DataFrame(d)

