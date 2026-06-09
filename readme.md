# Gesyca Commander

### The main software in Java is commander that can receive requisitions in the following endpoints:
**POST /boot** - expecting a json containing *String hostname*, *String ipAddress*, *int ID*, *String macAddress*. It registers the machine.
**POST /command/{id}** or **/command/all** - which expects  a json containing *String command*, *String password*. It executes the commands (separed by \n) in the machines and returns the outputs formatted.
**POST /clear** - will clear all machines
**GET /hostsr** - will return a raw json of all registered machines
**GET /hostsf** - will return a formatted String with all registered machines

### To set-up a machine, you don't need to send the /boot requisition on your own. Just use the "startup.sh" script once.
When executed in a debian-based OS, the script will create a rule to execute a Python file (that should be downloaded automatically) on every boot. The Python file sends the needed information to the */boot* endpoint (in a IP address that you must specify, editing the Python file).
After the script is executed once, feel free to delete it and use the */hostsr* or */hostsf* to check if the machine was indeed registered.
