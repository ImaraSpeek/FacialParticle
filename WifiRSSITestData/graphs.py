import glob
import matplotlib.pyplot as plt

csvfiles = glob.glob('loc*.csv')

data = []
fileC = 0
for csvfile in csvfiles:
    plt.figure(fileC)
    f = open(csvfile, 'r')
    data.append({})
    for line in f:
        line.strip('\n')
        ts,ssid,bssid,rssi = line.split(",")
        if data[fileC].has_key(bssid):
            data[fileC][bssid].append({'ts': int(ts), 'ssid': ssid, 'rssi': int(rssi)})
        else:
            data[fileC][bssid] = [{'ts': int(ts), 'ssid': ssid, 'rssi': int(rssi)}]
    
    for bssid,measurements in data[fileC].iteritems():
        x  = []
        y = []
        for measurement in measurements:
            x.append(measurement['ts']/1000)
            y.append(measurement['rssi'])
            
        plt.plot(x,y,label=measurement['ssid'])
        
    plt.legend()
    fileC += 1
plt.show()