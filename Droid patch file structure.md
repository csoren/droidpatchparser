# DRP file structure

#### Offset 0x0000:
  7 bytes "DRP\0\0\0\0"
#### Offset 0x0007
  256 bytes "Patch name\0"
#### Offset 0x0107
  256 bytes "Author\0"
#### Offset 0x0207
  2048 bytes "Comment\0" (0x0D 0x0A linefeed)
#### Offset 0xA07
  Little endian 32 bit words
*  0xA07 - Midi channel minus 1
*  0xA0B - DCO1 Amplitude CC 29
*  0xA0F - DCO1 Frequency CC 31
*  0xA13 - DCO1 Offset CC 28
*  0xA17 - DCO1 Pulsewidth CC 27
*  0xA1B - DCO2 Amplitude CC 105
*  0xA1F - DCO2 Frequency CC 107
*  0xA23 - DCO2 Offset CC 104
*  0xA27 - DCO2 Pulsewidth CC 103
*  0xA2B - ENV1 Offset CC 113 
*  0xA2F - ENV2 Offset CC 119
*  0xA33 - DCO1 Octave (CC 30 bit 3:0)
*  0xA37 - DCO2 Octave (CC 106 bit 3:0)
*  0xA3B - ENV1 Attack CC 108
*  0xA3F - ENV1 Decay CC 110
*  0xA43 - ENV1 Attack Level CC 109
*  0xA47 - ENV1 Release CC 112
*  0xA4B - ENV1 Sustain CC 111
*  0xA4F - ENV2 Attack CC 114
*  0xA53 - ENV2 Decay CC 116
*  0xA57 - ENV2 Attack Level CC 115
*  0xA5B - ENV2 Release CC 118
*  0xA5F - ENV2 Sustain CC 117
*  0xA63 - DCO2 and envelope 2 step amount CC 24
*  0xA67 - Arpeggio CC 23
*  0xA6B - Filter Frequency 1 CC 21
*  0xA6F - Filter Width / Frequency 2 CC 22
*  0xA73 - DCO1 Amplitude matrix controller CC 29 (0 - none, 1 - 16 matrix controller plus 1)
*  0xA77 - DCO1 Distortion (CC 26 bit 5:4)
*  0xA7B - DCO1 Frequency matrix controller CC 31 (0 - none, 1 - 16 matrix controller plus 1)
*  0xA7F - DCO1 Offset matrix controller CC 28 (0 - none, 1 - 16 matrix controller plus 1)
*  0xA83 - DCO1 Pulsewidth matrix controller CC 27 (0 - none, 1 - 16 matrix controller plus 1)
*  0xA87 - DCO1 Waveform (0-7 -> CC 26 bit 2:0)
                         (8-23 -> Matrix controller)
*  0xA8B - Arpeggio matrix controller CC 23 (0 - none, 1 - 16 matrix controller plus 1)
*  0xA8F - Filter Frequency 1 matrix controller CC 21 (0 - none, 1 - 16 matrix controller plus 1)
*  0xA93 - DCO2 Amplitude matrix controller CC 105 (0 - none, 1 - 16 matrix controller plus 1)
*  0xA97 - DCO2 Distortion (CC 102 bit 5:4)
*  0xA9B - DCO2 Frequency matrix controller CC 107 (0 - none, 1 - 16 matrix controller plus 1)
*  0xA9F - DCO2 Offset matrix controller CC 104 (0 - none, 1 - 16 matrix controller plus 1)
*  0xAA3 - DCO2 Pulsewidth matrix controller CC 103 (0 - none, 1 - 16 matrix controller plus 1)
*  0xAA7 - DCO2 Waveform (0-7 -> CC 102 bit 2:0)
                         (8-23 -> Matrix controller)
*  0xAAB - ENV1 Offset matrix controller CC 113 (0 - none, 1 - 16 matrix controller plus 1)
*  0xAAF - ENV2 Offset matrix controller CC 119 (0 - none, 1 - 16 matrix controller plus 1)
*  0xAB3 - Filter type/routing
*  0xAB7 - Midi channel minus 1
*  0xABB - Filter Width / Frequency 2 matrix controller CC 22 (0 - none, 1 - 16 matrix controller plus 1)
*  0xABF - DCO1 Frequency tuning mode (CC 30 bit 5:4)
*  0xAC3 - DCO2 Frequency tuning mode (CC 102 bit 5:4)
*  0xAC7 - Various modes (CC 25)
*  0xACB - Mixing structure (CC 20)
