import glob
import matplotlib.pyplot as plt

csvfile = glob.glob('histos.csv')

data = []

first = True
f = open('histos.csv', 'r')
plot = 0
label = ''
data.append({'values': [], 'm': []})
for line in f:
    line = line.strip()
    if line == '':
        plt.figure(plot)
        x = []
        y = []
        for m in data[plot]['m']:
            x.append(m['rssi'])
            y.append(m['freq'])
        #plt.plot(x,y,label=label)
        bins = []
        for i in range(min(data[plot]['values']),max(data[plot]['values'])+1):
            bins.append(i-0.5)
        bins.append(max(data[plot]['values'])+0.5)
        plt.hist(data[plot]['values'], bins)
        plt.title(label)
        
        first = True
        data.append({'values': [], 'm': []})
        plot += 1
    elif first == True:
        label = line
        first = False
    else:
        rssi,freq = line.split(",")
        for i in range(0, int(float(freq))):
            data[plot]['values'].append(int(rssi))
        data[plot]['m'].append({'rssi': int(rssi), 'freq': float(freq)})
plt.show()

# fileC = 0
# for csvfile in csvfiles:
    # plt.figure(fileC)
    # f = open(csvfile, 'r')
    # data.append({})
    # for line in f:
        # line.strip('\n')
        # ts,ssid,bssid,rssi = line.split(",")
        # if data[fileC].has_key(bssid):
            # data[fileC][bssid].append({'ts': int(ts), 'ssid': ssid, 'rssi': int(rssi)})
        # else:
            # data[fileC][bssid] = [{'ts': int(ts), 'ssid': ssid, 'rssi': int(rssi)}]
    
    # for bssid,measurements in data[fileC].iteritems():
        # x  = []
        # y = []
        # for measurement in measurements:
            # x.append(measurement['ts']/1000)
            # y.append(measurement['rssi'])
            
        # plt.plot(x,y,label=measurement['ssid'])
        
    # plt.legend()
    # fileC += 1
# plt.show()