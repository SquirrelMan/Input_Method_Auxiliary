#!/bin/bash


echo high > /sys/class/gpio/gpio26/direction
echo high > /sys/class/gpio/gpio65/direction
echo high > /sys/class/gpio/gpio44/direction
echo high > /sys/class/gpio/gpio45/direction
echo high > /sys/class/gpio/gpio46/direction
echo high > /sys/class/gpio/gpio47/direction
echo 0 > /sys/class/gpio/gpio26/value
echo 0 > /sys/class/gpio/gpio65/value
echo 0 > /sys/class/gpio/gpio44/value
echo 0 > /sys/class/gpio/gpio45/value
echo 0 > /sys/class/gpio/gpio46/value
echo 0 > /sys/class/gpio/gpio47/value

#ENA
echo 1 > /sys/class/gpio/gpio26/value
#ENB
echo 1 > /sys/class/gpio/gpio65/value
#IN2
echo 1 > /sys/class/gpio/gpio45/value

