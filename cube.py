import red
import network


training_data, test_data = red.gain()
net = network.Network([8,10,2])
net.SGD(training_data, 100, 100,0.05,test_data=test_data)