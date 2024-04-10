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

## Deployment on real sensors (3 nrf52840-dongle)

### Step 1: Database
Import the **smart_aquarium** db on *mysql*.<br>
*NOTE: If possible, delete all the records inside each of the 7 tables.*

### Step 2: Repository
**Clone** the repository inside the **contiki-ng** folder.<br>
Move to the `\smart-aquarium\smart-aquarium-java-app\` folder.<br>
Run the command `mvn package` to build the application.

### Step 3: Compile and flash the code in the dongles
  **a)** Start the contiki-ng container with docker.<br>
  **b)** Move to the `contiki-ng/examples/rpl-border-router/` folder.<br>
  **c)** Insert the dongle inside the **USB port** and press the **reset byutton** until the red led starts to blink.<br>
  **d)** Plug the device inside the VM (On virtual box click on the **devices -> USB** tab and then click on the device name).<br>
  **e)** **Compile** the code running the following command: `make TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM0 border-router.dfu-upload`; after the finish perform the **d)** point again.<br>
  **f)** To start the **border router** run the command `make TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM0 connect-router` *NOTE: ttyACMX is just the port on which the USB device is inserted, change the X with the correct number* (to see the devices run `ls /dev/ttyACM*` ).<br>
  **g)** **Open** a new terminal (docker -exec ...), **Move** to the `contiki-ng/smart-aquarium/MQTT-network/mqtt-device/` and perform the steps **c)** and **d)** for the second device. **Compile** the code running the command: `make TARGET=nrf52840 BOARD=dongle mqtt-device.dfu-upload PORT=/dev/ttyACM1` and perform again the step **d)**.<br>
  **h)** To see the **output** of the device run the command: `make login TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM1`.<br>
  **i)** **Open** a new terminal (docker -exec ...), **Move** to the `contiki-ng/smart-aquarium/CoAP-network/coap-device/` and perform the steps **c)** and **d)** for the third device. **Compile** the code running the command: `make TARGET=nrf52840 BOARD=dongle coap-device.dfu-upload PORT=/dev/ttyACM2` and perform again the step **d)**.<br>
  **j)** To see the **output** of the device run the command: `make login TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM2`.<br>

### Step 4: Run the Smart Aquarium Application
From a new terminal **outside the container**, move to the `\smart-aquarium\smart-aquarium-java-app\` folder and run the following command to start the app: `java -jar target/smart-aquarium-java-app-0.0.1-SNAPSHOT.jar`. 

## Simulation with cooja

### Step 1
**Clone** the repository inside the **contiki-ng** folder.<br>
Move to the `\smart-aquarium\smart-aquarium-java-app\` folder.<br>
Run the following commands: `mvn package` and `java -jar target/smart-aquarium-java-app-0.0.1-SNAPSHOT.jar` to build and start the application.

### Step 2
Run the **contiki-ng container** using docker.<br>
Start the **cooja** simulator.<br>
Is possible to skip the remaining steps of **step 2**, by opening the `FinalSimulation.csc` using *cooja*. <br>
<br>
#### optional:
**As first** device add the **rpl-border-router**, present inside the `contiki-ng/examples/rpl-border-router/` folder.<br>
**Then** deploy a **single** device per type. The devices code can be found inside their relative folders: `MQTT-network` and `CoAP-network`.<br>
Before starting the system the **border router**:<br>
  -Add the socket on the BR: `Tools` -> `Serial Socket (SERVER)` -> `Contiki 1` –> `Press START`<br>
  -Open a new termianl and move to the folder `contiki-ng/examples/rpl-border-router/` and execute the command `make TARGET=cooja connect-router-cooja`<br>
<br>
### Step 3
Import the **smart_aquarium** db on *mysql*.

### Step 4
Put the speed of the simulation on **1x** and start the simulation.

### To close the app
Issue the command **:quit** to stop the application.
