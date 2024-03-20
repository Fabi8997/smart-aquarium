# smart-aquarium
Repository containing the project for the course on Internet of Things (IOT) at the University of Pisa.<br>
All the details are explained in the project documentation, the requirements in the project specifications and the commend is well commented.
The aim is to create an **intelligent aquarium** that maintains the values of substances in the water at an optimal value for the life of the living creatures inside (aquatic plants and fishes).<br>
Keeping these values constant is of crucial importance for the life inside the aquarium, because abrupt fluctuations in these values could lead to diseases or even death for the species. These values are monitored by sensors inside the aquarium and are altered by the release of substances into the water, such as CO2 or osmotic water. The aquarium provides also the possibility to keep the temperature of the water at a
certain value since it’s very important for the internal ecosystem.

## Project structure

![architectureIOT](https://github.com/Fabi8997/smart-aquarium/assets/83593602/500ef1b1-2089-4a12-bcd6-2da118b358f7)

The project is composed by these main modules:

- *MQTT Network*
- *CoAP Network*
- *Smart aquarium application*

## How to start the system
The starting assumption is that the **Ubuntu VM**, with the **Contiki-ng OS container** and the **cooja simulator**, is correctly installed (it is suggested to view and install all the components presented during the lab sessions).

### Step 1
**Clone** the repository inside the **contiki-ng** folder.<br>
Move to the ==\smart-aquarium\smart-aquarium-java-app\== folder.<br>
Run the following commands: `mvn package` and `java -jar target/smart-aquarium-java-app-0.0.1-SNAPSHOT.jar` to build and start the application.

### Step 2
Run the **contiki-ng container** using docker.<br>
Start the **cooja** simulator.<br>
**As first** device add the **rpl-border-router**, present inside the `contiki-ng/examples/rpl-border-router/` folder.<br>
**Then** deploy a **single** device per type. The devices code can be found inside their relative folders: `MQTT-network` and `CoAP-network`.<br>
Before starting the system the **border router**:<br>
  -Add the socket on the BR: `Tools` -> `Serial Socket (SERVER)` -> `Contiki 1` –> `Press START`<br>
  -Open a new termianl and move to the folder `contiki-ng/examples/rpl-border-router/` and execute the command `make TARGET=cooja connect-router-cooja`<br>

### Step 3
Import the **smart_aquarium** db on *mysql*.

### Step 4
Put the speed of the simulation on **1x** and start the simulation.

### To close the app
Issue the command **:quit** to stop the application.
