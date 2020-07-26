When the project is imported in eclipse
1) Set the parameter for the 3 executables by:
	a) Go to run configurations
	b) In the arguements section
	b) For server: ClientPortNumber 
		       NumOfWorkers 
		       WorkerPortNumber
	c) For Client: ServerIP(127.0.0.1)
		       ClientPortNumber
	d) For Worker: ServerIP(127.0.0.1)
		       WorkerPortNumber
2) Now run MyCharFreqServer
3) Now run MyCharFreqWorker equal to the numOfWorkers (i.e. if number of workers is 5, then, press run 5 times)
4) Now run MyCharFreqClient	
5) Now follow client console output for client to send requests