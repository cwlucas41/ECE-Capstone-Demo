import subprocess
import sys

class Gain:

  def __init__(self,gain,r1i2chex,r2i2chex):
    self.gain = gain 
    self.r1_i2c_hex = r1i2chex
    self.r2_i2c_hex= r2i2chex


class Frequency:

  def __init__(self,freq,r3):
    self.freq = freq
    self.r3_i2c_hex = r3




#globals
r1andr2file = open("R1ANDR2")
r3file = open("R3")
gains = []
freqs = []

address = 0x50
r1 = 0xf9
r2 = 0xfa
r3 = 0xf8

def setGain(desiredGain):
  valueR1 = 0
  valueR2 = 0
  for gain in gains:
    if desiredGain >= gain.gain:
      valueR1 = gain.r1_i2c_hex
      valueR2 = gain.r2_i2c_hex
    else:
      break
  
  i2c_command_r1 = "i2cset -y 1 {} {} {}".format(hex(address),hex(r1),hex(valueR1))
  i2c_command_r2 = "i2cset -y 1 {} {} {}".format(hex(address),hex(r2),hex(valueR2))
  subprocess.call(i2c_command_r1.split())
  subprocess.call(i2c_command_r2.split())

def setFreq(desiredFreq):
  for freq in freqs:
    if desiredFreq <= freq.freq:
      valueR3 = freq.r3_i2c_hex
    else:
      break
  
  i2c_command = "i2cset -y 1 {} {} {}".format(hex(address),hex(r3),hex(valueR3))
  subprocess.call(i2c_command.split())


def main():
  for line in r1andr2file:
    fileLine = line.strip("\n").split()
    #print fileLine[0], fileLine[1], fileLine[2]
    gains.append(Gain(float(fileLine[0]),int(fileLine[1],16),int(fileLine[2],16)))
  
  for line in r3file:
    fileLine = line.strip("\n").split()
    freqs.append(Frequency(float(fileLine[0]),int(fileLine[1],16)))

  r1andr2file.close()
  r3file.close()
  if len(sys.argv) == 1:   
    #interactive mode
    while True:
      command = raw_input("enter a command (g for gain or f for sample frequency) followed by a desired value with a space. e.g. g 1.2\n")
      splitcommand = command.split()
      valueR1 = 0
      valueR2 = 0
      valueR3 = 0
      
      if splitcommand[0] == 'g':
        #gain
        desiredGain = float(splitcommand[1])
        setGain(desiredGain)

      elif splitcommand[0] == 'f':
        #frequency
        desiredFreq= float(splitcommand[1])
        setFreq(desiredFreq)
        
  else:
    mode = sys.argv[1]
    value = sys.argv[2]
    
    if mode == 'g':
      #gain
      desiredGain = float(value)
      setGain(desiredGain)

    elif mode == 'f':
      desiredFreq= float(value)
      setFreq(desiredFreq) 
    


if __name__ == "__main__":
  main()
