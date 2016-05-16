import Adafruit_BBIO.GPIO as gpio
import time
from time import sleep

def getchar():
   #Returns a single character from standard input
   import tty, termios, sys
   fd = sys.stdin.fileno()
   old_settings = termios.tcgetattr(fd)
   try:
      tty.setraw(sys.stdin.fileno())
      ch = sys.stdin.read(1)
   finally:
      termios.tcsetattr(fd, termios.TCSADRAIN, old_settings)
   return ch

#ENA
gpio.setup("P8_14", gpio.OUT)
gpio.output("P8_14", gpio.HIGH)

#ENB
gpio.setup("P8_18", gpio.OUT)
gpio.output("P8_18", gpio.HIGH)

#=======================================

#IN1 BACK
gpio.setup("P8_12", gpio.OUT)
gpio.output("P8_12", gpio.HIGH)

#IN2 FORWARD
gpio.setup("P8_11", gpio.OUT)
gpio.output("P8_11", gpio.HIGH)

#IN3 BACK
gpio.setup("P8_16", gpio.OUT)
gpio.output("P8_16", gpio.HIGH)

#IN4 FORWARD
gpio.setup("P8_15", gpio.OUT)
gpio.output("P8_15", gpio.HIGH)


#=======================================

def stop():
    gpio.output("P8_12",gpio.LOW)
    gpio.output("P8_11",gpio.LOW)
    gpio.output("P8_16",gpio.LOW)
    gpio.output("P8_15",gpio.LOW)
    
def forward():
    gpio.output("P8_11",gpio.HIGH)
    gpio.output("P8_15",gpio.HIGH)
    
def back():
    gpio.output("P8_12",gpio.HIGH)
    gpio.output("P8_16",gpio.HIGH)
    
def left():
    gpio.output("P8_11",gpio.HIGH)
    
def right():
    gpio.output("P8_15",gpio.HIGH)
    
def leftb():
    gpio.output("P8_16",gpio.HIGH)
    
def rightb():
    gpio.output("P8_12",gpio.HIGH)

while True:
    stop()
    ch=getchar()
    if(ch=='q'):
	break
    if(ch=='g'):
	forward()
    if(ch=='b'):
	back()
    if(ch=='f'):
	left()
    if(ch=='h'):
	right()
    if(ch=='v'):
	leftb()
    if(ch=='n'):
	rightb()
    sleep(0.025)

