'''
Created April 20 2016

Using Python 2.7.11

Notes: Risk levels are based on an increase from the average
CVS format: ug/L,hours,temp,humidity%

classificationLevels = ["No risk or low risk", "Risk of acute onset", "Risk of long term exposure"]
meanAnnual = 15 #ug/L
mean24hr = 35 #ug/L
acuteRisk = 20 #ug/L 
acuteRiskTimeLow = 24 #hours 
acuteRiskTimeHigh = 24*5 #hours = 5 days 
longTermRisk = 10 #ug/L 
longTermRiskTimeLow = 24*365 #hours = 1 year
longTermRiskTimeHigh = 24*365*4 #hours = 4 years 

@author Alexis Burnight 
'''

import csv, argparse
import numpy as np
import matplotlib.pyplot as plt
#from mpl_toolkits.mplot3d import Axes3D
#from sklearn.cluster import KMeans
#from sklearn import datasets #not needed 
from sklearn import svm, linear_model

parser = argparse.ArgumentParser(description='Clustering of Particulate Matter Data for Cardiovascular Disease')
parser.add_argument('-m', '--model', action='store', required=True, help='CSV file with data for clustering training')
#parser.add_argument('-n', '--clusters' action='store', type=int, required=True, help='Number of clusters to create')
parser.add_argument('-c', '--classification', action='store', required=True, help='CSV file with classification values of the data model')
parser.add_argument('-r', '--runs', action='store', type=int, default=10, help='Number of k-means iterations')
parser.add_argument('-t', '--test', action='store_true', help='Flag set for test set')
parser.add_argument('-o', '--output', action='store', help='Destination for output of classification')
parser.add_argument('-d', '--data', action='store', help='CSV file with test data')
parser.add_argument
args = parser.parse_args()

dataRead = []
classRead = []

classificationLevels = ["No risk or low risk", "Risk of acute onset", "Risk of long term exposure"]

with open(args.model, 'r') as model, open(args.classification, 'r') as classification: 
		model_reader = csv.reader(model, delimiter=',')
		class_reader = csv.reader(classification)
		for row in model_reader:
			dataRead.append(row)
		for row in class_reader: 
			classRead.append(row[0])
		#print(classRead)

clf = svm.SVC()
clf.probability = True
clf.fit(dataRead,classRead)

if(args.test):
	with open(args.data, 'r') as test, open(args.output, 'w') as output: 
		test_reader = csv.reader(test, delimiter=',')
		output_writer = csv.writer(output,delimiter='\t')

		for row in test_reader: 
			storeRow = row
			prediction = clf.predict(row[1:])
			print('%')
			probab =  clf.predict_proba(row[1:])
			decisions = clf.decision_function(row[1:])
			print probab
			storeRow.append(prediction[0])
			storeRow.append(probab[0][0]) 
			storeRow.append(probab[0][1]) 
			storeRow.append(probab[0][2])
			storeRow.append(decisions[0][0]) 
			storeRow.append(decisions[0][1])
			storeRow.append(decisions[0][2])
			output_writer.writerow(storeRow)

print(clf)


x1 = np.array(dataRead).astype(np.float)
X = x1[:,:2]
Y = np.array(classRead).astype(np.float)
clf2 = svm.SVC()
clf2.probability=True
clf2.fit(X,Y)
h = 2 #step 
'''

# Plot the decision boundary. For that, we will assign a color to each
# point in the mesh [x_min, m_max]x[y_min, y_max].
x_min, x_max = X[:, 0].min() - 1, X[:, 0].max() + 1
y_min, y_max = X[:, 1].min() - 1, X[:, 1].max() + 1
print(x_min, x_max, y_min, y_max)
print('$')

xx, yy = np.meshgrid(np.arange(x_min, x_max, h), np.arange(y_min, y_max, h))
print(xx.shape)

Z = clf2.predict(np.c_[xx.ravel(), yy.ravel()]).astype(np.float)
print(Z)

# Put the result into a color plot
Z = Z.reshape(xx.shape)
#plt.pcolormesh(xx, yy, Z, cmap=plt.cm.Paired)

# Plot also the training points
plt.scatter(X[:, 0], X[:, 1], c=Y, cmap=plt.cm.Paired)
plt.title('2D Classification Map CVD and PM2.5')
plt.axis('tight')
plt.show()
'''
logreg = linear_model.LogisticRegression(C=1e5)

# we create an instance of Neighbours Classifier and fit the data.
logreg.fit(X, Y)

# Plot the decision boundary. For that, we will assign a color to each
# point in the mesh [x_min, m_max]x[y_min, y_max].
x_min, x_max = X[:, 0].min() - .5, X[:, 0].max() + .5
y_min, y_max = X[:, 1].min() - .5, X[:, 1].max() + .5
xx, yy = np.meshgrid(np.arange(x_min, x_max, h), np.arange(y_min, y_max, h))
Z = logreg.predict(np.c_[xx.ravel(), yy.ravel()])

# Put the result into a color plot
Z = Z.reshape(xx.shape)
plt.figure(1, figsize=(4, 3))
plt.pcolormesh(xx, yy, Z, cmap=plt.cm.Paired, alpha=.8)

# Plot also the training points
plt.scatter(X[:, 0], X[:, 1], c=Y, edgecolors='k', cmap=plt.cm.Paired)
plt.xlabel('PM2.5 Density')
plt.ylabel('Exposure Time')

plt.xlim(xx.min(), xx.max())
plt.ylim(yy.min(), yy.max())
plt.xticks(())
plt.yticks(())

plt.show()
