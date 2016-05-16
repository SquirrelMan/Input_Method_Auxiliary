#!/bin/bash


echo high > /sys/class/gpio/gpio67/direction
echo high > /sys/class/gpio/gpio65/direction
echo high > /sys/class/gpio/gpio44/direction
echo high > /sys/class/gpio/gpio46/direction
echo high > /sys/class/gpio/gpio68/direction
echo high > /sys/class/gpio/gpio26/direction
echo 0 > /sys/class/gpio/gpio65/value
echo 0 > /sys/class/gpio/gpio67/value
echo 0 > /sys/class/gpio/gpio68/value
echo 0 > /sys/class/gpio/gpio44/value
echo 0 > /sys/class/gpio/gpio46/value
echo 0 > /sys/class/gpio/gpio26/value

#ENA
echo 1 > /sys/class/gpio/gpio26/value
#ENB
echo 1 > /sys/class/gpio/gpio65/value
#IN1
echo 1 > /sys/class/gpio/gpio44/value
#IN3
echo 1 > /sys/class/gpio/gpio46/value

