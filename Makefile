all: server client

server: Server.class

client: Client.class

%.class: %.java
	javac $<

clean:
	rm *.class
