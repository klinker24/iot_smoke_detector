'''
Created April 26 2016

Using Python 3.3 <- required for urllib.request
Gathers data fromt endpoint and outputs to test file for classification

@author Alexis Burnight 
'''
# Average particle exposure for entire data set for each household

import urllib.request as rq 
import argparse, csv,json

parser = argparse.ArgumentParser(description='Reading data from webpage')
parser.add_argument('-w', '--webpage', action='store', required=True, help='URL for webdata')
parser.add_argument('-o', '--output', action='store', required=True, help='Destination for output file from the webpage')
parser.add_argument('-r', '--rate', action='store', default=.5, type=float, help='Sampling rate in minutes')
args = parser.parse_args()

page = rq.urlopen(args.webpage)
#pageData = page.read().decode('utf-8')
pageData = json.loads(page.read().decode('utf-8'))
items = pageData['items']

with open(args.output, 'w') as csvFile: 
	writer = csv.writer(csvFile, delimiter=',')
	accounts = []
	for i in range(0,len(items)): 
		if items[i]['account'] not in accounts: 
			accounts.append(items[i]['account'])
	data = []
	for j in range(0,len(accounts)):
		data.append([])
		
	for k in range(0,len(items)): 
		
		idx = accounts.index(items[k]['account'])
		data[idx].append([items[k]['particleDensity'], items[i]['temperature'], items[i]['relativeHumidity']])
		
		#tmp = []
		#tmp.append(items[i]['particleDensity'])
		#tmp.append()
	#print(data)

	''' 
	Data set created, generated test file 
	'''
	avg_density = 0 
	avg_temperature = 0 
	avg_humidity = 0
	for l in range(0,len(accounts)): 
		for m in range(0,len(data[l])): 
			total_time = args.rate*len(data[l])/60 #hours
			acuteTime = 4 #hours
			chronicTime = 760 #hours

			avg_density += int(data[l][m][0])
			avg_temperature += int(data[l][m][1])
			avg_humidity += int(data[l][m][2])

		avg_density = avg_density/len(data[l])
		avg_temperature = avg_temperature/len(data[l])
		avg_humidity = avg_humidity/len(data[l])

	writer.writerow([accounts[l],avg_density,total_time,avg_temperature,avg_humidity])