The server must be operated through a TUI. You simply enter a line in the standard input and it will be parsed and executed.
The server application can be launched using the following command from the root directory (which contains the server/shared folders):

java -cp "server/bin:shared/bin" server.Main

The server should now be running, but is not yet listening for clients.
To do this enter the following command in the standard input:

start PORT

where port is a valid integer between 0 and 65536.

To stop listening you can simply type:

stop

this will disconnect all clients and stops the server from accepting new clients. You can then call the start command again as above
to begin listening for clients again.

To exit the application simply type:

quit

Other commands:

setLogLevel LEVEL (where level is either 'off', 'minimal', 'normal' or 'verbose'). This will set the current log level.
debugMode BOOL (where bool is either 'true' or 'false'). This will enable/disable the debug mode.
listClients This will list all connected clients and some basic information about their state.
listGames This will list all active games.
clientInfo NAME (where name is the name of the client). This will show detailed information about the client's state.