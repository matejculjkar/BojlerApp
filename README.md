# BojlerApp
This app is used to turn ON/OFF a water heater remotely via SMS which is sent to ARDUINO UNO with SIM and Relay module.

The SMS is constructed with OFF:____ [time in s];ON:____ [time in s], and this interval can be repeated several times in one SMS. The time next to an "OFF:" means that the heater will be OFF from the time of the receiving SMS to the time writen next to an "OFF". And when the time runs out, the heater will be switched ON and will stay on for the time writen next to "ON:".

Example: We sent an SMS: OFF:240;ON:600; and let's say that the time when unit receives SMS is 9.03. So the heater will be OFF for 4 minutes - till 9.07, and afterwards will turn ON and will stay on till 9.13 (600s-240s=6mins --> 9.07+6mins=9.13). And after that time the heater will switch OFF.

The app has also a possibility to run certain programs of heating, for example to heat water only for certain amount of time, or to only heat the water during cheaper electricity (TBD). You can use it on multiple devices, because all work is synchronized. App also picks up the weather data to make heating times shorter or longer regarding the outside temperature.

The file Celotna.ino contains code for Arduino.
